package com.finapp.backend.dto.fundbox;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FundBoxResponse(
        Long id,
        String name,
        BigDecimal financialGoal,
        LocalDate targetDate,
        OwnerResponse owner
) {}
