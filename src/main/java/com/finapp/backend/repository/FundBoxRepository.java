package com.finapp.backend.repository;

import com.finapp.backend.model.FundBox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FundBoxRepository extends JpaRepository<FundBox, Long> {
    List<FundBox> findByOwnerId(Long ownerId);
    boolean existsByOwnerIdAndName(Long ownerId, String name);
}
