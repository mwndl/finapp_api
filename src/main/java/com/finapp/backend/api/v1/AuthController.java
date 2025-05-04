package com.finapp.backend.api.v1;

import com.finapp.backend.domain.model.PasswordResetToken;
import com.finapp.backend.domain.service.*;
import com.finapp.backend.domain.service.utils.ResetLinkBuilder;
import com.finapp.backend.dto.auth.*;
import com.finapp.backend.exception.ApiErrorCode;
import com.finapp.backend.exception.ApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration and login")
public class AuthController {

    private final AuthService authService;
    private final SessionService sessionService;
    private final PasswordResetService passwordResetService;
    private final EmailService emailService;
    private final UserService userService;
    private final ResetLinkBuilder resetLinkBuilder;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account and returns an authentication token",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK - User successfully registered"),
                    @ApiResponse(responseCode = "400", description = "Bad Request - Validation errors"),
            }
    )
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.register(request, httpRequest));
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
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.login(request, httpRequest));
    }

    @PostMapping("/validate-username")
    @Operation(
            summary = "Validate Username Availability",
            description = "Checks if the given username is available for registration. If the username is already taken, it returns an error.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK - Username is available for registration"),
                    @ApiResponse(responseCode = "400", description = "Bad Request - Username is already taken")
            }
    )
    public ResponseEntity<Void> validateUsername(@RequestBody UsernameValidationRequest request) {
        authService.validateUsernameAvailability(request.getUsername());
        return ResponseEntity.ok().build();
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

    @DeleteMapping("/logout")
    @Operation(
            summary = "Logout user",
            description = "Invalidates the current access token and refresh token",
            responses = {
                    @ApiResponse(responseCode = "204", description = "No Content - Logout successful"),
                    @ApiResponse(responseCode = "400", description = "Bad Request - Invalid or missing Authorization header"),
            }
    )
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer "))
            throw new ApiException(ApiErrorCode.INVALID_ACCESS_TOKEN);

        String accessToken = authorizationHeader.substring(7);
        sessionService.revokeCurrentSession(accessToken);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("logout/{sessionId}")
    @Operation(
            summary = "Logout a specific session",
            description = "Logout and revokes the token of another open session.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "No Content - Session revoked successfully"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User not allowed to revoke this session"),
                    @ApiResponse(responseCode = "404", description = "Not Found - Session not found")
            }
    )
    public ResponseEntity<Void> logoutById(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        String authorizationHeader = httpRequest.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer "))
            throw new ApiException(ApiErrorCode.INVALID_ACCESS_TOKEN);
        String currentAccessToken = authorizationHeader.substring(7);

        sessionService.revokeSpecificSession(sessionId, userDetails.getUsername(), currentAccessToken);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/logout-all")
    @Operation(
            summary = "Logout all sessions",
            description = "Invalidates all the active sessions",
            responses = {
                    @ApiResponse(responseCode = "204", description = "No Content - Logout successful"),
                    @ApiResponse(responseCode = "400", description = "Bad Request - Invalid or missing Authorization header"),
            }
    )
    public ResponseEntity<Void> logoutAllSessions(@AuthenticationPrincipal UserDetails userDetails, HttpServletRequest httpRequest) {
        String authorizationHeader = httpRequest.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer "))
            throw new ApiException(ApiErrorCode.INVALID_ACCESS_TOKEN);
        String currentAccessToken = authorizationHeader.substring(7);

        sessionService.logoutAllSessions(userDetails.getUsername(), currentAccessToken);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sessions")
    @Operation(
            summary = "Get active sessions",
            description = "Returns a list of active user sessions",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK - Active sessions returned"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
            }
    )
    public ResponseEntity<List<SessionInfo>> getActiveSessions(@AuthenticationPrincipal UserDetails userDetails, HttpServletRequest httpRequest) {
        String authorizationHeader = httpRequest.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer "))
            throw new ApiException(ApiErrorCode.INVALID_ACCESS_TOKEN);
        String currentAccessToken = authorizationHeader.substring(7);

        List<SessionInfo> activeSessions = sessionService.getActiveSessions(userDetails.getUsername(), currentAccessToken);
        return ResponseEntity.ok(activeSessions);
    }

    @PostMapping("/forgot-password")
    public void forgotPassword(@RequestBody ForgotPasswordRequest request) {
        PasswordResetToken token = passwordResetService.createToken(request.getEmail());
        String resetLink = resetLinkBuilder.buildResetPasswordLink(token.getToken());

        String message = "Click the link to reset your password: " + resetLink;
        emailService.sendEmail(request.getEmail(), "Password Reset", message);
    }

    @PostMapping("/reset-password")
    public void resetPassword(@RequestBody ResetPasswordRequest request) {
        PasswordResetToken token = passwordResetService.validateToken(request.getToken());
        userService.updatePasswordByEmail(token.getEmail(), request.getNewPassword());
        passwordResetService.markTokenAsUsed(token);
    }
}
