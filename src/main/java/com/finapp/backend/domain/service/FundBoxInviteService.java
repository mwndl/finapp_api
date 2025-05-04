package com.finapp.backend.domain.service;

import com.finapp.backend.domain.service.utils.UserUtilService;
import com.finapp.backend.dto.user.InviteResponse;
import com.finapp.backend.exception.ApiErrorCode;
import com.finapp.backend.exception.ApiException;
import com.finapp.backend.domain.model.FundBox;
import com.finapp.backend.domain.model.FundBoxCollaborator;
import com.finapp.backend.domain.model.FundBoxInvitation;
import com.finapp.backend.domain.model.User;
import com.finapp.backend.domain.model.enums.InvitationStatus;
import com.finapp.backend.domain.repository.FundBoxInvitationRepository;
import com.finapp.backend.domain.repository.FundBoxRepository;
import com.finapp.backend.domain.service.utils.FundBoxUtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FundBoxInviteService {

    private final FundBoxUtilService fundBoxManager;
    private final FundBoxInvitationRepository fundBoxInvitationRepository;
    private final FundBoxRepository fundBoxRepository;
    private final UserUtilService userUtilService;

    public void inviteCollaborator(UUID fundBoxId, String email, String collaboratorIdentifier) {
        User inviter = userUtilService.getUserByEmail(email);
        userUtilService.checkUserStatus(inviter);

        FundBox fundBox = fundBoxManager.getFundBoxById(fundBoxId, inviter);

        if (!fundBox.getOwner().getId().equals(inviter.getId()))
            throw new ApiException(ApiErrorCode.FORBIDDEN_COLLABORATOR_ADDITION);

        User invitee = userUtilService.getUserByIdentifier(collaboratorIdentifier);
        userUtilService.checkUserStatus(invitee);

        if (fundBox.getOwner().getId().equals(invitee.getId()))
            throw new ApiException(ApiErrorCode.COLLABORATOR_CANNOT_BE_OWNER);

        userUtilService.checkUserStatus(invitee);

        boolean isAlreadyCollaborator = fundBox.getCollaborators().stream()
                .anyMatch(c -> c.getUser().getId().equals(invitee.getId()));

        if (isAlreadyCollaborator) throw new ApiException(ApiErrorCode.COLLABORATOR_ALREADY_EXISTS);

        boolean alreadyInvited = fundBoxInvitationRepository.existsByFundBoxAndInviteeAndStatus(fundBox, invitee, InvitationStatus.PENDING);
        if (alreadyInvited) throw new ApiException(ApiErrorCode.COLLABORATOR_ALREADY_INVITED);

        FundBoxInvitation invitation = new FundBoxInvitation();
        invitation.setFundBox(fundBox);
        invitation.setInviter(inviter);
        invitation.setInvitee(invitee);
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setInvitationDate(LocalDateTime.now());

        fundBoxInvitationRepository.save(invitation);
    }

    public Page<InviteResponse> getSentInvites(String email, Pageable pageable) {
        User inviter = userUtilService.getUserByEmail(email);
        return fundBoxInvitationRepository.findByInviter(inviter, pageable)
                .map(fundBoxManager::toInviteResponse);
    }

    public void cancelInvitation(UUID invitationId, String email) {
        FundBoxInvitation invitation = fundBoxInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.INVITATION_NOT_FOUND));

        User inviter = userUtilService.getUserByEmail(email);
        if (!invitation.getInviter().getId().equals(inviter.getId()))
            throw new ApiException(ApiErrorCode.FORBIDDEN_ACTION);

        if (invitation.getStatus() != InvitationStatus.PENDING)
            throw new ApiException(ApiErrorCode.INVITATION_CANNOT_BE_CANCELED);

        fundBoxInvitationRepository.delete(invitation);
    }

    public Page<InviteResponse> getUserInvites(String username, Pageable pageable) {
        User user = userUtilService.getUserByEmail(username);
        Page<FundBoxInvitation> invites = fundBoxInvitationRepository.findByInvitee_Id(user.getId(), pageable);
        return invites.map(fundBoxManager::toInviteResponse);
    }

    public void acceptInvitation(UUID invitationId, String email) {
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

    public void declineInvitation(UUID invitationId, String email) {
        FundBoxInvitation invitation = fundBoxManager.validateInvitationForUser(invitationId, email);
        fundBoxInvitationRepository.delete(invitation);
    }

}
