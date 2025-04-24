package com.finapp.backend.controller;

import com.finapp.backend.dto.auth.*;
import com.finapp.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration and login")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account and returns an authentication token",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK - User successfully registered"),
                    @ApiResponse(responseCode = "400", description = "Bad Request - Validation errors"),
            }
    )
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate user",
            description = "Authenticates the user and returns a JWT token if credentials are valid",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK - Login successful"),
                    @ApiResponse(responseCode = "400", description = "Bad Request - Validation errors"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
            }
    )
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Generates a new access token using a valid refresh token",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK - Token successfully refreshed"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
            }
    )
    public ResponseEntity<AuthResponse> refresh(
            @RequestBody @Valid RefreshRequest request)
    {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Logout user",
            description = "Invalidates the current access token and refresh token",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK - Logout successful"),
                    @ApiResponse(responseCode = "400", description = "Bad Request - Invalid access token"),
            }
    )
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorizationHeader) {
        String accessToken = authorizationHeader.substring(7);
        authService.logout(accessToken);
        return ResponseEntity.noContent().build();
    }
}
