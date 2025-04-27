package com.finapp.backend.domain.service;

import com.finapp.backend.dto.auth.AuthResponse;
import com.finapp.backend.dto.auth.LoginRequest;
import com.finapp.backend.dto.auth.RegisterRequest;
import com.finapp.backend.exception.ApiException;
import com.finapp.backend.exception.ApiErrorCode;
import com.finapp.backend.domain.model.User;
import com.finapp.backend.domain.model.UserToken;
import com.finapp.backend.domain.repository.UserRepository;
import com.finapp.backend.domain.repository.UserTokenRepository;
import com.finapp.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserTokenRepository userTokenRepository;
    private final UserDetailsService userDetailsService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent())
            throw new ApiException(ApiErrorCode.EMAIL_ALREADY_REGISTERED);

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);
        userRepository.save(user);

        return generateAndPersistTokens(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(ApiErrorCode.AUTH_EMAIL_NOT_FOUND));

        if (!user.getActive()) {
            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash()))
                throw new ApiException(ApiErrorCode.INVALID_CREDENTIALS);

            user.setActive(true); // reactivates if it was in the process of being deleted
            user.setDeletionRequestedAt(null);
            userRepository.save(user);
        }

        authenticateUser(request);
        return generateAndPersistTokens(user);
    }

    public AuthResponse refreshToken(String refreshToken) {
        String username = jwtUtil.extractUsername(refreshToken);
        if (username == null)
            throw new ApiException(ApiErrorCode.INVALID_REFRESH_TOKEN);

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ApiException(ApiErrorCode.AUTH_EMAIL_NOT_FOUND));

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        validateRefreshToken(refreshToken, userDetails);

        String newAccessToken = generateAccessTokenForUser(user);
        Date newAccessTokenExpirationDate = jwtUtil.extractExpiration(newAccessToken);

        updateAccessToken(refreshToken, newAccessToken, newAccessTokenExpirationDate);

        return new AuthResponse(newAccessToken, newAccessTokenExpirationDate, refreshToken, jwtUtil.extractExpiration(refreshToken));
    }

    private void authenticateUser(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new ApiException(ApiErrorCode.INVALID_CREDENTIALS);
        }
    }

    private String generateAccessTokenForUser(User user) {
        UserDetails userDetails = buildUserDetails(user);
        userRepository.save(user);
        return jwtUtil.generateToken(userDetails);
    }

    private String generateRefreshTokenForUser(User user) {
        UserDetails userDetails = buildUserDetails(user);
        return jwtUtil.generateRefreshToken(userDetails);
    }

    private UserDetails buildUserDetails(User user) {
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .roles("USER")
                .accountLocked(!user.getActive())
                .build();
    }

    private void validateRefreshToken(String refreshToken, UserDetails userDetails) {
        if (!jwtUtil.isTokenValid(refreshToken, userDetails))
            throw new ApiException(ApiErrorCode.INVALID_REFRESH_TOKEN);
        if (jwtUtil.isTokenExpired(refreshToken))
            throw new ApiException(ApiErrorCode.EXPIRED_REFRESH_TOKEN);
        if (isRefreshTokenRevoked(refreshToken))
            throw new ApiException(ApiErrorCode.REVOKED_REFRESH_TOKEN);
    }

    private void updateAccessToken(String refreshToken, String newAccessToken, Date newAccessTokenExpiration) {
        UserToken userToken = userTokenRepository.findByRefreshTokenAndRevokedFalse(refreshToken)
                .orElseThrow(() -> new ApiException(ApiErrorCode.INVALID_REFRESH_TOKEN));

        userToken.setAccessToken(newAccessToken);
        userToken.setAccessTokenExpiration(newAccessTokenExpiration);
        userToken.setUpdatedAt(new Date());
        userToken.setRevoked(false);

        userTokenRepository.save(userToken);
    }

    private AuthResponse generateAndPersistTokens(User user) {
        String accessToken = generateAccessTokenForUser(user);
        String refreshToken = generateRefreshTokenForUser(user);

        saveTokens(user, accessToken, refreshToken);

        return new AuthResponse(
                accessToken,
                jwtUtil.extractExpiration(accessToken),
                refreshToken,
                jwtUtil.extractExpiration(refreshToken)
        );
    }

    private void saveTokens(User user, String accessToken, String refreshToken) {
        Date accessTokenExpiration = jwtUtil.extractExpiration(accessToken);
        Date refreshTokenExpiration = jwtUtil.extractExpiration(refreshToken);

        UserToken userToken = new UserToken();
        userToken.setUser(user);
        userToken.setAccessToken(accessToken);
        userToken.setRefreshToken(refreshToken);
        userToken.setAccessTokenExpiration(accessTokenExpiration);
        userToken.setRefreshTokenExpiration(refreshTokenExpiration);
        userToken.setRevoked(false);
        userToken.setCreatedAt(new Date());
        userToken.setUpdatedAt(new Date());

        userTokenRepository.save(userToken);
    }


    public boolean isRefreshTokenRevoked(String refreshToken) {
        return userTokenRepository.findByRefreshTokenAndRevokedTrue(refreshToken).isPresent();
    }

    public void revokeCurrentSession(String accessToken) {
        UserToken userToken = userTokenRepository.findByAccessTokenAndRevokedFalse(accessToken)
                .orElseThrow(() -> new ApiException(ApiErrorCode.INVALID_ACCESS_TOKEN));

        userToken.setRevoked(true);
        userToken.setUpdatedAt(new Date());

        userTokenRepository.save(userToken);
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
