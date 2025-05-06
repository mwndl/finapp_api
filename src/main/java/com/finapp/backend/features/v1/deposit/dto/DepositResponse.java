package com.finapp.backend.features.v1.deposit.dto;

import com.finapp.backend.features.v1.fundbox.dto.OwnerResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class DepositResponse {
    private UUID id;
    private BigDecimal amount;
    private LocalDateTime dateTime;
    private String description;
    private FundBoxInfo fundBox;
    private String TransactionType;
    private OwnerResponse owner;
}
