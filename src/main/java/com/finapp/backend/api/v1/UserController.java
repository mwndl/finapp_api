package com.finapp.backend.api.v1;

import com.finapp.backend.dto.user.UpdateUserDataRequest;
import com.finapp.backend.dto.user.UpdateUserPasswordRequest;
import com.finapp.backend.dto.user.UserResponse;
import com.finapp.backend.domain.service.UserService;
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
@RequestMapping("api/v1/users")
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
            summary = "Update user data",
            description = "Updates the authenticated user's data.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK - User data updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Bad Request - Validation errors"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User not allowed to update this data")
            }
    )
    public ResponseEntity<?> updateUserData(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateUserDataRequest request
    ) {
        userService.updateUserData(userDetails.getUsername(), request.getNewName(), request.getNewUsername());
        return ResponseEntity.ok("User data updated successfully");
    }

    @PutMapping("/password")
    @Operation(
            summary = "Update user password",
            description = "Updates the authenticated user's password.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK - Password updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Bad Request - Validation errors"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Invalid password")
            }
    )
    public ResponseEntity<?> updateUserPassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid UpdateUserPasswordRequest request
    ) {
        userService.updateUserPassword(userDetails.getUsername(), request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok("Password updated successfully");
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
