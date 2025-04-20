package com.finapp.backend.repository;

import com.finapp.backend.model.FundBox;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FundBoxRepository extends JpaRepository<FundBox, Long> {
    boolean existsByOwnerIdAndName(Long ownerId, String name);

    @Query("SELECT f FROM FundBox f WHERE f.owner.id = :userId OR :userId IN (SELECT fc.user.id FROM FundBoxCollaborator fc WHERE fc.fundBox.id = f.id)")
    Page<FundBox> findByOwnerIdOrCollaboratorsContaining(@Param("userId") Long userId, Pageable pageable);

    Page<FundBox> findByOwnerId(Long ownerId, Pageable pageable);
    Optional<FundBox> findByIdAndOwnerId(Long id, Long ownerId);
}
