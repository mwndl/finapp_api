package com.finapp.backend.service;

import com.finapp.backend.dto.deposit.CreateDepositRequest;
import com.finapp.backend.dto.deposit.DepositResponse;
import com.finapp.backend.dto.deposit.DepositSummaryResponse;
import com.finapp.backend.dto.deposit.UpdateDepositRequest;
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

        FundBox fundBox = findAndValidateFundBox(request.getFundBoxId(), email);

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

    public Page<DepositResponse> listUserDeposits(String email, List<TransactionType> transactionTypes, Pageable pageable) {
        User user = getActiveUserByEmail(email);

        Page<Deposit> depositPage = (transactionTypes == null || transactionTypes.isEmpty())
                ? depositRepository.findByUserId(user.getId(), pageable)
                : depositRepository.findByUserIdAndTransactionTypeIn(user.getId(), transactionTypes, pageable);

        return depositPage.map(this::mapToDepositResponse);
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
        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.DEPOSIT_NOT_FOUND));

        if (!deposit.getUser().getEmail().equals(email)) {
            throw new ApiException(ApiErrorCode.UNAUTHORIZED_ACCESS);
        }

        updateAmount(deposit, request.getAmount());
        updateDate(deposit, request.getDate());
        updateTransactionType(deposit, request.getTransactionType());
        updateDescription(deposit, request.getDescription());
        updateFundBox(deposit, request.getFundBoxId(), email, deposit.getFundBox());

        depositRepository.save(deposit);
        return mapToDepositResponse(deposit);
    }

    public void deleteDeposit(Long depositId, String email) {
        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.DEPOSIT_NOT_FOUND));

        if (!deposit.getUser().getEmail().equals(email)) {
            throw new ApiException(ApiErrorCode.UNAUTHORIZED_ACCESS);
        }

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
        if (fundBoxId == null) return null;

        FundBox fundBox = fundBoxRepository.findById(fundBoxId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.FUND_BOX_NOT_FOUND));

        if (!fundBox.getOwner().getEmail().equals(email))
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

    private void updateFundBox(Deposit deposit, Long fundBoxId, String email, FundBox currentFundBox) {
        if (fundBoxId == null) return;
        if (currentFundBox != null && currentFundBox.getId().equals(fundBoxId)) return;

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
        DepositResponse response = new DepositResponse();
        response.setId(deposit.getId());
        response.setAmount(deposit.getTransactionType() == TransactionType.EXIT
                ? deposit.getAmount().negate()
                : deposit.getAmount());
        response.setDate(deposit.getDate());
        response.setDescription(deposit.getDescription());
        response.setTransactionType(deposit.getTransactionType().toString());

        if (deposit.getFundBox() != null) {
            FundBoxInfo fundBoxInfo = new FundBoxInfo();
            fundBoxInfo.setId(deposit.getFundBox().getId());
            fundBoxInfo.setName(deposit.getFundBox().getName());
            response.setFundBox(fundBoxInfo);
        }

        return response;
    }

}