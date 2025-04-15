package com.finapp.backend.dto.fundbox;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FundBoxListResponse(
        Long id,
        String name,
        BigDecimal totalInvested,
        BigDecimal financialGoal,
        int progressPercentage,
        LocalDate targetDate,
        long daysRemaining
) {}
