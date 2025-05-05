package com.finapp.backend.dto.fundbox.v2;

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
