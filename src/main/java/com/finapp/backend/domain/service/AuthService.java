package com.finapp.backend.domain.service;

import com.finapp.backend.domain.model.LoginAttempt;
import com.finapp.backend.domain.model.enums.UserStatus;
import com.finapp.backend.domain.repository.LoginAttemptRepository;
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
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserTokenRepository userTokenRepository;
    private final UserDetailsService userDetailsService;
    private final LoginAttemptService loginAttemptService;
    private final ValidationService validationService;

    public AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        if (userRepository.findByEmail(request.getEmail()).isPresent())
            throw new ApiException(ApiErrorCode.EMAIL_ALREADY_REGISTERED);
        if (userRepository.existsByUsername(request.getUsername()))
            throw new ApiException(ApiErrorCode.USERNAME_ALREADY_TAKEN);

        validationService.validateName(request.getName());
        validationService.validateUsername(request.getUsername());
        validationService.validatePassword(request.getPassword());

        User user = createUser(request);
        userRepository.save(user);

        return generateAndPersistTokens(user, httpRequest);
    }

    public AuthResponse login(LoginRequest loginRequest, HttpServletRequest httpRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ApiException(ApiErrorCode.AUTH_EMAIL_NOT_FOUND));

        // reactivates if it was in the process of being deleted
        if (user.getStatus() == UserStatus.DEACTIVATION_REQUESTED) {
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash()))
                throw new ApiException(ApiErrorCode.INVALID_CREDENTIALS);

            user.setStatus(UserStatus.ACTIVE);
            user.setDeletionRequestedAt(null);
            userRepository.save(user);
        }

        authenticateUser(loginRequest, httpRequest);
        return generateAndPersistTokens(user, httpRequest);
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

    private void authenticateUser(LoginRequest request, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        String email = request.getEmail();

        LoginAttempt loginAttempt = loginAttemptService.getOrCreateLoginAttempt(ip, userAgent, email);

        if (loginAttempt.getBlockedUntil() != null && loginAttempt.getBlockedUntil().isAfter(LocalDateTime.now())) {
            long waitSeconds = Duration.between(LocalDateTime.now(), loginAttempt.getBlockedUntil()).getSeconds();
            throw new ApiException(ApiErrorCode.TOO_MANY_LOGIN_ATTEMPTS, Map.of("Retry-After", String.valueOf(waitSeconds)));
        }

        try {
            authenticateWithCredentials(request);
            loginAttemptService.clearLoginAttempts(ip, email);
        } catch (BadCredentialsException e) {
            loginAttemptService.handleFailedLoginAttempt(loginAttempt);
        }
    }

    private void authenticateWithCredentials(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
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
                .accountLocked(
                        user.getStatus() == UserStatus.DEACTIVATION_REQUESTED || user.getStatus() == UserStatus.LOCKED
                )
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

    private AuthResponse generateAndPersistTokens(User user, HttpServletRequest request) {
        String accessToken = generateAccessTokenForUser(user);
        String refreshToken = generateRefreshTokenForUser(user);

        saveTokens(user, accessToken, refreshToken, request);

        return new AuthResponse(
                accessToken,
                jwtUtil.extractExpiration(accessToken),
                refreshToken,
                jwtUtil.extractExpiration(refreshToken)
        );
    }

    private void saveTokens(User user, String accessToken, String refreshToken, HttpServletRequest request) {
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

        String deviceInfo = request.getHeader("User-Agent");
        String deviceIp = request.getRemoteAddr();

        userToken.setDeviceInfo(deviceInfo);
        userToken.setDeviceIp(deviceIp);

        userTokenRepository.save(userToken);
    }

    public boolean isRefreshTokenRevoked(String refreshToken) {
        return userTokenRepository.findByRefreshTokenAndRevokedTrue(refreshToken).isPresent();
    }

    private User createUser(RegisterRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE); // replace by 'UserStatus.PENDING_VERIFICATION' when the email logic is completed
        return user;
    }

}
