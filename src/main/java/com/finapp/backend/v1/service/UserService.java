package com.finapp.backend.v1.service;

import com.finapp.backend.domain.event.PasswordChangedEvent;
import com.finapp.backend.domain.model.enums.UserStatus;
import com.finapp.backend.v1.service.utils.UserUtilService;
import com.finapp.backend.v1.dto.user.UserResponse;
import com.finapp.backend.v1.dto.user.UserSearchResult;
import com.finapp.backend.exception.ApiErrorCode;
import com.finapp.backend.exception.ApiException;
import com.finapp.backend.domain.model.User;
import com.finapp.backend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final UserUtilService userUtilService;
    private final SessionService sessionService;
    private final ValidationService validationService;

    public UserResponse getUserInfo(String email) {
        User user = userUtilService.getUserByEmail(email);
        userUtilService.checkUserStatus(user);

        return new UserResponse(user.getId(), user.getUsername(), user.getName(), user.getEmail());
    }

    public List<UserSearchResult> searchUsersByUsername(String identifier) {
        Set<UUID> addedIds = new HashSet<>();
        List<UserSearchResult> results = new ArrayList<>();

        // search for exact username
        try {
            User exactMatch = userUtilService.getUserByUsername(identifier);
            results.add(new UserSearchResult(exactMatch.getId(), exactMatch.getUsername(), exactMatch.getName(), 1.0));
            addedIds.add(exactMatch.getId());
        } catch (ApiException e) {
            // ignore if not found (not an error)
        }

        // partial search by username
        List<User> partialMatches = userRepository
                .searchByUsername(identifier.toLowerCase(), PageRequest.of(0, 10));

        for (User user : partialMatches) {
            if (addedIds.contains(user.getId())) continue;

            double score = computeUsernameConfidence(identifier.toLowerCase(), user.getUsername().toLowerCase());
            results.add(new UserSearchResult(user.getId(), user.getUsername(), user.getName(), score));
        }

        return results.stream()
                .sorted(Comparator.comparingDouble(UserSearchResult::getConfidence).reversed())
                .limit(5)
                .toList();
    }

    private double computeUsernameConfidence(String input, String username) {
        if (username.equals(input)) return 1.0;
        if (username.startsWith(input)) return 0.9;
        if (username.contains(input)) return 0.75;
        return 0.5;
    }

    public void updateUserData(String email, String newName, String newUsername) {
        User user = userUtilService.getUserByEmail(email);
        userUtilService.checkUserStatus(user);

        if (newName != null && !newName.trim().isEmpty())
            updateName(user, newName);
        if (newUsername != null && !newUsername.trim().isEmpty())
            updateUsername(user, newUsername);
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
    private void updateName(User user, String newName) {

        if (newName.equals(user.getName()))
            throw new ApiException(ApiErrorCode.SAME_NAME);

        validationService.validateName(newName);

        user.setName(newName);
        userRepository.save(user);
    }


    private void updateUsername(User user, String newUsername) {
        String normalized = newUsername.trim().toLowerCase();
        if (normalized.equals(user.getUsername()))
            throw new ApiException(ApiErrorCode.SAME_USERNAME);

        validationService.validateUsername(normalized);

        user.setUsername(normalized);
        userRepository.save(user);
    }

    private void updateUserPassword(User user, String newPassword) {
        if (passwordEncoder.matches(newPassword, user.getPasswordHash()))
            throw new ApiException(ApiErrorCode.SAME_PASSWORD);

        validationService.validatePassword(newPassword);

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        eventPublisher.publishEvent(new PasswordChangedEvent(user));
    }
}
