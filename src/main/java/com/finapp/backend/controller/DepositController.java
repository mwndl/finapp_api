package com.finapp.backend.controller;

import com.finapp.backend.dto.deposit.CreateDepositRequest;
import com.finapp.backend.dto.deposit.DepositResponse;
import com.finapp.backend.dto.deposit.DepositSummaryResponse;
import com.finapp.backend.dto.deposit.UpdateDepositRequest;
import com.finapp.backend.model.enums.TransactionType;
import com.finapp.backend.service.DepositService;
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
    public ResponseEntity<Page<DepositResponse>> listDeposits(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) List<TransactionType> transactionType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(depositService.listUserDeposits(
                userDetails.getUsername(),
                transactionType,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"))
        ));
    }


    @GetMapping("/summary")
    public ResponseEntity<DepositSummaryResponse> getSummary(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(depositService.getDepositSummary(userDetails.getUsername()));
    }

    @PatchMapping("/{depositId}")
    public ResponseEntity<Void> updateDeposit(
            @PathVariable Long depositId,
            @RequestBody @Valid UpdateDepositRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        depositService.updateDeposit(depositId, userDetails.getUsername(), request);
        return ResponseEntity.noContent().build();
    }

}
