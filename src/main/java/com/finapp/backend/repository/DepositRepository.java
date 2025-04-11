package com.finapp.backend.repository;

import com.finapp.backend.model.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepositRepository extends JpaRepository<Deposit, Long> {
    List<Deposit> findByUserId(Long userId);
    List<Deposit> findByFundBoxId(Long fundBoxId);
}
