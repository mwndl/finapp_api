package com.finapp.backend.features.v2.fundbox.controller;

import com.finapp.backend.features.v2.fundbox.service.FundboxServiceV2;
import com.finapp.backend.features.v2.fundbox.dto.FundBoxDetailsResponseV2;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v2/fundboxes")
@RequiredArgsConstructor
@Tag(name = "FundBox", description = "Endpoints for managing fund boxes and collaborators")
public class FundboxControllerV2 {

    private final FundboxServiceV2 fundBoxService;

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
    public ResponseEntity<FundBoxDetailsResponseV2> getFundBoxById(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        FundBoxDetailsResponseV2 response = fundBoxService.getFundBoxDetailsV2(id, email, pageable);
        return ResponseEntity.ok(response);
    }
}

