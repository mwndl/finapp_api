package com.finapp.backend.service;

import com.finapp.backend.dto.deposit.CreateDepositRequest;
import com.finapp.backend.dto.deposit.DepositResponse;
import com.finapp.backend.dto.deposit.DepositSummaryResponse;
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

    public List<DepositResponse> listUserDeposits(String email, List<TransactionType> transactionTypes) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND));

        if (!user.getActive()) {
            throw new ApiException(ApiErrorCode.ACCOUNT_DEACTIVATED);
        }

        List<Deposit> deposits = (transactionTypes == null || transactionTypes.isEmpty())
                ? depositRepository.findByUserId(user.getId())
                : depositRepository.findByUserIdAndTransactionTypeIn(user.getId(), transactionTypes);

        return deposits.stream().map(deposit -> {
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
        }).collect(Collectors.toList());
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




}
