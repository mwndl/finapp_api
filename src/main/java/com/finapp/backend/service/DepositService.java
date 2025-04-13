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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepositService {

    private final DepositRepository depositRepository;
    private final UserRepository userRepository;
    private final FundBoxRepository fundBoxRepository;

    public void createDeposit(String email, CreateDepositRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND));

        if (!user.getActive()) {
            throw new ApiException(ApiErrorCode.ACCOUNT_DEACTIVATED);
        }

        FundBox fundBox = request.getFundBoxId() != null ?
                fundBoxRepository.findById(request.getFundBoxId())
                        .orElseThrow(() -> new ApiException(ApiErrorCode.FUND_BOX_NOT_FOUND))
                : null;

        Deposit deposit = new Deposit();
        deposit.setAmount(request.getAmount());
        deposit.setDate(request.getDate());
        deposit.setDescription(request.getDescription());
        deposit.setTransactionType(request.getTransactionType());
        deposit.setUser(user);
        deposit.setFundBox(fundBox);

        depositRepository.save(deposit);
    }

    public Page<DepositResponse> listUserDeposits(String email, List<TransactionType> transactionTypes, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND));

        if (!user.getActive()) {
            throw new ApiException(ApiErrorCode.ACCOUNT_DEACTIVATED);
        }

        Page<Deposit> deposits = (transactionTypes == null || transactionTypes.isEmpty())
                ? depositRepository.findByUserId(user.getId(), pageable)
                : depositRepository.findByUserIdAndTransactionTypeIn(user.getId(), transactionTypes, pageable);


        return deposits.map(deposit -> {
            DepositResponse response = new DepositResponse();
            response.setId(deposit.getId());
            response.setAmount(deposit.getTransactionType() == TransactionType.EXIT
                    ? deposit.getAmount().negate()
                    : deposit.getAmount());
            response.setDate(deposit.getDate());
            response.setDescription(deposit.getDescription());
            response.setTransactionType(deposit.getTransactionType().toString());
            response.setFundBoxName(deposit.getFundBox() != null ? deposit.getFundBox().getName() : null);
            return response;
        });
    }

    public DepositSummaryResponse getDepositSummary(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND));

        if (!user.getActive()) {
            throw new ApiException(ApiErrorCode.ACCOUNT_DEACTIVATED);
        }

        BigDecimal entryTotal = depositRepository.sumByUserIdAndTransactionType(user.getId(), TransactionType.ENTRY);
        BigDecimal exitTotal = depositRepository.sumByUserIdAndTransactionType(user.getId(), TransactionType.EXIT);

        // handling null values
        entryTotal = entryTotal != null ? entryTotal : BigDecimal.ZERO;
        exitTotal = exitTotal != null ? exitTotal : BigDecimal.ZERO;

        return new DepositSummaryResponse(
                entryTotal.subtract(exitTotal),
                entryTotal,
                exitTotal
        );
    }

    public void updateDeposit(Long depositId, String email, UpdateDepositRequest request) {
        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.DEPOSIT_NOT_FOUND));

        FundBox fundBox = request.getFundBoxId() != null ?
                fundBoxRepository.findById(request.getFundBoxId())
                        .orElseThrow(() -> new ApiException(ApiErrorCode.FUND_BOX_NOT_FOUND))
                : null;

        if (request.getAmount() != null) {
            if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ApiException(ApiErrorCode.INVALID_AMOUNT);
            }
            deposit.setAmount(request.getAmount());
        }
        if (request.getDate() != null) deposit.setDate(request.getDate());
        if (request.getTransactionType() != null) deposit.setTransactionType(request.getTransactionType());

        if (request.getDescription() != null) deposit.setDescription(request.getDescription());
        if (request.getFundBoxId() != null) deposit.setFundBox(fundBox);

        depositRepository.save(deposit);
    }




}
