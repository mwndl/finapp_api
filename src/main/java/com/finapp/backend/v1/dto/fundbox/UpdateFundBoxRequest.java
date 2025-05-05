package com.finapp.backend.v1.dto.fundbox;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateFundBoxRequest {
    private String name;
    private BigDecimal financialGoal;
    private LocalDate targetDate;
}
