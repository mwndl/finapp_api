package com.finapp.backend.service;

import com.finapp.backend.dto.auth.AuthResponse;
import com.finapp.backend.dto.auth.LoginRequest;
import com.finapp.backend.dto.auth.RegisterRequest;
import com.finapp.backend.exception.ApiException;
import com.finapp.backend.exception.ApiErrorCode;
import com.finapp.backend.model.User;
import com.finapp.backend.repository.UserRepository;
import com.finapp.backend.security.JwtUtil;
import io.jsonwebtoken.Jwts;
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

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ApiException(ApiErrorCode.EMAIL_ALREADY_REGISTERED);
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);
        userRepository.save(user);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .roles("USER")
                .accountLocked(!user.getActive())
                .build();

        String token = jwtUtil.generateToken(userDetails);
        Date expirationDate = jwtUtil.extractExpiration(token);

        return new AuthResponse(token, expirationDate);
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND));

        if (!user.getActive()) {
            user.setActive(true); // reactivates if it was in the process of being deleted
            user.setDeletionRequestedAt(null);
            userRepository.save(user);
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new ApiException(ApiErrorCode.INVALID_CREDENTIALS);
        }

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .roles("USER")
                .accountLocked(!user.getActive())
                .build();

        String token = jwtUtil.generateToken(userDetails);

        Date expirationDate = jwtUtil.extractExpiration(token);

        return new AuthResponse(token, expirationDate);
    }
}
