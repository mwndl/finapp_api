package com.finapp.backend.controller;

import com.finapp.backend.dto.deposit.CreateDepositRequest;
import com.finapp.backend.dto.deposit.DepositResponse;
import com.finapp.backend.dto.deposit.DepositSummaryResponse;
import com.finapp.backend.dto.deposit.UpdateDepositRequest;
import com.finapp.backend.model.enums.TransactionType;
import com.finapp.backend.service.DepositService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deposit")
@RequiredArgsConstructor
@Tag(name = "Deposit", description = "Endpoints for managing deposits")
public class DepositController {

    private final DepositService depositService;

    @PostMapping
    @Operation(
            summary = "Create a deposit",
            description = "Creates a new deposit for the authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Deposit created successfully"),
                    @ApiResponse(responseCode = "400", description = "Bad Request - Invalid request data"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User is not allowed to access the selected fund box"),
                    @ApiResponse(responseCode = "404", description = "Not Found - Fund box not found")

            }
    )
    public ResponseEntity<?> createDeposit(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateDepositRequest request
    ) {
        depositService.createDeposit(userDetails.getUsername(), request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(
            summary = "List user deposits",
            description = "Fetches a paginated list of deposits for the authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of deposits retrieved successfully"),
                    @ApiResponse(responseCode = "204", description = "No Content - No deposits found"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User account is deactivated"),
                    @ApiResponse(responseCode = "404", description = "Not Found - User not found")

            }
    )
    public ResponseEntity<Page<DepositResponse>> listDeposits(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) List<TransactionType> transactionType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return depositService.listUserDeposits(
                userDetails.getUsername(),
                transactionType,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"))
        );
    }

    @GetMapping("/{depositId}")
    @Operation(
            summary = "Get a specific deposit",
            description = "Fetches detailed information of a specific deposit by its ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Deposit retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Access to the deposit is not allowed"),
                    @ApiResponse(responseCode = "404", description = "Not Found - Deposit or user not found")

            }
    )
    public ResponseEntity<DepositResponse> getDepositById(
            @PathVariable Long depositId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        DepositResponse deposit = depositService.getDepositById(depositId, userDetails.getUsername());
        return ResponseEntity.ok(deposit);
    }

    @GetMapping("/summary")
    @Operation(
            summary = "Get deposit summary",
            description = "Fetches a summary of the user's deposits.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Deposit summary retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User account is deactivated"),
                    @ApiResponse(responseCode = "404", description = "Not Found - User not found")

            }
    )
    public ResponseEntity<DepositSummaryResponse> getSummary(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(depositService.getDepositSummary(userDetails.getUsername()));
    }

    @PutMapping("/{depositId}")
    @Operation(
            summary = "Update a deposit",
            description = "Updates an existing deposit for the authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Deposit updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Bad request - Invalid request data"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User not allowed to update this deposit or fund box"),
                    @ApiResponse(responseCode = "404", description = "Not Found - Deposit or fund box not found")

            }
    )
    public ResponseEntity<DepositResponse> updateDeposit(
            @PathVariable Long depositId,
            @RequestBody @Valid UpdateDepositRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        DepositResponse updated = depositService.updateDeposit(depositId, userDetails.getUsername(), request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{depositId}")
    @Operation(
            summary = "Delete a deposit",
            description = "Deletes a specific deposit by its ID.",
            responses = {

            }
    )
    public ResponseEntity<Void> deleteDeposit(
            @PathVariable Long depositId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        depositService.deleteDeposit(depositId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
