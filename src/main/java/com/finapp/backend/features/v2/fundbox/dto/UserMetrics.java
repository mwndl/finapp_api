package com.finapp.backend.features.v2.fundbox.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class UserMetrics {
    private BigDecimal totalAmount;
    private BigDecimal totalEntries;
    private BigDecimal totalExits;
    private Integer totalDeposits;
}
