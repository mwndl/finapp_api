package com.finapp.backend.service;

import com.finapp.backend.dto.fundbox.CreateFundBoxRequest;
import com.finapp.backend.dto.fundbox.FundBoxResponse;
import com.finapp.backend.dto.fundbox.OwnerResponse;
import com.finapp.backend.exception.ApiErrorCode;
import com.finapp.backend.exception.ApiException;
import com.finapp.backend.model.FundBox;
import com.finapp.backend.model.User;
import com.finapp.backend.repository.FundBoxRepository;
import com.finapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FundboxService {

    private final FundBoxRepository fundBoxRepository;
    private final UserRepository userRepository;

    public FundBoxResponse createFundBox(String email, CreateFundBoxRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new ApiException(ApiErrorCode.ACCOUNT_DEACTIVATED);
        }

        boolean exists = fundBoxRepository.existsByOwnerIdAndName(user.getId(), request.name().trim());
        if (exists) {
            throw new ApiException(ApiErrorCode.FUND_BOX_NAME_ALREADY_EXISTS);
        }

        FundBox fundBox = new FundBox();
        fundBox.setName(request.name().trim());
        fundBox.setFinancialGoal(request.financialGoal());
        fundBox.setTargetDate(request.targetDate());
        fundBox.setOwner(user);

        FundBox saved = fundBoxRepository.save(fundBox);

        return new FundBoxResponse(
                saved.getId(),
                saved.getName(),
                saved.getFinancialGoal(),
                saved.getTargetDate(),
                new OwnerResponse(user.getId(), user.getName())
        );
    }

    public Page<FundBoxResponse> listUserFundBoxes(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND));

        Page<FundBox> fundBoxes = fundBoxRepository.findByOwnerId(user.getId(), pageable);

        return fundBoxes.map(fb -> new FundBoxResponse(
                fb.getId(),
                fb.getName(),
                fb.getFinancialGoal(),
                fb.getTargetDate(),
                new OwnerResponse(user.getId(), user.getName())
        ));
    }

    public FundBoxResponse getFundBoxById(Long fundBoxId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND));

        FundBox fundBox = fundBoxRepository.findById(fundBoxId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.FUND_BOX_NOT_FOUND));

        if (!fundBox.getOwner().getId().equals(user.getId())) {
            throw new ApiException(ApiErrorCode.UNAUTHORIZED_ACCESS);
        }

        return new FundBoxResponse(
                fundBox.getId(),
                fundBox.getName(),
                fundBox.getFinancialGoal(),
                fundBox.getTargetDate(),
                new OwnerResponse(fundBox.getOwner().getId(), fundBox.getOwner().getName())
        );
    }



}
