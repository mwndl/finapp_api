package com.finapp.backend.features.v1.fundbox.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class FundboxDepositResponse {
    private UUID id;
    private BigDecimal amount;
    private LocalDateTime dateTime;
    private String description;
    private String TransactionType;
    private OwnerResponse owner;
}
