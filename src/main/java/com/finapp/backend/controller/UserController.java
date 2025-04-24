package com.finapp.backend.controller;

import com.finapp.backend.dto.user.UpdateUserRequest;
import com.finapp.backend.dto.user.UserResponse;
import com.finapp.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "Endpoints for managing user data and account")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(
            summary = "Get user info",
            description = "Returns the authenticated user's profile information",
            responses = {
                @ApiResponse(responseCode = "200", description = "OK - User profile returned successfully"),
                @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
            }
    )
    public ResponseEntity<UserResponse> getUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponse response = userService.getUserInfo(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PutMapping
    @Operation(
            summary = "Update user",
            description = "Updates the authenticated user's name or password",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK - User updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Bad Request - Validation errors"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Invalid password")
            }
    )
    public ResponseEntity<?> updateUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        userService.updateUser(userDetails.getUsername(), request);
        return ResponseEntity.ok("User updated successfully");
    }

    @DeleteMapping
    @Operation(
            summary = "Request account deletion",
            description = "Marks the account for deletion. Can be undone by logging in within 30 days",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK - Account deletion requested"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
            }
    )
    public ResponseEntity<?> requestAccountDeletion(@AuthenticationPrincipal UserDetails userDetails) {
        userService.requestAccountDeletion(userDetails.getUsername());
        return ResponseEntity.ok("Account deletion requested. You can revert by logging in within 30 days.");
    }
}
