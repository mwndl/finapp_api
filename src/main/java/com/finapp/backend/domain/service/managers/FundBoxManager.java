package com.finapp.backend.domain.service.managers;

import com.finapp.backend.dto.deposit.DepositResponse;
import com.finapp.backend.dto.deposit.FundBoxInfo;
import com.finapp.backend.dto.fundbox.CreateFundBoxRequest;
import com.finapp.backend.dto.fundbox.FundBoxResponse;
import com.finapp.backend.dto.fundbox.FundBoxSummary;
import com.finapp.backend.dto.fundbox.OwnerResponse;
import com.finapp.backend.dto.user.InviteResponse;
import com.finapp.backend.dto.user.UserSummary;
import com.finapp.backend.exception.ApiErrorCode;
import com.finapp.backend.exception.ApiException;
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

@Service
@RequiredArgsConstructor
public class FundBoxManager {

    private final UserRepository userRepository;
    private final FundBoxRepository fundBoxRepository;
    private final DepositRepository depositRepository;
    private final FundBoxInvitationRepository fundBoxInvitationRepository;

    public User getActiveUserByEmail(String email) {
        User user = getUserByEmail(email);
        checkUserStatus(user);
        return user;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND));
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND));
    }

    public void checkUserStatus(User user) {
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new ApiException(ApiErrorCode.ACCOUNT_DEACTIVATED);
        }
    }

    public boolean fundBoxExists(Long userId, String name) {
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

    public FundBox getFundBoxById(Long fundBoxId, User user) {
        return fundBoxRepository.findByIdAndUserIsOwnerOrCollaborator(fundBoxId, user.getId())
                .orElseThrow(() -> new ApiException(ApiErrorCode.FUND_BOX_NOT_FOUND));
    }

    public BigDecimal calculateBalance(Long fundBoxId) {
        BigDecimal entryTotal = Optional.ofNullable(
                depositRepository.sumByFundBoxIdAndTransactionType(fundBoxId, TransactionType.ENTRY)
        ).orElse(BigDecimal.ZERO);

        BigDecimal exitTotal = Optional.ofNullable(
                depositRepository.sumByFundBoxIdAndTransactionType(fundBoxId, TransactionType.EXIT)
        ).orElse(BigDecimal.ZERO);

        return entryTotal.subtract(exitTotal);
    }

    public Page<DepositResponse> getDepositResponses(Long fundBoxId, Pageable pageable) {
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

    public FundBoxInvitation validateInvitationForUser(Long invitationId, String email) {
        FundBoxInvitation invitation = fundBoxInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.INVITATION_NOT_FOUND));

        User user = getUserByEmail(email);
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
}
