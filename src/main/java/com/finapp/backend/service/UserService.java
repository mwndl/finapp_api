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
    private final AuthService authService;

    public UserResponse getUserInfo(String email) {
        User user = getUserByEmail(email);
        ensureAccountIsActive(user);

        return new UserResponse(user.getName(), user.getEmail());
    }

    public void updateUserData(String email, String newName) {
        User user = getUserByEmail(email);
        ensureAccountIsActive(user);

        if (newName != null && !newName.trim().isEmpty())
            updateUserName(user, newName);
    }

    public void updateUserPassword(String email, String newPassword, String accessToken) {
        User user = getUserByEmail(email);
        ensureAccountIsActive(user);
        updateUserPassword(user, newPassword, accessToken);
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
        if (!user.getActive())
            throw new ApiException(ApiErrorCode.ACCOUNT_DEACTIVATED);
    }

    private void updateUserName(User user, String newName) {
        if (newName.equals(user.getName()))
            throw new ApiException(ApiErrorCode.SAME_NAME);

        if (!newName.matches("^[A-Za-zÀ-ÿ]+\\s+[A-Za-zÀ-ÿ]+(\\s+[A-Za-zÀ-ÿ]+)*$"))
            throw new ApiException(ApiErrorCode.NAME_INVALID);

        user.setName(newName);
        userRepository.save(user);
    }

    private void updateUserPassword(User user, String newPassword, String accessToken) {
        if (!newPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_+\\-=])[A-Za-z\\d@$!%*?&#^()_+\\-=]{8,}$"))
            throw new ApiException(ApiErrorCode.PASSWORD_TOO_WEAK);

        if (passwordEncoder.matches(newPassword, user.getPasswordHash()))
            throw new ApiException(ApiErrorCode.SAME_PASSWORD);

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        authService.revokeUserSession(accessToken);
    }
}
