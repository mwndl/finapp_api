package com.finapp.backend.domain.service;

import com.finapp.backend.domain.model.User;
import com.finapp.backend.domain.model.UserToken;
import com.finapp.backend.domain.repository.UserTokenRepository;
import com.finapp.backend.domain.service.utils.UserUtilService;
import com.finapp.backend.dto.auth.SessionInfo;
import com.finapp.backend.exception.ApiErrorCode;
import com.finapp.backend.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

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
                        !token.isRevoked(),
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

    public void revokeSpecificSession(Long sessionId, String email, String currentAccessToken) {
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

    public UserToken getActiveSessionById(Long sessionId) {
        return userTokenRepository.findByIdAndRevokedFalse(sessionId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.SESSION_NOT_FOUND));
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
