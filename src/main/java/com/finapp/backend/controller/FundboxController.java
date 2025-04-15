package com.finapp.backend.controller;

import com.finapp.backend.dto.fundbox.CreateFundBoxRequest;
import com.finapp.backend.dto.fundbox.FundBoxResponse;
import com.finapp.backend.model.FundBox;
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

    @GetMapping("/{fundBoxId}")
    public ResponseEntity<FundBoxResponse> getFundBoxById(
            @PathVariable Long fundBoxId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        FundBoxResponse fundBox = fundBoxService.getFundBoxById(fundBoxId, userDetails.getUsername());
        return ResponseEntity.ok(fundBox);
    }


}
