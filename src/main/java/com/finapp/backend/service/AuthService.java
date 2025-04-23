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

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent())
            throw new ApiException(ApiErrorCode.EMAIL_ALREADY_REGISTERED);

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);
        user.setTokenVersion(1);
        userRepository.save(user);

        String accessToken = generateAccessTokenForUser(user);
        String refreshToken = generateRefreshTokenForUser(user);
        Date accessTokenExpirationDate = jwtUtil.extractExpiration(accessToken);
        Date refreshTokenExpirationDate = jwtUtil.extractExpiration(refreshToken);

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

    public AuthResponse refreshToken(String refreshToken, UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ApiException(ApiErrorCode.AUTH_EMAIL_NOT_FOUND));

        if (isRefreshTokenRevoked(refreshToken) || !jwtUtil.isTokenValid(refreshToken, userDetails))
            throw new ApiException(ApiErrorCode.INVALID_REFRESH_TOKEN);

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

        int newTokenVersion = user.getTokenVersion() + 1;
        user.setTokenVersion(newTokenVersion);
        userRepository.save(user);

        return jwtUtil.generateToken(userDetails, user.getTokenVersion());
    }

    private String generateRefreshTokenForUser(User user) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .roles("USER")
                .accountLocked(!user.getActive())
                .build();

        return jwtUtil.generateRefreshToken(userDetails, user.getTokenVersion());
    }
}
