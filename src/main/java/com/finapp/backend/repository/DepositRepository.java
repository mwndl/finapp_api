package com.finapp.backend.repository;

import com.finapp.backend.model.Deposit;
import com.finapp.backend.model.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepositRepository extends JpaRepository<Deposit, Long> {
    List<Deposit> findByUserId(Long userId);
    List<Deposit> findByUserIdAndTransactionTypeIn(Long userId, List<TransactionType> transactionTypes);
    List<Deposit> findByFundBoxId(Long fundBoxId);
}
