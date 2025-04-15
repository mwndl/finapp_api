package com.finapp.backend.controller;

import com.finapp.backend.dto.fundbox.CreateFundBoxRequest;
import com.finapp.backend.dto.fundbox.FundBoxDetailsResponse;
import com.finapp.backend.dto.fundbox.FundBoxResponse;
import com.finapp.backend.dto.fundbox.UpdateFundBoxRequest;
import com.finapp.backend.service.FundboxService;
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
public class FundboxController {

    private final FundboxService fundBoxService;

    @PostMapping
    public ResponseEntity<FundBoxResponse> createFundBox(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateFundBoxRequest request
    ) {
        FundBoxResponse created = fundBoxService.createFundBox(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<FundBoxResponse>> listUserFundBoxes(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("targetDate").ascending());
        Page<FundBoxResponse> result = fundBoxService.listUserFundBoxes(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
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
    public ResponseEntity<FundBoxResponse> updateFundBox(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFundBoxRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        FundBoxResponse updatedFundBox = fundBoxService.updateFundBox(id, email, request);
        return ResponseEntity.ok(updatedFundBox);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFundBox(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        fundBoxService.deleteFundBox(id, userDetails.getUsername());
        return ResponseEntity.noContent().build(); // HTTP 204
    }

}
