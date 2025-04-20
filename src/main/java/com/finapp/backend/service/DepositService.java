package com.finapp.backend.service;

import com.finapp.backend.dto.deposit.CreateDepositRequest;
import com.finapp.backend.dto.deposit.DepositResponse;
import com.finapp.backend.dto.deposit.DepositSummaryResponse;
import com.finapp.backend.dto.deposit.UpdateDepositRequest;
import com.finapp.backend.dto.deposit.FundBoxInfo;
import com.finapp.backend.exception.ApiErrorCode;
import com.finapp.backend.exception.ApiException;
import com.finapp.backend.model.Deposit;
import com.finapp.backend.model.FundBox;
import com.finapp.backend.model.User;
import com.finapp.backend.model.enums.TransactionType;
import com.finapp.backend.repository.DepositRepository;
import com.finapp.backend.repository.FundBoxRepository;
import com.finapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepositService {

    private final DepositRepository depositRepository;
    private final UserRepository userRepository;
    private final FundBoxRepository fundBoxRepository;

    public void createDeposit(String email, CreateDepositRequest request) {
        User user = getActiveUserByEmail(email);
        validateCreateRequest(request);

        FundBox fundBox = null;
        if (request.getFundBoxId() != null)
            fundBox = findAndValidateFundBox(request.getFundBoxId(), email);
        Deposit deposit = new Deposit();
        deposit.setAmount(request.getAmount());
        deposit.setDate(request.getDate());
        if (request.getDescription() != null)
            deposit.setDescription(request.getDescription().trim());
        deposit.setTransactionType(request.getTransactionType());
        deposit.setUser(user);
        deposit.setFundBox(fundBox);

        depositRepository.save(deposit);
    }

    public ResponseEntity<Page<DepositResponse>> listUserDeposits(String email, List<TransactionType> transactionTypes, Pageable pageable) {
        User user = getActiveUserByEmail(email);

        Page<Deposit> depositPage = (transactionTypes == null || transactionTypes.isEmpty())
                ? depositRepository.findByUserId(user.getId(), pageable)
                : depositRepository.findByUserIdAndTransactionTypeIn(user.getId(), transactionTypes, pageable);

        if (depositPage.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        Page<DepositResponse> depositResponses = depositPage.map(this::mapToDepositResponse);
        return ResponseEntity.ok(depositResponses);
    }

    public DepositResponse getDepositById(Long depositId, String email) {
        User user = getActiveUserByEmail(email);

        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.DEPOSIT_NOT_FOUND));

        FundBox fundBox = deposit.getFundBox();
        if (fundBox == null) {
            if (!deposit.getUser().getId().equals(user.getId()))
                throw new ApiException(ApiErrorCode.UNAUTHORIZED_ACCESS);
        } else {
            if (!fundBox.getOwner().getId().equals(user.getId()) &&
                    fundBox.getCollaborators().stream().noneMatch(collaborator -> collaborator.getUser().getId().equals(user.getId()))) {
                throw new ApiException(ApiErrorCode.UNAUTHORIZED_ACCESS);
            }
        }

        return mapToDepositResponse(deposit);
    }



    public DepositSummaryResponse getDepositSummary(String email) {
        User user = getActiveUserByEmail(email);

        BigDecimal entryTotal = Optional.ofNullable(
                depositRepository.sumByUserIdAndTransactionType(user.getId(), TransactionType.ENTRY)
        ).orElse(BigDecimal.ZERO);

        BigDecimal exitTotal = Optional.ofNullable(
                depositRepository.sumByUserIdAndTransactionType(user.getId(), TransactionType.EXIT)
        ).orElse(BigDecimal.ZERO);

        BigDecimal balance = entryTotal.subtract(exitTotal);

        return new DepositSummaryResponse(balance, entryTotal, exitTotal);
    }

    public DepositResponse updateDeposit(Long depositId, String email, UpdateDepositRequest request) {
        User user = getActiveUserByEmail(email);
        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.DEPOSIT_NOT_FOUND));

        if (!deposit.getUser().getId().equals(user.getId()))
            throw new ApiException(ApiErrorCode.UNAUTHORIZED_ACCESS);

        if (request.getAmount() != null)
            updateAmount(deposit, request.getAmount());
        if (request.getDate() != null)
            updateDate(deposit, request.getDate());
        if (request.getTransactionType() != null)
            updateTransactionType(deposit, request.getTransactionType());
        if (request.getDescription() != null)
            updateDescription(deposit, request.getDescription());
        if (request.getFundBoxId() != null)
            updateFundBox(deposit, request.getFundBoxId(), email);

        depositRepository.save(deposit);
        return mapToDepositResponse(deposit);
    }


    public void deleteDeposit(Long depositId, String email) {
        User user = getActiveUserByEmail(email);

        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.DEPOSIT_NOT_FOUND));

        if (!deposit.getUser().getId().equals(user.getId()))
            throw new ApiException(ApiErrorCode.UNAUTHORIZED_ACCESS);

        depositRepository.delete(deposit);
    }



    // aux methods
    private void validateCreateRequest(CreateDepositRequest request) {
        validateAmount(request.getAmount());
        validateDate(request.getDate());
        validateTransactionType(request.getTransactionType());
        validateDescription(request.getDescription());
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new ApiException(ApiErrorCode.INVALID_AMOUNT);
    }

    private void validateDate(LocalDate date) {
        if (date == null || date.isAfter(LocalDate.now()))
            throw new ApiException(ApiErrorCode.INVALID_DATE_FUTURE);
    }

    private void validateTransactionType(TransactionType transactionType) {
        if (transactionType == null)
            throw new ApiException(ApiErrorCode.INVALID_TRANSACTION_TYPE);
    }

    private void validateDescription(String description) {
        if (description != null) {
            String trimmed = description.trim();
            if (trimmed.isEmpty() || trimmed.length() > 255) {
                throw new ApiException(ApiErrorCode.INVALID_DESCRIPTION);
            }
        }
    }

    private FundBox findAndValidateFundBox(Long fundBoxId, String email) {
        if (fundBoxId == null)
            throw new ApiException(ApiErrorCode.FUND_BOX_NOT_FOUND);

        FundBox fundBox = fundBoxRepository.findById(fundBoxId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.FUND_BOX_NOT_FOUND));

        boolean isOwnerOrCollaborator = fundBox.getOwner().getEmail().equals(email) ||
                fundBox.getCollaborators().stream().anyMatch(collaborator -> collaborator.getUser().getEmail().equals(email));

        if (!isOwnerOrCollaborator)
            throw new ApiException(ApiErrorCode.UNAUTHORIZED_ACCESS);

        return fundBox;
    }

    private void updateAmount(Deposit deposit, BigDecimal amount) {
        if (amount == null) return;
        validateAmount(amount);
        deposit.setAmount(amount);
    }

    private void updateDate(Deposit deposit, LocalDate date) {
        if (date == null) return;
        validateDate(date);
        deposit.setDate(date);
    }

    private void updateTransactionType(Deposit deposit, TransactionType transactionType) {
        if (transactionType == null) return;
        try {
            TransactionType validType = TransactionType.valueOf(transactionType.name());
            deposit.setTransactionType(validType);
        } catch (IllegalArgumentException e) {
            throw new ApiException(ApiErrorCode.INVALID_TRANSACTION_TYPE);
        }
    }

    private void updateDescription(Deposit deposit, String description) {
        if (description == null || description.trim().isEmpty()) return;
        validateDescription(description);
        deposit.setDescription(description.trim());
    }

    private void updateFundBox(Deposit deposit, Long fundBoxId, String email) {
        if (fundBoxId == null) {
            deposit.setFundBox(null);
            return;
        }

        if (deposit.getFundBox() != null && deposit.getFundBox().getId().equals(fundBoxId))
            return;

        FundBox fundBox = fundBoxRepository.findById(fundBoxId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.FUND_BOX_NOT_FOUND));

        if (!fundBox.getOwner().getEmail().equals(email))
            throw new ApiException(ApiErrorCode.UNAUTHORIZED_ACCESS);

        deposit.setFundBox(fundBox);
    }

    private User getActiveUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND));

        if (!Boolean.TRUE.equals(user.getActive()))
            throw new ApiException(ApiErrorCode.ACCOUNT_DEACTIVATED);
        return user;
    }

    private DepositResponse mapToDepositResponse(Deposit deposit) {
        FundBoxInfo fundBoxInfo = null;
        if (deposit.getFundBox() != null) {
            fundBoxInfo = new FundBoxInfo(
                    deposit.getFundBox().getId(),
                    deposit.getFundBox().getName()
            );
        }
        return new DepositResponse(
                deposit.getId(),
                deposit.getTransactionType() == TransactionType.EXIT
                        ? deposit.getAmount().negate()
                        : deposit.getAmount(),
                deposit.getDate(),
                deposit.getDescription(),
                fundBoxInfo,
                deposit.getTransactionType().toString()
        );
    }

}