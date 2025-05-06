package com.finapp.backend.features.v2.fundbox.service;

import com.finapp.backend.domain.model.Deposit;
import com.finapp.backend.domain.model.FundBox;
import com.finapp.backend.domain.model.User;
import com.finapp.backend.domain.model.enums.InvitationStatus;
import com.finapp.backend.domain.model.enums.TransactionType;
import com.finapp.backend.domain.repository.FundBoxInvitationRepository;
import com.finapp.backend.features.v1.utils.FundBoxUtilService;
import com.finapp.backend.features.v1.utils.UserUtilService;
import com.finapp.backend.features.v1.fundbox.dto.FundboxDepositResponse;
import com.finapp.backend.features.v1.user.dto.InviteResponse;
import com.finapp.backend.features.v2.fundbox.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FundboxServiceV2 {

    private final FundBoxInvitationRepository fundBoxInvitationRepository;
    private final FundBoxUtilService fundBoxManager;
    private final UserUtilService userUtilService;

    public FundBoxDetailsResponseV2 getFundBoxDetailsV2(UUID fundBoxId, String email, Pageable pageable) {
        User user = userUtilService.getActiveUserByEmail(email);

        FundBox fundBox = fundBoxManager.getFundBoxById(fundBoxId, user);
        Page<FundboxDepositResponse> depositResponses = fundBoxManager.getDepositResponses(fundBoxId, pageable);

        List<Deposit> deposits = fundBox.getDeposits();
        BigDecimal totalEntries = BigDecimal.ZERO;
        BigDecimal totalExits = BigDecimal.ZERO;

        for (Deposit deposit : deposits) {
            if (deposit.getTransactionType() == TransactionType.ENTRY)
                totalEntries = totalEntries.add(deposit.getAmount());
            else if (deposit.getTransactionType() == TransactionType.EXIT)
                totalExits = totalExits.add(deposit.getAmount());
        }

        BigDecimal netBalance = totalEntries.subtract(totalExits);

        Map<User, BigDecimal> totalContributionsPerUser = new HashMap<>();
        Map<User, BigDecimal> totalEntriesPerUser = new HashMap<>();
        Map<User, BigDecimal> totalExitsPerUser = new HashMap<>();
        Map<User, Integer> totalDepositsPerUser = new HashMap<>();

        for (Deposit deposit : deposits) {
            User userInDeposit = deposit.getUser();

            totalContributionsPerUser.put(userInDeposit,
                    totalContributionsPerUser.getOrDefault(userInDeposit, BigDecimal.ZERO).add(deposit.getAmount()));

            if (deposit.getTransactionType() == TransactionType.ENTRY) {
                totalEntriesPerUser.put(userInDeposit,
                        totalEntriesPerUser.getOrDefault(userInDeposit, BigDecimal.ZERO).add(deposit.getAmount()));
            }

            if (deposit.getTransactionType() == TransactionType.EXIT) {
                totalExitsPerUser.put(userInDeposit,
                        totalExitsPerUser.getOrDefault(userInDeposit, BigDecimal.ZERO).add(deposit.getAmount()));
            }

            totalDepositsPerUser.put(userInDeposit,
                    totalDepositsPerUser.getOrDefault(userInDeposit, 0) + 1);
        }

        List<CollaboratorResponseV2> collaborators = fundBox.getCollaborators().stream()
                .map(collaborator -> {
                    BigDecimal totalAmount = totalContributionsPerUser.getOrDefault(collaborator.getUser(), BigDecimal.ZERO);
                    BigDecimal totalUserEntries = totalEntriesPerUser.getOrDefault(collaborator.getUser(), BigDecimal.ZERO);
                    BigDecimal totalUserExits = totalExitsPerUser.getOrDefault(collaborator.getUser(), BigDecimal.ZERO);
                    int totalDeposits = totalDepositsPerUser.getOrDefault(collaborator.getUser(), 0);

                    UserMetrics userMetrics = new UserMetrics(
                            totalAmount,
                            totalUserEntries,
                            totalUserExits,
                            totalDeposits
                    );

                    return new CollaboratorResponseV2(
                            collaborator.getUser().getId(),
                            collaborator.getUser().getName(),
                            collaborator.getUser().getUsername(),
                            collaborator.getJoinedAt(),
                            userMetrics
                    );
                })
                .collect(Collectors.toList());

        List<InviteResponse> invites = fundBoxInvitationRepository
                .findByFundBox_IdAndStatus(fundBoxId, InvitationStatus.PENDING)
                .stream()
                .map(fundBoxManager::toInviteResponse)
                .collect(Collectors.toList());

        Metrics metrics = new Metrics(
                deposits.size(),
                totalEntries,
                totalExits,
                netBalance,
                fundBox.getFinancialGoal(),
                fundBox.getTargetDate()
        );

        User ownerUser = fundBox.getOwner();

        BigDecimal totalAmount = totalContributionsPerUser.getOrDefault(ownerUser, BigDecimal.ZERO);
        BigDecimal totalUserEntries = totalEntriesPerUser.getOrDefault(ownerUser, BigDecimal.ZERO);
        BigDecimal totalUserExits = totalExitsPerUser.getOrDefault(ownerUser, BigDecimal.ZERO);
        int totalDeposits = totalDepositsPerUser.getOrDefault(ownerUser, 0);

        UserMetrics ownerMetrics = new UserMetrics(
                totalAmount,
                totalUserEntries,
                totalUserExits,
                totalDeposits
        );

        OwnerResponseV2 owner = new OwnerResponseV2(
                ownerUser.getId(),
                ownerUser.getName(),
                ownerUser.getUsername(),
                ownerMetrics
        );

        Collaboration collaboration = new Collaboration(
                owner,
                collaborators,
                invites
        );

        return new FundBoxDetailsResponseV2(
                fundBox.getId(),
                fundBox.getName(),
                metrics,
                collaboration,
                depositResponses.getContent()
        );
    }
}
