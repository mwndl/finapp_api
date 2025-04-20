package com.finapp.backend.service;

import com.finapp.backend.dto.deposit.DepositResponse;
import com.finapp.backend.dto.fundbox.*;
import com.finapp.backend.dto.deposit.FundBoxInfo;
import com.finapp.backend.exception.ApiErrorCode;
import com.finapp.backend.exception.ApiException;
import com.finapp.backend.model.Deposit;
import com.finapp.backend.model.FundBox;
import com.finapp.backend.model.FundBoxCollaborator;
import com.finapp.backend.model.User;
import com.finapp.backend.model.enums.TransactionType;
import com.finapp.backend.repository.DepositRepository;
import com.finapp.backend.repository.FundBoxRepository;
import com.finapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FundboxService {

    private final FundBoxRepository fundBoxRepository;
    private final UserRepository userRepository;
    private final DepositRepository depositRepository;

    public FundBoxResponse createFundBox(String email, CreateFundBoxRequest request) {
        User user = getUserByEmail(email);
        checkUserStatus(user);

        if (fundBoxExists(user.getId(), request.name())) {
            throw new ApiException(ApiErrorCode.FUND_BOX_NAME_ALREADY_EXISTS);
        }

        FundBox fundBox = buildFundBox(request, user);
        FundBox saved = fundBoxRepository.save(fundBox);

        return buildFundBoxResponse(saved, user);
    }

    public ResponseEntity<Page<FundBoxResponse>> listUserFundBoxes(String email, Pageable pageable) {
        User user = getUserByEmail(email);
        checkUserStatus(user);

        Page<FundBox> fundBoxes = fundBoxRepository.findByOwnerIdOrCollaboratorsContaining(user.getId(), pageable);

        if (fundBoxes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        Page<FundBoxResponse> fundBoxResponses = fundBoxes.map(fb -> buildFundBoxResponse(fb, fb.getOwner()));
        return ResponseEntity.ok(fundBoxResponses);
    }


    public FundBoxDetailsResponse getFundBoxDetails(Long fundBoxId, String email, Pageable pageable) {
        User user = getUserByEmail(email);
        checkUserStatus(user);

        FundBox fundBox = getFundBoxById(fundBoxId, user);
        BigDecimal balance = calculateBalance(fundBoxId);
        Page<DepositResponse> depositResponses = getDepositResponses(fundBoxId, pageable);

        return new FundBoxDetailsResponse(
                fundBox.getId(),
                fundBox.getName(),
                fundBox.getFinancialGoal(),
                fundBox.getTargetDate(),
                new OwnerResponse(fundBox.getOwner().getId(), fundBox.getOwner().getName()),
                balance,
                depositResponses
        );
    }

    public FundBoxResponse updateFundBox(Long fundBoxId, String email, UpdateFundBoxRequest request) {
        User user = getUserByEmail(email);
        checkUserStatus(user);

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
        User user = getUserByEmail(email);
        checkUserStatus(user);

        FundBox fundBox = getFundBoxById(fundBoxId, user);

        depositRepository.findByFundBoxId(fundBoxId).forEach(deposit -> {
            deposit.setFundBox(null);
            depositRepository.save(deposit);
        });

        fundBoxRepository.delete(fundBox);
    }

    public void addCollaborator(Long fundBoxId, String email, Long collaboratorId) {
        User owner = getUserByEmail(email);
        checkUserStatus(owner);

        FundBox fundBox = getFundBoxById(fundBoxId, owner);

        if (fundBox.getOwner().getId().equals(collaboratorId)) {
            throw new ApiException(ApiErrorCode.COLLABORATOR_CANNOT_BE_OWNER);
        }

        User collaborator = getUserById(collaboratorId);
        checkUserStatus(collaborator);

        FundBoxCollaborator relation = new FundBoxCollaborator();
        relation.setFundBox(fundBox);
        relation.setUser(collaborator);

        if (!fundBox.getCollaborators().add(relation)) {
            throw new ApiException(ApiErrorCode.COLLABORATOR_ALREADY_EXISTS);
        }

        fundBoxRepository.save(fundBox);
    }


    public void removeCollaborator(Long fundBoxId, String email, Long collaboratorId) {
        User owner = getUserByEmail(email);
        checkUserStatus(owner);

        FundBox fundBox = getFundBoxById(fundBoxId, owner);

        User collaborator = getUserById(collaboratorId);

        boolean removed = fundBox.getCollaborators().remove(collaborator);
        if (!removed) {
            throw new ApiException(ApiErrorCode.COLLABORATOR_NOT_FOUND);
        }

        fundBoxRepository.save(fundBox);
    }



    // aux methods
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
        return fundBoxRepository.findByIdAndOwnerId(fundBoxId, user.getId())
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

            return new DepositResponse(
                    deposit.getId(),
                    deposit.getTransactionType() == TransactionType.EXIT
                            ? deposit.getAmount().negate()
                            : deposit.getAmount(),
                    deposit.getDate(),
                    deposit.getDescription(),
                    fundBoxInfo,
                    deposit.getTransactionType().toString()
            );
        });
    }
}
