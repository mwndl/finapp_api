package com.finapp.backend.domain.repository;

import com.finapp.backend.domain.model.User;
import com.finapp.backend.domain.model.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserTokenRepository extends JpaRepository<UserToken, UUID> {

    List<UserToken> findAllByUserAndRevokedFalse(User user);

    Optional<UserToken> findByRefreshTokenAndRevokedTrue(String refreshToken);

    List<UserToken> findByRevokedFalse();

    Optional<UserToken> findByRefreshTokenAndRevokedFalse(String refreshToken);

    Optional<UserToken> findByAccessTokenAndRevokedFalse(String accessToken);

    Optional<UserToken> findByIdAndRevokedFalse(UUID sessionId);

}