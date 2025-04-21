package com.finapp.backend.service;

import com.finapp.backend.dto.user.UpdateUserRequest;
import com.finapp.backend.dto.user.UserResponse;
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

    public UserResponse getUserInfo(String email) {
        User user = getUserByEmail(email);
        ensureAccountIsActive(user);

        return new UserResponse(user.getName(), user.getEmail());
    }


    public void updateUser(String email, UpdateUserRequest request) {
        User user = getUserByEmail(email);
        ensureAccountIsActive(user);

        boolean changed = false;

        if (request.getName() != null && !request.getName().equals(user.getName())) {
            user.setName(request.getName());
            changed = true;
        } else if (request.getName() != null)
            throw new ApiException(ApiErrorCode.SAME_NAME);

        if (request.getPassword() != null && !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            changed = true;
        } else if (request.getPassword() != null)
            throw new ApiException(ApiErrorCode.SAME_PASSWORD);

        if (changed)
            userRepository.save(user);
    }

    public void requestAccountDeletion(String email) {
        User user = getUserByEmail(email);
        ensureAccountIsActive(user);

        user.setActive(false);
        user.setDeletionRequestedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // aux methods
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND));
    }

    private User getActiveUserByEmail(String email) {
        User user = getUserByEmail(email);
        ensureAccountIsActive(user);
        return user;
    }

    private void ensureAccountIsActive(User user) {
        if (!user.getActive()) {
            throw new ApiException(ApiErrorCode.ACCOUNT_DEACTIVATED);
        }
    }
}
