package com.finapp.backend.dto.deposit;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class DepositSummaryResponse {
    private BigDecimal total;
    private BigDecimal entryTotal;
    private BigDecimal exitTotal;
}
