package com.finapp.backend.features.v1.fundbox.service;

import com.finapp.backend.domain.model.FundBox;
import com.finapp.backend.domain.model.User;
import com.finapp.backend.features.v1.fundbox.dto.*;
import com.finapp.backend.features.v1.utils.UserUtilService;
import com.finapp.backend.features.v1.user.dto.InviteResponse;
import com.finapp.backend.shared.exception.ApiErrorCode;
import com.finapp.backend.shared.exception.ApiException;
import com.finapp.backend.domain.enums.InvitationStatus;
import com.finapp.backend.features.v1.deposit.repository.DepositRepository;
import com.finapp.backend.features.v1.fundbox.repository.FundBoxInvitationRepository;
import com.finapp.backend.features.v1.fundbox.repository.FundBoxRepository;
import com.finapp.backend.features.v1.utils.FundBoxUtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FundboxService {

    private final FundBoxRepository fundBoxRepository;
    private final DepositRepository depositRepository;
    private final FundBoxInvitationRepository fundBoxInvitationRepository;
    private final FundBoxUtilService fundBoxManager;
    private final UserUtilService userUtilService;

    public FundBoxResponse createFundBox(String email, CreateFundBoxRequest request) {
        User user = userUtilService.getActiveUserByEmail(email);

        if (fundBoxManager.fundBoxExists(user.getId(), request.name()))
            throw new ApiException(ApiErrorCode.FUND_BOX_NAME_ALREADY_EXISTS);

        FundBox fundBox = fundBoxManager.buildFundBox(request, user);
        FundBox saved = fundBoxRepository.save(fundBox);

        return fundBoxManager.buildFundBoxResponse(saved, user);
    }

    public ResponseEntity<Page<FundBoxResponse>> listUserFundBoxes(String email, Pageable pageable) {
        User user = userUtilService.getActiveUserByEmail(email);

        Page<FundBox> fundBoxes = fundBoxRepository.findByOwnerIdOrCollaboratorsContaining(user.getId(), pageable);

        if (fundBoxes.isEmpty())
            return ResponseEntity.noContent().build();

        Page<FundBoxResponse> fundBoxResponses = fundBoxes.map(fb -> fundBoxManager.buildFundBoxResponse(fb, fb.getOwner()));
        return ResponseEntity.ok(fundBoxResponses);
    }

    public FundBoxDetailsResponse getFundBoxDetails(UUID fundBoxId, String email, Pageable pageable) {
        User user = userUtilService.getActiveUserByEmail(email);

        FundBox fundBox = fundBoxManager.getFundBoxById(fundBoxId, user);
        BigDecimal balance = fundBoxManager.calculateBalance(fundBoxId);
        Page<FundboxDepositResponse> depositResponses = fundBoxManager.getDepositResponses(fundBoxId, pageable);

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
                .map(fundBoxManager::toInviteResponse)
                .collect(Collectors.toList());


        return new FundBoxDetailsResponse(
                fundBox.getId(),
                fundBox.getName(),
                fundBox.getFinancialGoal(),
                fundBox.getTargetDate(),
                balance,
                new OwnerResponse(fundBox.getOwner().getId(), fundBox.getOwner().getName()),
                collaborators,
                invites,
                depositResponses
        );
    }

    public FundBoxResponse updateFundBox(UUID fundBoxId, String email, UpdateFundBoxRequest request) {
        User user = userUtilService.getActiveUserByEmail(email);
        FundBox fundBox = fundBoxManager.getFundBoxById(fundBoxId, user);

        if (request.getName() != null && !request.getName().trim().isEmpty())
            fundBox.setName(request.getName().trim());
        if (request.getFinancialGoal() != null)
            fundBox.setFinancialGoal(request.getFinancialGoal());
        if (request.getTargetDate() != null)
            fundBox.setTargetDate(request.getTargetDate());

        FundBox savedFundBox = fundBoxRepository.save(fundBox);
        return fundBoxManager.buildFundBoxResponse(savedFundBox, fundBox.getOwner());
    }

    public void deleteFundBox(UUID fundBoxId, String email) {
        User user = userUtilService.getActiveUserByEmail(email);
        FundBox fundBox = fundBoxManager.getFundBoxById(fundBoxId, user);

        depositRepository.findByFundBoxId(fundBoxId).forEach(deposit -> {
            deposit.setFundBox(null);
            depositRepository.save(deposit);
        });

        fundBoxRepository.delete(fundBox);
    }

    public void removeCollaborator(UUID fundBoxId, String email, UUID collaboratorId) {
        User owner = userUtilService.getUserByEmail(email);
        userUtilService.checkUserStatus(owner);
        FundBox fundBox = fundBoxManager.getFundBoxById(fundBoxId, owner);
        User collaborator = userUtilService.getUserById(collaboratorId);

        boolean removed = fundBox.getCollaborators().removeIf(c -> c.getUser().getId().equals(collaborator.getId()));
        if (!removed)
            throw new ApiException(ApiErrorCode.COLLABORATOR_NOT_FOUND);

        depositRepository.unsetFundBoxForUserDeposits(collaborator.getId(), fundBoxId);

        fundBoxRepository.save(fundBox);
    }

    public void leaveFundBox(UUID fundBoxId, String email) {
        User collaborator = userUtilService.getUserByEmail(email);
        userUtilService.checkUserStatus(collaborator);
        FundBox fundBox = fundBoxManager.getFundBoxById(fundBoxId, collaborator);

        if (fundBox.getOwner().getId().equals(collaborator.getId()))
            throw new ApiException(ApiErrorCode.CANNOT_LEAVE_AS_OWNER);

        boolean removed = fundBox.getCollaborators().removeIf(c -> c.getUser().getId().equals(collaborator.getId()));
        if (!removed)
            throw new ApiException(ApiErrorCode.COLLABORATOR_NOT_FOUND);

        depositRepository.unsetFundBoxForUserDeposits(collaborator.getId(), fundBoxId);

        fundBoxRepository.save(fundBox);
    }
}
