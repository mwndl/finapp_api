package com.finapp.backend.features.v1.deposit.repository;

import com.finapp.backend.domain.model.Deposit;
import com.finapp.backend.domain.enums.TransactionType;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface DepositRepository extends JpaRepository<Deposit, UUID> {
    Page<Deposit> findByUserId(UUID userId, Pageable pageable);
    Page<Deposit> findByUserIdAndTransactionTypeIn(UUID userId, List<TransactionType> transactionTypes, Pageable pageable);

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Deposit d WHERE d.fundBox.id = :fundBoxId AND d.transactionType = :transactionType")
    BigDecimal sumByFundBoxIdAndTransactionType(@Param("fundBoxId") UUID fundBoxId, @Param("transactionType") TransactionType transactionType);

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Deposit d WHERE d.user.id = :userId AND d.transactionType = :transactionType")
    BigDecimal sumByUserIdAndTransactionType(@Param("userId") UUID userId, @Param("transactionType") TransactionType transactionType);

    Page<Deposit> findByFundBoxId(UUID fundBoxId, Pageable pageable);
    List<Deposit> findByFundBoxId(UUID fundBoxId);

    @Transactional
    @Modifying
    @Query("UPDATE Deposit d SET d.fundBox = NULL WHERE d.user.id = :userId AND d.fundBox.id = :fundBoxId")
    void unsetFundBoxForUserDeposits(@Param("userId") UUID userId, @Param("fundBoxId") UUID fundBoxId);
}
