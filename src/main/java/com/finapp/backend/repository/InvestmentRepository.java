package com.finapp.backend.repository;

import com.finapp.backend.model.Investment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvestmentRepository extends JpaRepository<Investment, Long> {
    List<Investment> findByUserId(Long userId);
    List<Investment> findByFundBoxId(Long fundBoxId);
}
