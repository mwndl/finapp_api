package com.finapp.backend.features.v1.deposit.dto;

import com.finapp.backend.domain.model.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreateDepositRequest {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "DateTime is required")
    private LocalDateTime dateTime;

    private String description;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    private UUID fundBoxId;
}
