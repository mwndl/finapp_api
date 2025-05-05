package com.finapp.backend.v2.dto.fundbox;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class Metrics {

    private int totalDeposits;
    private BigDecimal totalEntries;
    private BigDecimal totalExits;
    private BigDecimal netBalance;
    private BigDecimal financialGoal;
    private LocalDate targetDate;
}

