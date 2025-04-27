package com.finapp.backend.domain.service;

import com.finapp.backend.dto.deposit.CreateDepositRequest;
import com.finapp.backend.dto.deposit.DepositResponse;
import com.finapp.backend.dto.deposit.DepositSummaryResponse;
import com.finapp.backend.dto.deposit.UpdateDepositRequest;
import com.finapp.backend.dto.deposit.FundBoxInfo;
import com.finapp.backend.dto.fundbox.OwnerResponse;
import com.finapp.backend.exception.ApiErrorCode;
import com.finapp.backend.exception.ApiException;
import com.finapp.backend.domain.model.Deposit;
import com.finapp.backend.domain.model.FundBox;
import com.finapp.backend.domain.model.User;
import com.finapp.backend.domain.model.enums.TransactionType;
import com.finapp.backend.domain.repository.DepositRepository;
import com.finapp.backend.domain.repository.FundBoxRepository;
import com.finapp.backend.domain.repository.UserRepository;
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
        FundBox fundBox = (request.getFundBoxId() != null) ? validateAndGetFundBox(request.getFundBoxId(), email) : null;
        Deposit deposit = createDepositFromRequest(request, user, fundBox);
        depositRepository.save(deposit);
    }


    public ResponseEntity<Page<DepositResponse>> listUserDeposits(String email, List<TransactionType> transactionTypes, Pageable pageable) {
        User user = getActiveUserByEmail(email);
        Page<Deposit> depositPage = getUserDeposits(user, transactionTypes, pageable);
        if (depositPage.isEmpty())
            return ResponseEntity.noContent().build();

        Page<DepositResponse> depositResponses = depositPage.map(this::mapToDepositResponse);
        return ResponseEntity.ok(depositResponses);
    }

    public DepositResponse getDepositById(Long depositId, String email) {
        User user = getActiveUserByEmail(email);
        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.DEPOSIT_NOT_FOUND));
        validateDepositAccess(deposit, user);
        return mapToDepositResponse(deposit);
    }

    public DepositSummaryResponse getDepositSummary(String email) {
        User user = getActiveUserByEmail(email);
        BigDecimal entryTotal = calculateTotalForTransactionType(user, TransactionType.ENTRY);
        BigDecimal exitTotal = calculateTotalForTransactionType(user, TransactionType.EXIT);
        BigDecimal balance = entryTotal.subtract(exitTotal);
        return new DepositSummaryResponse(balance, entryTotal, exitTotal);
    }

    public DepositResponse updateDeposit(Long depositId, String email, UpdateDepositRequest request) {
        User user = getActiveUserByEmail(email);
        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.DEPOSIT_NOT_FOUND));
        validateDepositOwnership(deposit, user);
        updateDepositField(deposit, request.getAmount(), request.getDate(), request.getTransactionType(), request.getDescription(), request.getFundBoxId(), email);
        depositRepository.save(deposit);
        return mapToDepositResponse(deposit);
    }

    public void deleteDeposit(Long depositId, String email) {
        User user = getActiveUserByEmail(email);
        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.DEPOSIT_NOT_FOUND));
        validateDepositOwnership(deposit, user);
        depositRepository.delete(deposit);
    }

    // aux methods
    private void validateFundBoxAccess(FundBox fundBox, String email) {
        if (fundBox == null) {
            throw new ApiException(ApiErrorCode.FUND_BOX_NOT_FOUND);
        }

        boolean isOwner = fundBox.getOwner().getEmail().equals(email);
        boolean isCollaborator = fundBox.getCollaborators().stream()
                .anyMatch(collaborator -> collaborator.getUser().getEmail().equals(email));

        if (!isOwner && !isCollaborator) {
            throw new ApiException(ApiErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    private void validateDepositAccess(Deposit deposit, User user) {
        FundBox fundBox = deposit.getFundBox();
        if (fundBox != null) {
            validateFundBoxAccess(fundBox, user.getEmail());
        } else if (!deposit.getUser().getId().equals(user.getId())) {
            throw new ApiException(ApiErrorCode.UNAUTHORIZED_ACCESS);
        }
    }


    private void validateDepositOwnership(Deposit deposit, User user) {
        if (!deposit.getUser().getId().equals(user.getId()))
            throw new ApiException(ApiErrorCode.UNAUTHORIZED_ACCESS);
    }

    private BigDecimal calculateTotalForTransactionType(User user, TransactionType type) {
        return Optional.ofNullable(depositRepository.sumByUserIdAndTransactionType(user.getId(), type))
                .orElse(BigDecimal.ZERO);
    }

    private boolean hasAccessToFundBox(FundBox fundBox, String email) {
        return fundBox.getOwner().getEmail().equals(email) ||
                fundBox.getCollaborators().stream().anyMatch(collaborator -> collaborator.getUser().getEmail().equals(email));
    }

    private Deposit createDepositFromRequest(CreateDepositRequest request, User user, FundBox fundBox) {
        Deposit deposit = new Deposit();
        deposit.setAmount(request.getAmount());
        deposit.setDate(request.getDate());
        if (request.getDescription() != null) {
            deposit.setDescription(request.getDescription().trim());
        }
        deposit.setTransactionType(request.getTransactionType());
        deposit.setUser(user);
        deposit.setFundBox(fundBox);
        return deposit;
    }

    private FundBox validateAndGetFundBox(Long fundBoxId, String email) {
        if (fundBoxId == null)
            throw new ApiException(ApiErrorCode.FUND_BOX_NOT_FOUND);

        FundBox fundBox = fundBoxRepository.findById(fundBoxId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.FUND_BOX_NOT_FOUND));

        if (!hasAccessToFundBox(fundBox, email))
            throw new ApiException(ApiErrorCode.UNAUTHORIZED_ACCESS);

        return fundBox;
    }

    private void updateDepositField(Deposit deposit, BigDecimal amount, LocalDate date, TransactionType transactionType, String description, Long fundBoxId, String email) {
        if (amount != null) updateAmount(deposit, amount);
        if (date != null) updateDate(deposit, date);
        if (transactionType != null) updateTransactionType(deposit, transactionType);
        if (description != null) updateDescription(deposit, description);
        if (fundBoxId != null) updateFundBox(deposit, fundBoxId, email);
    }

    private Page<Deposit> getUserDeposits(User user, List<TransactionType> transactionTypes, Pageable pageable) {
        if (transactionTypes == null || transactionTypes.isEmpty()) {
            return depositRepository.findByUserId(user.getId(), pageable);
        } else {
            return depositRepository.findByUserIdAndTransactionTypeIn(user.getId(), transactionTypes, pageable);
        }
    }

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

        boolean isOwner = fundBox.getOwner().getEmail().equals(email);
        boolean isCollaborator = fundBox.getCollaborators().stream()
                .anyMatch(collaborator -> collaborator.getUser().getEmail().equals(email));

        if (!isOwner && !isCollaborator)
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
        OwnerResponse ownerResponse = null;

        if (deposit.getFundBox() != null) {
            fundBoxInfo = new FundBoxInfo(
                    deposit.getFundBox().getId(),
                    deposit.getFundBox().getName()
            );
        }

        if (deposit.getUser() != null) {
            ownerResponse = new OwnerResponse(
                    deposit.getUser().getId(),
                    deposit.getUser().getName()
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
                deposit.getTransactionType().toString(),
                ownerResponse
        );
    }
}