package com.finapp.backend.task;

import com.finapp.backend.repository.UserTokenRepository;
import com.finapp.backend.model.UserToken;
import com.finapp.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupTask {

    private final UserTokenRepository userTokenRepository;
    private final JwtUtil jwtUtil;

    @Scheduled(cron = "0 0 1 * * *") // 1 am
    public void deleteExpiredOrNonRevokedTokens() {
        List<UserToken> userTokens = userTokenRepository.findByRevokedFalse();

        int deleted = 0;

        for (UserToken userToken : userTokens) {
            if (jwtUtil.isTokenExpired(userToken.getRefreshToken())) {
                userTokenRepository.delete(userToken);
                deleted++;
                log.info("Deleted expired token for user ID: {}", userToken.getUser().getId());
            }
        }

        if (deleted > 0) {
            log.info("Deleted {} expired or non-revoked tokens", deleted);
        } else {
            log.info("No tokens to delete.");
        }
    }
}
