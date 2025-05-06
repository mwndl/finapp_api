package com.finapp.backend.features.v1.fundbox.repository;

import com.finapp.backend.domain.model.FundBox;
import com.finapp.backend.domain.model.FundBoxInvitation;
import com.finapp.backend.domain.model.User;
import com.finapp.backend.domain.enums.InvitationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FundBoxInvitationRepository extends JpaRepository<FundBoxInvitation, UUID> {

    boolean existsByFundBoxAndInviteeAndStatus(FundBox fundBox, User invitee, InvitationStatus status);

    Optional<FundBoxInvitation> findById(UUID invitationId);

    Page<FundBoxInvitation> findByInviter(User inviter, Pageable pageable);

    Page<FundBoxInvitation> findByInvitee_Id(UUID inviteeId, Pageable pageable);

    List<FundBoxInvitation> findByFundBox_IdAndStatus(UUID fundBoxId, InvitationStatus status);

}

