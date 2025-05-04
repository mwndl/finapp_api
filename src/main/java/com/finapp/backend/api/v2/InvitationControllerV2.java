package com.finapp.backend.api.v2;

import com.finapp.backend.domain.service.FundBoxInviteService;
import com.finapp.backend.dto.fundbox.CollaboratorInvitationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v2/invites")
@RequiredArgsConstructor
@Tag(name = "Invitations", description = "Endpoints for managing fund box collaboration invitations")
public class InvitationControllerV2 {

    private final FundBoxInviteService fundBoxInviteService;

    @PostMapping("/fundbox/{fundBoxId}/invite")
    @Operation(
            summary = "Invite a user to a fund box",
            description = "Invites a user to collaborate on a specific fundbox. Supports UUID, username or email as user input.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created - Invitation sent successfully"),
                    @ApiResponse(responseCode = "400", description = "Bad Request - User is already a collaborator or has a pending invitation"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - only the owner can invite collaborators"),
                    @ApiResponse(responseCode = "404", description = "Not Found - Fund box or user not found"),
            }
    )
    public ResponseEntity<Void> inviteCollaborator(
            @PathVariable UUID fundBoxId,
            @RequestBody CollaboratorInvitationRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        fundBoxInviteService.inviteCollaborator(fundBoxId, userDetails.getUsername(), request.getUserIdentifier());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

