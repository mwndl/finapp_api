package com.finapp.backend.service;

import com.finapp.backend.dto.deposit.DepositResponse;
import com.finapp.backend.dto.fundbox.*;
import com.finapp.backend.dto.deposit.FundBoxInfo;
import com.finapp.backend.dto.user.InviteResponse;
import com.finapp.backend.dto.user.UserSummary;
import com.finapp.backend.exception.ApiErrorCode;
import com.finapp.backend.exception.ApiException;
import com.finapp.backend.model.*;
import com.finapp.backend.model.enums.InvitationStatus;
import com.finapp.backend.model.enums.TransactionType;
import com.finapp.backend.repository.DepositRepository;
import com.finapp.backend.repository.FundBoxInvitationRepository;
import com.finapp.backend.repository.FundBoxRepository;
import com.finapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FundboxService {

    private final FundBoxRepository fundBoxRepository;
    private final UserRepository userRepository;
    private final DepositRepository depositRepository;
    private final FundBoxInvitationRepository fundBoxInvitationRepository;

    public FundBoxResponse createFundBox(String email, CreateFundBoxRequest request) {
        User user = getActiveUserByEmail(email);

        if (fundBoxExists(user.getId(), request.name()))
            throw new ApiException(ApiErrorCode.FUND_BOX_NAME_ALREADY_EXISTS);

        FundBox fundBox = buildFundBox(request, user);
        FundBox saved = fundBoxRepository.save(fundBox);

        return buildFundBoxResponse(saved, user);
    }

    public ResponseEntity<Page<FundBoxResponse>> listUserFundBoxes(String email, Pageable pageable) {
        User user = getActiveUserByEmail(email);

        Page<FundBox> fundBoxes = fundBoxRepository.findByOwnerIdOrCollaboratorsContaining(user.getId(), pageable);

        if (fundBoxes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        Page<FundBoxResponse> fundBoxResponses = fundBoxes.map(fb -> buildFundBoxResponse(fb, fb.getOwner()));
        return ResponseEntity.ok(fundBoxResponses);
    }


    public FundBoxDetailsResponse getFundBoxDetails(Long fundBoxId, String email, Pageable pageable) {
        User user = getActiveUserByEmail(email);

        FundBox fundBox = getFundBoxById(fundBoxId, user);
        BigDecimal balance = calculateBalance(fundBoxId);
        Page<DepositResponse> depositResponses = getDepositResponses(fundBoxId, pageable);

        List<CollaboratorResponse> collaborators = fundBox.getCollaborators().stream()
                .map(collaborator -> new CollaboratorResponse(
                        collaborator.getUser().getId(),
                        collaborator.getUser().getName(),
                        collaborator.getJoinedAt()
                ))
                .collect(Collectors.toList());

        List<InviteResponse> invites = fundBoxInvitationRepository
                .findByFundBox_IdAndStatus(fundBoxId, InvitationStatus.PENDING)
                .stream()
                .map(this::toInviteResponse)
                .collect(Collectors.toList());


        return new FundBoxDetailsResponse(
                fundBox.getId(),
                fundBox.getName(),
                fundBox.getFinancialGoal(),
                fundBox.getTargetDate(),
                new OwnerResponse(fundBox.getOwner().getId(), fundBox.getOwner().getName()),
                balance,
                depositResponses,
                collaborators,
                invites
        );
    }

    public FundBoxResponse updateFundBox(Long fundBoxId, String email, UpdateFundBoxRequest request) {
        User user = getActiveUserByEmail(email);
        FundBox fundBox = getFundBoxById(fundBoxId, user);

        if (request.getName() != null && !request.getName().trim().isEmpty())
            fundBox.setName(request.getName().trim());
        if (request.getFinancialGoal() != null)
            fundBox.setFinancialGoal(request.getFinancialGoal());
        if (request.getTargetDate() != null)
            fundBox.setTargetDate(request.getTargetDate());

        FundBox savedFundBox = fundBoxRepository.save(fundBox);
        return buildFundBoxResponse(savedFundBox, fundBox.getOwner());
    }

    public void deleteFundBox(Long fundBoxId, String email) {
        User user = getActiveUserByEmail(email);
        FundBox fundBox = getFundBoxById(fundBoxId, user);

        depositRepository.findByFundBoxId(fundBoxId).forEach(deposit -> {
            deposit.setFundBox(null);
            depositRepository.save(deposit);
        });

        fundBoxRepository.delete(fundBox);
    }


    // invites
    public void inviteCollaborator(Long fundBoxId, String email, Long collaboratorId) {
        User inviter = getUserByEmail(email);
        checkUserStatus(inviter);

        FundBox fundBox = getFundBoxById(fundBoxId, inviter);

        if (!fundBox.getOwner().getId().equals(inviter.getId()))
            throw new ApiException(ApiErrorCode.FORBIDDEN_COLLABORATOR_ADDITION);

        if (fundBox.getOwner().getId().equals(collaboratorId))
            throw new ApiException(ApiErrorCode.COLLABORATOR_CANNOT_BE_OWNER);

        User invitee = getUserById(collaboratorId);
        checkUserStatus(invitee);

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

    public void cancelInvitation(Long invitationId, String email) {
        FundBoxInvitation invitation = fundBoxInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.INVITATION_NOT_FOUND));

        User inviter = getUserByEmail(email);
        if (!invitation.getInviter().getId().equals(inviter.getId()))
            throw new ApiException(ApiErrorCode.FORBIDDEN_ACTION);

        if (invitation.getStatus() != InvitationStatus.PENDING)
            throw new ApiException(ApiErrorCode.INVITATION_CANNOT_BE_CANCELED);

        fundBoxInvitationRepository.delete(invitation);
    }

    public Page<InviteResponse> getUserInvites(String username, Pageable pageable) {
        User user = getUserByEmail(username);
        Page<FundBoxInvitation> invites = fundBoxInvitationRepository.findByInvitee_Id(user.getId(), pageable);
        return invites.map(this::toInviteResponse);
    }

    private InviteResponse toInviteResponse(FundBoxInvitation invite) {
        InviteResponse response = new InviteResponse();
        response.setInviteId(invite.getId());
        response.setFundBox(new FundBoxSummary(
                invite.getFundBox().getId(),
                invite.getFundBox().getName()
        ));
        response.setInviter(new UserSummary(
                invite.getInviter().getId(),
                invite.getInviter().getName()
        ));
        response.setInvitee(new UserSummary(
                invite.getInvitee().getId(),
                invite.getInvitee().getName()
        ));
        response.setStatus(invite.getStatus().name());
        response.setInvitationDate(invite.getInvitationDate());
        return response;
    }

    public void acceptInvitation(Long invitationId, String email) {
        FundBoxInvitation invitation = validateInvitationForUser(invitationId, email);

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
        FundBoxInvitation invitation = validateInvitationForUser(invitationId, email);
        fundBoxInvitationRepository.delete(invitation);
    }

    public void removeCollaborator(Long fundBoxId, String email, Long collaboratorId) {
        User owner = getUserByEmail(email);
        checkUserStatus(owner);
        FundBox fundBox = getFundBoxById(fundBoxId, owner);
        User collaborator = getUserById(collaboratorId);

        boolean removed = fundBox.getCollaborators().removeIf(c -> c.getUser().getId().equals(collaborator.getId()));
        if (!removed)
            throw new ApiException(ApiErrorCode.COLLABORATOR_NOT_FOUND);

        depositRepository.unsetFundBoxForUserDeposits(collaborator.getId(), fundBoxId);

        fundBoxRepository.save(fundBox);
    }

    public void leaveFundBox(Long fundBoxId, String email) {
        User collaborator = getUserByEmail(email);
        checkUserStatus(collaborator);
        FundBox fundBox = getFundBoxById(fundBoxId, collaborator);

        if (fundBox.getOwner().getId().equals(collaborator.getId()))
            throw new ApiException(ApiErrorCode.CANNOT_LEAVE_AS_OWNER);

        boolean removed = fundBox.getCollaborators().removeIf(c -> c.getUser().getId().equals(collaborator.getId()));
        if (!removed)
            throw new ApiException(ApiErrorCode.COLLABORATOR_NOT_FOUND);

        depositRepository.unsetFundBoxForUserDeposits(collaborator.getId(), fundBoxId);

        fundBoxRepository.save(fundBox);
    }


    // aux methods
    private User getActiveUserByEmail(String email) {
        User user = getUserByEmail(email);
        checkUserStatus(user);
        return user;
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND));
    }

    private void checkUserStatus(User user) {
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new ApiException(ApiErrorCode.ACCOUNT_DEACTIVATED);
        }
    }

    private boolean fundBoxExists(Long userId, String name) {
        return fundBoxRepository.existsByOwnerIdAndName(userId, name.trim());
    }

    private FundBox buildFundBox(CreateFundBoxRequest request, User user) {
        FundBox fundBox = new FundBox();
        fundBox.setName(request.name().trim());
        fundBox.setFinancialGoal(request.financialGoal());
        fundBox.setTargetDate(request.targetDate());
        fundBox.setOwner(user);
        return fundBox;
    }

    private FundBoxResponse buildFundBoxResponse(FundBox fundBox, User user) {
        return new FundBoxResponse(
                fundBox.getId(),
                fundBox.getName(),
                fundBox.getFinancialGoal(),
                fundBox.getTargetDate(),
                new OwnerResponse(user.getId(), user.getName())
        );
    }

    private FundBox getFundBoxById(Long fundBoxId, User user) {
        return fundBoxRepository.findByIdAndUserIsOwnerOrCollaborator(fundBoxId, user.getId())
                .orElseThrow(() -> new ApiException(ApiErrorCode.FUND_BOX_NOT_FOUND));
    }


    private BigDecimal calculateBalance(Long fundBoxId) {
        BigDecimal entryTotal = Optional.ofNullable(
                depositRepository.sumByFundBoxIdAndTransactionType(fundBoxId, TransactionType.ENTRY)
        ).orElse(BigDecimal.ZERO);

        BigDecimal exitTotal = Optional.ofNullable(
                depositRepository.sumByFundBoxIdAndTransactionType(fundBoxId, TransactionType.EXIT)
        ).orElse(BigDecimal.ZERO);

        return entryTotal.subtract(exitTotal);
    }

    private Page<DepositResponse> getDepositResponses(Long fundBoxId, Pageable pageable) {
        Page<Deposit> depositPage = depositRepository.findByFundBoxId(fundBoxId, pageable);

        return depositPage.map(deposit -> {
            com.finapp.backend.dto.deposit.FundBoxInfo fundBoxInfo = new FundBoxInfo(
                    deposit.getFundBox().getId(),
                    deposit.getFundBox().getName()
            );

            OwnerResponse ownerInfo = null;
            if (deposit.getUser() != null) {
                ownerInfo = new OwnerResponse(
                        deposit.getUser().getId(),
                        deposit.getUser().getName()
                );
            }

            return new DepositResponse(
                    deposit.getId(),
                    deposit.getTransactionType() == TransactionType.EXIT
                            ? deposit.getAmount().negate()
                            : deposit.getAmount(),
                    deposit.getDate(),
                    deposit.getDescription(),
                    fundBoxInfo,
                    deposit.getTransactionType().toString(),
                    ownerInfo
            );
        });
    }

    private FundBoxInvitation validateInvitationForUser(Long invitationId, String email) {
        FundBoxInvitation invitation = fundBoxInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.INVITATION_NOT_FOUND));

        User user = getUserByEmail(email);
        if (!invitation.getInvitee().getId().equals(user.getId())) {
            throw new ApiException(ApiErrorCode.FORBIDDEN_ACTION);
        }

        return invitation;
    }


}
