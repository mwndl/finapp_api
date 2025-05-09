package com.finapp.backend.domain.service;

import com.finapp.backend.domain.event.PasswordChangedEvent;
import com.finapp.backend.domain.model.enums.UserStatus;
import com.finapp.backend.domain.service.utils.UserUtilService;
import com.finapp.backend.dto.user.UserResponse;
import com.finapp.backend.exception.ApiErrorCode;
import com.finapp.backend.exception.ApiException;
import com.finapp.backend.domain.model.User;
import com.finapp.backend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final UserUtilService userUtilService;
    private final SessionService sessionService;

    public UserResponse getUserInfo(String email) {
        User user = userUtilService.getUserByEmail(email);
        userUtilService.checkUserStatus(user);

        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }

    public void updateUserData(String email, String newName) {
        User user = userUtilService.getUserByEmail(email);
        userUtilService.checkUserStatus(user);

        if (newName != null && !newName.trim().isEmpty())
            updateUserName(user, newName);
    }

    public void updatePasswordByEmail(String email, String newPassword) {
        User user = userUtilService.getUserByEmail(email);
        userUtilService.checkUserStatus(user);
        updateUserPassword(user, newPassword);
    }

    public void updateUserPassword(String email, String currentPassword, String newPassword) {
        User user = userUtilService.getUserByEmail(email);
        userUtilService.checkUserStatus(user);
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash()))
            throw new ApiException(ApiErrorCode.INVALID_CREDENTIALS);

        updateUserPassword(user, newPassword);
    }

    public void requestAccountDeletion(String email) {
        User user = userUtilService.getUserByEmail(email);
        userUtilService.checkUserStatus(user);

        user.setStatus(UserStatus.DEACTIVATION_REQUESTED);
        user.setDeletionRequestedAt(LocalDateTime.now());
        userRepository.save(user);

        sessionService.revokeAllUserSessions(user);
    }

    // aux methods
    private void updateUserName(User user, String newName) {
        if (newName.equals(user.getName()))
            throw new ApiException(ApiErrorCode.SAME_NAME);

        if (!newName.matches("^[A-Za-zÀ-ÿ]+\\s+[A-Za-zÀ-ÿ]+(\\s+[A-Za-zÀ-ÿ]+)*$"))
            throw new ApiException(ApiErrorCode.NAME_INVALID);

        user.setName(newName);
        userRepository.save(user);
    }

    private void updateUserPassword(User user, String newPassword) {
        if (!newPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_+\\-=])[A-Za-z\\d@$!%*?&#^()_+\\-=]{8,}$"))
            throw new ApiException(ApiErrorCode.PASSWORD_TOO_WEAK);

        if (passwordEncoder.matches(newPassword, user.getPasswordHash()))
            throw new ApiException(ApiErrorCode.SAME_PASSWORD);

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        eventPublisher.publishEvent(new PasswordChangedEvent(user));
    }
}
