package com.finapp.backend.service;

import com.finapp.backend.dto.auth.AuthResponse;
import com.finapp.backend.dto.auth.LoginRequest;
import com.finapp.backend.dto.auth.RegisterRequest;
import com.finapp.backend.exception.ApiException;
import com.finapp.backend.exception.ApiErrorCode;
import com.finapp.backend.model.User;
import com.finapp.backend.model.UserToken;
import com.finapp.backend.repository.UserRepository;
import com.finapp.backend.repository.UserTokenRepository;
import com.finapp.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

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

        String accessToken = generateAccessTokenForUser(user);
        String refreshToken = generateRefreshTokenForUser(user);
        Date accessTokenExpirationDate = jwtUtil.extractExpiration(accessToken);
        Date refreshTokenExpirationDate = jwtUtil.extractExpiration(refreshToken);

        saveTokens(user, accessToken, refreshToken, accessTokenExpirationDate, refreshTokenExpirationDate);

        return new AuthResponse(accessToken, accessTokenExpirationDate, refreshToken, refreshTokenExpirationDate);
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

        String accessToken = generateAccessTokenForUser(user);
        String refreshToken = generateRefreshTokenForUser(user);
        Date accessTokenExpirationDate = jwtUtil.extractExpiration(accessToken);
        Date refreshTokenExpirationDate = jwtUtil.extractExpiration(refreshToken);

        saveTokens(user, accessToken, refreshToken, accessTokenExpirationDate, refreshTokenExpirationDate);

        return new AuthResponse(accessToken, accessTokenExpirationDate, refreshToken, refreshTokenExpirationDate);
    }

    public void logout(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ApiException(ApiErrorCode.AUTH_EMAIL_NOT_FOUND));

        UserToken userToken = userTokenRepository.findByUserIdAndRevokedFalse(user.getId())
                .orElseThrow(() -> new ApiException(ApiErrorCode.INVALID_REFRESH_TOKEN));

        userToken.setRevoked(true);
        userTokenRepository.save(userToken);
    }

    public AuthResponse refreshToken(String refreshToken) {

        String username = jwtUtil.extractUsername(refreshToken);
        if (username == null) {
            throw new ApiException(ApiErrorCode.INVALID_REFRESH_TOKEN);
        }

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ApiException(ApiErrorCode.AUTH_EMAIL_NOT_FOUND));

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        validateRefreshToken(refreshToken, userDetails);

        String newAccessToken = generateAccessTokenForUser(user);
        Date newAccessTokenExpirationDate = jwtUtil.extractExpiration(newAccessToken);

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
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .roles("USER")
                .accountLocked(!user.getActive())
                .build();

        userRepository.save(user);

        return jwtUtil.generateToken(userDetails);
    }

    private String generateRefreshTokenForUser(User user) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .roles("USER")
                .accountLocked(!user.getActive())
                .build();

        return jwtUtil.generateRefreshToken(userDetails);
    }

    private void saveTokens(User user, String accessToken, String refreshToken, Date accessTokenExpiration, Date refreshTokenExpiration) {
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
}
