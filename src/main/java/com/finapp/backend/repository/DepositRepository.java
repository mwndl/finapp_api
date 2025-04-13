package com.finapp.backend.repository;

import com.finapp.backend.model.Deposit;
import com.finapp.backend.model.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface DepositRepository extends JpaRepository<Deposit, Long> {
    Page<Deposit> findByUserId(Long userId, Pageable pageable);
    Page<Deposit> findByUserIdAndTransactionTypeIn(Long userId, List<TransactionType> transactionTypes, Pageable pageable);

    @Query("SELECT SUM(d.amount) FROM Deposit d WHERE d.user.id = :userId AND d.transactionType = :type")
    BigDecimal sumByUserIdAndTransactionType(@Param("userId") Long userId, @Param("type") TransactionType type);

    List<Deposit> findByFundBoxId(Long fundBoxId);
}
