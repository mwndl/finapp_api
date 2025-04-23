package com.finapp.backend.controller;

import com.finapp.backend.dto.user.InviteResponse;
import com.finapp.backend.service.FundboxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invites")
@RequiredArgsConstructor
@Tag(name = "Invitations", description = "Endpoints for managing fund box collaboration invitations")
public class InvitationController {

    private final FundboxService fundBoxService;

    @GetMapping("/received")
    @Operation(summary = "Get pending invitations", description = "Gets a list of pending invitations for the user")
    public ResponseEntity<Page<InviteResponse>> getInvites(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<InviteResponse> invites = fundBoxService.getUserInvites(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(invites);
    }

    @GetMapping("/sent")
    @Operation(summary = "Get sent invitations", description = "Get the paginated list of invitations sent by the user")
    public ResponseEntity<Page<InviteResponse>> getSentInvitations(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("invitationDate").descending());
        Page<InviteResponse> sentInvites = fundBoxService.getSentInvites(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(sentInvites);
    }

    @PostMapping("/{invitationId}/accept")
    @Operation(summary = "Accept a fund box collaboration invitation", description = "Accepts an invitation to become a collaborator on a fund box.")
    public ResponseEntity<Void> acceptInvitation(
            @PathVariable Long invitationId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        fundBoxService.acceptInvitation(invitationId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{invitationId}/decline")
    @Operation(summary = "Decline an invitation", description = "Declines an invitation to join a fund box.")
    public ResponseEntity<Void> declineInvitation(
            @PathVariable Long invitationId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        fundBoxService.declineInvitation(invitationId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/fundbox/{fundBoxId}/user/{userId}")
    @Operation(summary = "Invite a user to a fund box", description = "Invites a user to collaborate on a specific fund box.")
    public ResponseEntity<Void> inviteCollaborator(
            @PathVariable Long fundBoxId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        fundBoxService.inviteCollaborator(fundBoxId, userDetails.getUsername(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{invitationId}")
    @Operation(summary = "Cancel an invitation", description = "Cancels a pending invitation to a fund box.")
    public ResponseEntity<Void> cancelInvitation(
            @PathVariable Long invitationId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        fundBoxService.cancelInvitation(invitationId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
