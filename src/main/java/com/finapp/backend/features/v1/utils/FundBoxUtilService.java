package com.finapp.backend.features.v1.utils;

import com.finapp.backend.features.v1.deposit.dto.FundBoxInfo;
import com.finapp.backend.features.v1.fundbox.dto.*;
import com.finapp.backend.features.v1.user.dto.InviteResponse;
import com.finapp.backend.features.v1.user.dto.UserSummary;
import com.finapp.backend.shared.exception.ApiErrorCode;
import com.finapp.backend.shared.exception.ApiException;
import com.finapp.backend.domain.model.Deposit;
import com.finapp.backend.domain.model.FundBox;
import com.finapp.backend.domain.model.FundBoxInvitation;
import com.finapp.backend.domain.model.User;
import com.finapp.backend.domain.model.enums.TransactionType;
import com.finapp.backend.domain.repository.FundBoxInvitationRepository;
import com.finapp.backend.domain.repository.UserRepository;
import com.finapp.backend.domain.repository.DepositRepository;
import com.finapp.backend.domain.repository.FundBoxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FundBoxUtilService {

    private final UserRepository userRepository;
    private final FundBoxRepository fundBoxRepository;
    private final DepositRepository depositRepository;
    private final FundBoxInvitationRepository fundBoxInvitationRepository;
    private final UserUtilService userUtilService;

    public boolean fundBoxExists(UUID userId, String name) {
        return fundBoxRepository.existsByOwnerIdAndName(userId, name.trim());
    }

    public FundBox buildFundBox(CreateFundBoxRequest request, User user) {
        FundBox fundBox = new FundBox();
        fundBox.setName(request.name().trim());
        fundBox.setFinancialGoal(request.financialGoal());
        fundBox.setTargetDate(request.targetDate());
        fundBox.setOwner(user);
        return fundBox;
    }

    public FundBoxResponse buildFundBoxResponse(FundBox fundBox, User user) {
        return new FundBoxResponse(
                fundBox.getId(),
                fundBox.getName(),
                fundBox.getFinancialGoal(),
                fundBox.getTargetDate(),
                new OwnerResponse(user.getId(), user.getName())
        );
    }

    public FundBox getFundBoxById(UUID fundBoxId, User user) {
        return fundBoxRepository.findByIdAndUserIsOwnerOrCollaborator(fundBoxId, user.getId())
                .orElseThrow(() -> new ApiException(ApiErrorCode.FUND_BOX_NOT_FOUND));
    }

    public BigDecimal calculateBalance(UUID fundBoxId) {
        BigDecimal entryTotal = Optional.ofNullable(
                depositRepository.sumByFundBoxIdAndTransactionType(fundBoxId, TransactionType.ENTRY)
        ).orElse(BigDecimal.ZERO);

        BigDecimal exitTotal = Optional.ofNullable(
                depositRepository.sumByFundBoxIdAndTransactionType(fundBoxId, TransactionType.EXIT)
        ).orElse(BigDecimal.ZERO);

        return entryTotal.subtract(exitTotal);
    }

    public Page<FundboxDepositResponse> getDepositResponses(UUID fundBoxId, Pageable pageable) {
        Page<Deposit> depositPage = depositRepository.findByFundBoxId(fundBoxId, pageable);

        return depositPage.map(deposit -> {
            FundBoxInfo fundBoxInfo = new FundBoxInfo(
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

            return new FundboxDepositResponse(
                    deposit.getId(),
                    deposit.getTransactionType() == TransactionType.EXIT
                            ? deposit.getAmount().negate()
                            : deposit.getAmount(),
                    deposit.getDate(),
                    deposit.getDescription(),
                    deposit.getTransactionType().toString(),
                    ownerInfo
            );
        });
    }

    public FundBoxInvitation validateInvitationForUser(UUID invitationId, String email) {
        FundBoxInvitation invitation = fundBoxInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.INVITATION_NOT_FOUND));

        User user = userUtilService.getUserByEmail(email);
        if (!invitation.getInvitee().getId().equals(user.getId())) {
            throw new ApiException(ApiErrorCode.FORBIDDEN_ACTION);
        }

        return invitation;
    }

    public InviteResponse toInviteResponse(FundBoxInvitation invite) {
        InviteResponse response = new InviteResponse();
        response.setInviteId(invite.getId());
        response.setFundBox(new FundBoxSummary(
                invite.getFundBox().getId(),
                invite.getFundBox().getName()
        ));
        response.setInviter(new UserSummary(
                invite.getInviter().getId(),
                invite.getInviter().getUsername(),
                invite.getInviter().getName()

        ));
        response.setInvitee(new UserSummary(
                invite.getInvitee().getId(),
                invite.getInvitee().getUsername(),
                invite.getInvitee().getName()
        ));
        response.setStatus(invite.getStatus().name());
        response.setInvitationDate(invite.getInvitationDate());
        return response;
    }
}
