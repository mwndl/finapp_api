package com.finapp.backend.dto.deposit;

import com.finapp.backend.domain.model.enums.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateDepositRequest {
    private BigDecimal amount;
    private LocalDate date;
    private String description;
    private TransactionType transactionType;
    private Long fundBoxId;
}

