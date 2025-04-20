package com.finapp.backend.controller;

import com.finapp.backend.dto.user.InviteResponse;
import com.finapp.backend.dto.user.UpdateUserRequest;
import com.finapp.backend.dto.user.UserResponse;
import com.finapp.backend.service.FundboxService;
import com.finapp.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "Endpoints for managing user data and account")
public class UserController {

    private final UserService userService;
    private final FundboxService fundboxService;

    @GetMapping
    @Operation(summary = "Get user info", description = "Returns the authenticated user's profile information")
    public ResponseEntity<UserResponse> getUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponse response = userService.getUserInfo(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PutMapping
    @Operation(summary = "Update user", description = "Updates the authenticated user's name or password")
    public ResponseEntity<?> updateUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        userService.updateUser(userDetails.getUsername(), request);
        return ResponseEntity.ok("User updated successfully");
    }

    @DeleteMapping
    @Operation(summary = "Request account deletion", description = "Marks the account for deletion. Can be undone by logging in within 30 days")
    public ResponseEntity<?> requestAccountDeletion(@AuthenticationPrincipal UserDetails userDetails) {
        userService.requestAccountDeletion(userDetails.getUsername());
        return ResponseEntity.ok("Account deletion requested. You can revert by logging in within 30 days.");
    }

    @GetMapping("/invites")
    @Operation(summary = "Get pending invitations", description = "Gets a list of pending invitations for the user")
    public ResponseEntity<Page<InviteResponse>> getInvites(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<InviteResponse> invites = fundboxService.getUserInvites(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(invites);
    }

    @PostMapping("/invitations/{invitationId}/accept")
    @Operation(summary = "Accept a fund box collaboration invitation", description = "Accepts an invitation to become a collaborator on a fund box.")
    public ResponseEntity<Void> acceptInvitation(
            @PathVariable Long invitationId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        fundboxService.acceptInvitation(invitationId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/invitations/{invitationId}/decline")
    @Operation(summary = "Decline an invitation", description = "Declines an invitation to join a fund box.")
    public ResponseEntity<Void> declineInvitation(
            @PathVariable Long invitationId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        fundboxService.declineInvitation(invitationId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
