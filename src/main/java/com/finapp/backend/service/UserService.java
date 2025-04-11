package com.finapp.backend.service;

import com.finapp.backend.dto.user.UpdateUserRequest;
import com.finapp.backend.exception.ApiErrorCode;
import com.finapp.backend.exception.ApiException;
import com.finapp.backend.model.User;
import com.finapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void updateUser(String email, UpdateUserRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND));

        if (!user.getActive()) {
            throw new ApiException(ApiErrorCode.ACCOUNT_DEACTIVATED);
        }

        if (user.getName().equals(request.getName())) {
            throw new ApiException(ApiErrorCode.SAME_NAME);
        }

        if (passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ApiException(ApiErrorCode.SAME_PASSWORD);
        }

        user.setName(request.getName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }

    public void requestAccountDeletion(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND));

        if (!user.getActive()) {
            throw new ApiException(ApiErrorCode.ACCOUNT_DEACTIVATED);
        }

        user.setActive(false);
        user.setDeletionRequestedAt(LocalDateTime.now());
        userRepository.save(user);
    }
}
