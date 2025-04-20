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
            description = "Creates a new user account and returns an authentication token"
    )
    @ApiResponse(responseCode = "200", description = "User successfully registered")
    @ApiResponse(responseCode = "400", description = "Validation failed: Fields are either missing or in invalid format")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate user",
            description = "Authenticates the user and returns a JWT token if credentials are valid"
    )
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "400", description = "Missing fields")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
