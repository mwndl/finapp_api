package com.finapp.backend.domain.repository;

import com.finapp.backend.domain.model.FundBox;
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

    @Query("""
    SELECT f FROM FundBox f
    WHERE f.id = :fundBoxId
      AND (
          f.owner.id = :userId OR
          EXISTS (
              SELECT 1 FROM FundBoxCollaborator fc
              WHERE fc.fundBox.id = :fundBoxId AND fc.user.id = :userId
          )
      )
    """)
    Optional<FundBox> findByIdAndUserIsOwnerOrCollaborator(@Param("fundBoxId") Long fundBoxId, @Param("userId") Long userId);


    Page<FundBox> findByOwnerId(Long ownerId, Pageable pageable);
    Optional<FundBox> findByIdAndOwnerId(Long id, Long ownerId);
}
