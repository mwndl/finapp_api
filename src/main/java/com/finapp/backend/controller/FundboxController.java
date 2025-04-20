package com.finapp.backend.controller;

import com.finapp.backend.dto.fundbox.CreateFundBoxRequest;
import com.finapp.backend.dto.fundbox.FundBoxDetailsResponse;
import com.finapp.backend.dto.fundbox.FundBoxResponse;
import com.finapp.backend.dto.fundbox.UpdateFundBoxRequest;
import com.finapp.backend.service.FundboxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/fundbox")
@RequiredArgsConstructor
@Tag(name = "FundBox", description = "Endpoints for managing fund boxes and collaborators")
public class FundboxController {

    private final FundboxService fundBoxService;

    @PostMapping
    @Operation(summary = "Create a fund box", description = "Creates a new fund box for the authenticated user.")
    public ResponseEntity<FundBoxResponse> createFundBox(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateFundBoxRequest request
    ) {
        FundBoxResponse created = fundBoxService.createFundBox(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "List user fund boxes", description = "Fetches a paginated list of fund boxes for the authenticated user.")
    public ResponseEntity<Page<FundBoxResponse>> listUserFundBoxes(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("targetDate").ascending());
        return fundBoxService.listUserFundBoxes(userDetails.getUsername(), pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a specific fund box", description = "Fetches details of a specific fund box by its ID.")
    public ResponseEntity<FundBoxDetailsResponse> getFundBoxById(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        FundBoxDetailsResponse response = fundBoxService.getFundBoxDetails(id, email, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a fund box", description = "Updates an existing fund box for the authenticated user.")
    public ResponseEntity<FundBoxResponse> updateFundBox(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFundBoxRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        FundBoxResponse updatedFundBox = fundBoxService.updateFundBox(id, email, request);
        return ResponseEntity.ok(updatedFundBox);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a fund box", description = "Deletes a specific fund box by its ID.")
    public ResponseEntity<Void> deleteFundBox(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        fundBoxService.deleteFundBox(id, userDetails.getUsername());
        return ResponseEntity.noContent().build(); // HTTP 204
    }

    @PostMapping("/{id}/invitations/{userId}")
    @Operation(summary = "Invite a user to collaborate on a fund box", description = "Sends an invitation to a user to become a collaborator on the specified fund box.")
    public ResponseEntity<Void> inviteCollaborator(
            @PathVariable Long id,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        fundBoxService.inviteCollaborator(id, userDetails.getUsername(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @DeleteMapping("/{id}/collaborators/{userId}")
    @Operation(summary = "Remove a collaborator from a fund box", description = "Removes a user as a collaborator from the fund box.")
    public ResponseEntity<Void> removeCollaborator(
            @PathVariable Long id,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        fundBoxService.removeCollaborator(id, userDetails.getUsername(), userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/leave")
    @Operation(summary = "Leave a fund box", description = "Removes the authenticated user from the fund box as a collaborator.")
    public ResponseEntity<Void> leaveFundBox(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        fundBoxService.leaveFundBox(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
