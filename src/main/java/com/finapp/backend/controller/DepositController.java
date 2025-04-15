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
        return ResponseEntity.ok().build();
    }

    @GetMapping
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
    public ResponseEntity<DepositResponse> getDepositById(
            @PathVariable Long depositId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        DepositResponse deposit = depositService.getDepositById(depositId, userDetails.getUsername());
        return ResponseEntity.ok(deposit);
    }

    @GetMapping("/summary")
    public ResponseEntity<DepositSummaryResponse> getSummary(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(depositService.getDepositSummary(userDetails.getUsername()));
    }

    @PatchMapping("/{depositId}")
    public ResponseEntity<DepositResponse> updateDeposit(
            @PathVariable Long depositId,
            @RequestBody @Valid UpdateDepositRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        DepositResponse updated = depositService.updateDeposit(depositId, userDetails.getUsername(), request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{depositId}")
    public ResponseEntity<Void> deleteDeposit(
            @PathVariable Long depositId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        depositService.deleteDeposit(depositId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
