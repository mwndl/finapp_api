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

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Deposit d WHERE d.fundBox.id = :fundBoxId AND d.transactionType = :transactionType")
    BigDecimal sumByFundBoxIdAndTransactionType(@Param("fundBoxId") Long fundBoxId, @Param("transactionType") TransactionType transactionType);

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Deposit d WHERE d.user.id = :userId AND d.transactionType = :transactionType")
    BigDecimal sumByUserIdAndTransactionType(@Param("userId") Long userId, @Param("transactionType") TransactionType transactionType);

    Page<Deposit> findByFundBoxId(Long fundBoxId, Pageable pageable);
    List<Deposit> findByFundBoxId(Long fundBoxId);
}
