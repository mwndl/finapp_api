package com.finapp.backend.domain.service.utils;

import com.finapp.backend.domain.model.User;
import com.finapp.backend.domain.model.enums.UserStatus;
import com.finapp.backend.domain.repository.UserRepository;
import com.finapp.backend.exception.ApiErrorCode;
import com.finapp.backend.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserUtilService {

    private final UserRepository userRepository;

    public User getActiveUserByEmail(String email) {
        User user = getUserByEmail(email);
        checkUserStatus(user);
        return user;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND));
    }

    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND));
    }

    public void checkUserStatus(User user) {
        if (user.getStatus() == UserStatus.PENDING_VERIFICATION)
            throw new ApiException(ApiErrorCode.ACCOUNT_NOT_VERIFIED);
        if (user.getStatus() == UserStatus.DEACTIVATION_REQUESTED)
            throw new ApiException(ApiErrorCode.ACCOUNT_DEACTIVATED);
        if (user.getStatus() == UserStatus.LOCKED)
            throw new ApiException(ApiErrorCode.ACCOUNT_LOCKED);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND));
    }

}
