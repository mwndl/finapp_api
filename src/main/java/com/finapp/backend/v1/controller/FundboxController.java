package com.finapp.backend.v1.controller;

import com.finapp.backend.v1.dto.fundbox.CreateFundBoxRequest;
import com.finapp.backend.v1.dto.fundbox.FundBoxDetailsResponse;
import com.finapp.backend.v1.dto.fundbox.FundBoxResponse;
import com.finapp.backend.v1.dto.fundbox.UpdateFundBoxRequest;
import com.finapp.backend.v1.service.FundboxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

import java.util.UUID;

@RestController
@RequestMapping("api/v1/fundboxes")
@RequiredArgsConstructor
@Tag(name = "FundBox", description = "Endpoints for managing fund boxes and collaborators")
public class FundboxController {

    private final FundboxService fundBoxService;

    @PostMapping
    @Operation(
            summary = "Create a fund box",
            description = "Creates a new fund box for the authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created - Fund box created successfully"),
                    @ApiResponse(responseCode = "400", description = "Bad request - Invalid request data or fund box name already exists"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
            }
    )
    public ResponseEntity<FundBoxResponse> createFundBox(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateFundBoxRequest request
    ) {
        FundBoxResponse created = fundBoxService.createFundBox(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(
            summary = "List user fund boxes",
            description = "Fetches a paginated list of fund boxes for the authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK - Fund boxes fetched successfully"),
                    @ApiResponse(responseCode = "204", description = "No Content - No fund boxes found for the user"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
            }
    )
    public ResponseEntity<Page<FundBoxResponse>> listUserFundBoxes(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("targetDate").ascending());
        return fundBoxService.listUserFundBoxes(userDetails.getUsername(), pageable);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a specific fund box",
            description = "Fetches details of a specific fund box by its ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK - Fund box details fetched successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User is not allowed to access this fund box"),
                    @ApiResponse(responseCode = "404", description = "Not Found - Fund box not found")
            }
    )
    public ResponseEntity<FundBoxDetailsResponse> getFundBoxById(
            @PathVariable UUID id,
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
    @Operation(
            summary = "Update a fund box",
            description = "Updates an existing fund box for the authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK - Fund box updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Bad request - Invalid request data"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User is not allowed to update this fund box"),
                    @ApiResponse(responseCode = "404", description = "Not Found - Fund box not found")
            }
    )
    public ResponseEntity<FundBoxResponse> updateFundBox(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFundBoxRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        FundBoxResponse updatedFundBox = fundBoxService.updateFundBox(id, email, request);
        return ResponseEntity.ok(updatedFundBox);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a fund box",
            description = "Deletes a specific fund box by its ID.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "No Content - Fund box deleted successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User is not allowed to delete this fund box"),
                    @ApiResponse(responseCode = "404", description = "Not Found - Fund box not found")
            }
    )
    public ResponseEntity<Void> deleteFundBox(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        fundBoxService.deleteFundBox(id, userDetails.getUsername());
        return ResponseEntity.noContent().build(); // HTTP 204
    }

    @DeleteMapping("/{id}/collaborators/{userId}")
    @Operation(
            summary = "Remove a collaborator from a fund box",
            description = "Removes a user as a collaborator from the fund box.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "No Content - Collaborator removed successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Only the owner can remove collaborators"),
                    @ApiResponse(responseCode = "404", description = "Not Found - Fund box or collaborator not found")
            }
    )
    public ResponseEntity<Void> removeCollaborator(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        fundBoxService.removeCollaborator(id, userDetails.getUsername(), userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/leave")
    @Operation(
            summary = "Leave a fund box",
            description = "Removes the authenticated user from the fund box as a collaborator.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "No Content - User left the fund box successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Owners cannot leave their own fund box"),
                    @ApiResponse(responseCode = "404", description = "Not Found - Fund box not found or user not a collaborator")
            }
    )
    public ResponseEntity<Void> leaveFundBox(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        fundBoxService.leaveFundBox(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
