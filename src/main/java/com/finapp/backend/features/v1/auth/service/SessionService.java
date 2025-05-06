package com.finapp.backend.features.v1.auth.service;

import com.finapp.backend.domain.model.User;
import com.finapp.backend.domain.model.UserToken;
import com.finapp.backend.domain.repository.UserTokenRepository;
import com.finapp.backend.features.v1.utils.UserUtilService;
import com.finapp.backend.features.v1.auth.dto.SessionInfo;
import com.finapp.backend.shared.exception.ApiErrorCode;
import com.finapp.backend.shared.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final UserTokenRepository userTokenRepository;
    private final UserUtilService userUtilService;

    public List<SessionInfo> getActiveSessions(String email, String currentAccessToken) {
        User user = userUtilService.getActiveUserByEmail(email);
        List<UserToken> tokens = userTokenRepository.findAllByUserAndRevokedFalse(user);
        return tokens.stream()
                .map(token -> new SessionInfo(
                        token.getId().toString(),
                        token.getCreatedAt().toString(),
                        token.getDeviceIp(),
                        token.getDeviceInfo(),
                        token.getAccessToken().equals(currentAccessToken)
                ))
                .toList();
    }

    public void revokeCurrentSession(String accessToken) {
        UserToken userToken = userTokenRepository.findByAccessTokenAndRevokedFalse(accessToken)
                .orElseThrow(() -> new ApiException(ApiErrorCode.INVALID_ACCESS_TOKEN));

        userToken.setRevoked(true);
        userToken.setUpdatedAt(new Date());

        userTokenRepository.save(userToken);
    }

    public void revokeSpecificSession(UUID sessionId, String email, String currentAccessToken) {
        User user = userUtilService.getActiveUserByEmail(email);
        UserToken userSession = getActiveSessionById(sessionId);

        if (!userSession.getUser().equals(user))
            throw new ApiException(ApiErrorCode.FORBIDDEN_ACTION);

        if (userSession.getAccessToken().equals(currentAccessToken))
            throw new ApiException(ApiErrorCode.CANNOT_REVOKE_OWN_SESSION);

        userSession.setRevoked(true);
        userSession.setUpdatedAt(new Date());

        userTokenRepository.save(userSession);
    }

    public UserToken getActiveSessionById(UUID sessionId) {
        return userTokenRepository.findByIdAndRevokedFalse(sessionId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.SESSION_NOT_FOUND));
    }

    public void logoutAllSessions(String email,String currentAccessToken) {
        User user = userUtilService.getActiveUserByEmail(email);
        List<UserToken> activeTokens = userTokenRepository.findAllByUserAndRevokedFalse(user);

        for (UserToken token : activeTokens) {
            if (!token.getAccessToken().equals(currentAccessToken)) {
                token.setRevoked(true);
                token.setUpdatedAt(new Date());
            }
        }

        userTokenRepository.saveAll(activeTokens);
    }

    public void revokeAllUserSessions(User user) {
        List<UserToken> activeTokens = userTokenRepository.findAllByUserAndRevokedFalse(user);

        for (UserToken token : activeTokens) {
            token.setRevoked(true);
            token.setUpdatedAt(new Date());
        }

        userTokenRepository.saveAll(activeTokens);
    }
}
