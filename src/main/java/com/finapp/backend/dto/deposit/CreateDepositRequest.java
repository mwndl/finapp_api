package com.finapp.backend.dto.deposit;

import com.finapp.backend.model.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateDepositRequest {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Date is required")
    private LocalDate date;

    private String description;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    private Long fundBoxId;
}
