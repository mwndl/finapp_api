package com.finapp.backend.service;

import com.finapp.backend.dto.user.InviteResponse;
import com.finapp.backend.exception.ApiErrorCode;
import com.finapp.backend.exception.ApiException;
import com.finapp.backend.model.FundBox;
import com.finapp.backend.model.FundBoxCollaborator;
import com.finapp.backend.model.FundBoxInvitation;
import com.finapp.backend.model.User;
import com.finapp.backend.model.enums.InvitationStatus;
import com.finapp.backend.repository.FundBoxInvitationRepository;
import com.finapp.backend.repository.FundBoxRepository;
import com.finapp.backend.service.managers.FundBoxManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FundBoxInviteService {

    private final FundBoxManager fundBoxManager;
    private final FundBoxInvitationRepository fundBoxInvitationRepository;
    private final FundBoxRepository fundBoxRepository;

    public void inviteCollaborator(Long fundBoxId, String email, Long collaboratorId) {
        User inviter = fundBoxManager.getUserByEmail(email);
        fundBoxManager.checkUserStatus(inviter);

        FundBox fundBox = fundBoxManager.getFundBoxById(fundBoxId, inviter);

        if (!fundBox.getOwner().getId().equals(inviter.getId()))
            throw new ApiException(ApiErrorCode.FORBIDDEN_COLLABORATOR_ADDITION);

        if (fundBox.getOwner().getId().equals(collaboratorId))
            throw new ApiException(ApiErrorCode.COLLABORATOR_CANNOT_BE_OWNER);

        User invitee = fundBoxManager.getUserById(collaboratorId);
        fundBoxManager.checkUserStatus(invitee);

        boolean isAlreadyCollaborator = fundBox.getCollaborators().stream()
                .anyMatch(c -> c.getUser().getId().equals(collaboratorId));
        if (isAlreadyCollaborator)
            throw new ApiException(ApiErrorCode.COLLABORATOR_ALREADY_EXISTS);

        boolean alreadyInvited = fundBoxInvitationRepository.existsByFundBoxAndInviteeAndStatus(fundBox, invitee, InvitationStatus.PENDING);
        if (alreadyInvited)
            throw new ApiException(ApiErrorCode.COLLABORATOR_ALREADY_INVITED);

        FundBoxInvitation invitation = new FundBoxInvitation();
        invitation.setFundBox(fundBox);
        invitation.setInviter(inviter);
        invitation.setInvitee(invitee);
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setInvitationDate(LocalDateTime.now());

        fundBoxInvitationRepository.save(invitation);
    }

    public Page<InviteResponse> getSentInvites(String email, Pageable pageable) {
        User inviter = fundBoxManager.getUserByEmail(email);
        return fundBoxInvitationRepository.findByInviter(inviter, pageable)
                .map(fundBoxManager::toInviteResponse);
    }

    public void cancelInvitation(Long invitationId, String email) {
        FundBoxInvitation invitation = fundBoxInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.INVITATION_NOT_FOUND));

        User inviter = fundBoxManager.getUserByEmail(email);
        if (!invitation.getInviter().getId().equals(inviter.getId()))
            throw new ApiException(ApiErrorCode.FORBIDDEN_ACTION);

        if (invitation.getStatus() != InvitationStatus.PENDING)
            throw new ApiException(ApiErrorCode.INVITATION_CANNOT_BE_CANCELED);

        fundBoxInvitationRepository.delete(invitation);
    }

    public Page<InviteResponse> getUserInvites(String username, Pageable pageable) {
        User user = fundBoxManager.getUserByEmail(username);
        Page<FundBoxInvitation> invites = fundBoxInvitationRepository.findByInvitee_Id(user.getId(), pageable);
        return invites.map(fundBoxManager::toInviteResponse);
    }

    public void acceptInvitation(Long invitationId, String email) {
        FundBoxInvitation invitation = fundBoxManager.validateInvitationForUser(invitationId, email);

        if (invitation.getStatus() == InvitationStatus.ACCEPTED)
            throw new ApiException(ApiErrorCode.INVITATION_ALREADY_ACCEPTED);

        FundBox fundBox = invitation.getFundBox();
        FundBoxCollaborator relation = new FundBoxCollaborator();
        relation.setFundBox(fundBox);
        relation.setUser(invitation.getInvitee());
        fundBox.getCollaborators().add(relation);

        fundBoxRepository.save(fundBox);
        fundBoxInvitationRepository.delete(invitation);
    }

    public void declineInvitation(Long invitationId, String email) {
        FundBoxInvitation invitation = fundBoxManager.validateInvitationForUser(invitationId, email);
        fundBoxInvitationRepository.delete(invitation);
    }

}
