package com.finapp.backend.repository;

import com.finapp.backend.model.FundBox;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FundBoxRepository extends JpaRepository<FundBox, Long> {
    boolean existsByOwnerIdAndName(Long ownerId, String name);
    Page<FundBox> findByOwnerId(Long ownerId, Pageable pageable);
    Optional<FundBox> findByIdAndOwnerId(Long id, Long ownerId);
}
