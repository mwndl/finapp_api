package com.finapp.backend.repository;

import com.finapp.backend.model.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserTokenRepository extends JpaRepository<UserToken, Long> {

    Optional<UserToken> findByRefreshTokenAndRevokedTrue(String refreshToken);

    List<UserToken> findByRevokedFalse();

    Optional<UserToken> findByRefreshTokenAndRevokedFalse(String refreshToken);

    Optional<UserToken> findByAccessTokenAndRevokedFalse(String accessToken);

}