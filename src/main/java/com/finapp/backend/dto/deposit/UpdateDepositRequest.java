package com.finapp.backend.dto.deposit;

import com.finapp.backend.domain.model.enums.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UpdateDepositRequest {
    private BigDecimal amount;
    private LocalDateTime dateTime;
    private String description;
    private TransactionType transactionType;
    private UUID fundBoxId;
}

