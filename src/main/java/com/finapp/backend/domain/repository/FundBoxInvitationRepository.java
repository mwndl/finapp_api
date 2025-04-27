package com.finapp.backend.domain.repository;

import com.finapp.backend.domain.model.FundBox;
import com.finapp.backend.domain.model.FundBoxInvitation;
import com.finapp.backend.domain.model.User;
import com.finapp.backend.domain.model.enums.InvitationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FundBoxInvitationRepository extends JpaRepository<FundBoxInvitation, Long> {

    boolean existsByFundBoxAndInviteeAndStatus(FundBox fundBox, User invitee, InvitationStatus status);

    Optional<FundBoxInvitation> findById(Long invitationId);

    Page<FundBoxInvitation> findByInviter(User inviter, Pageable pageable);

    Page<FundBoxInvitation> findByInvitee_Id(Long inviteeId, Pageable pageable);

    List<FundBoxInvitation> findByFundBox_IdAndStatus(Long fundBoxId, InvitationStatus status);

}

