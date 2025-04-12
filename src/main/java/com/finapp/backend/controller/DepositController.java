package com.finapp.backend.controller;

import com.finapp.backend.dto.deposit.CreateDepositRequest;
import com.finapp.backend.dto.deposit.DepositResponse;
import com.finapp.backend.dto.deposit.DepositSummaryResponse;
import com.finapp.backend.model.enums.TransactionType;
import com.finapp.backend.service.DepositService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deposit")
@RequiredArgsConstructor
public class DepositController {

    private final DepositService depositService;

    @PostMapping
    public ResponseEntity<?> createDeposit(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateDepositRequest request
    ) {
        depositService.createDeposit(userDetails.getUsername(), request);
        return ResponseEntity.ok("Deposit created successfully");
    }

    @GetMapping
    public ResponseEntity<List<DepositResponse>> listDeposits(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) List<TransactionType> transactionType
    ) {
        return ResponseEntity.ok(depositService.listUserDeposits(
                userDetails.getUsername(),
                transactionType
        ));
    }

    @GetMapping("/summary")
    public ResponseEntity<DepositSummaryResponse> getSummary(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(depositService.getDepositSummary(userDetails.getUsername()));
    }

}
