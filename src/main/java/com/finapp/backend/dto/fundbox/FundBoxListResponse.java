package com.finapp.backend.dto.fundbox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FundBoxListResponse(
        UUID id,
        String name,
        BigDecimal totalInvested,
        BigDecimal financialGoal,
        int progressPercentage,
        LocalDate targetDate,
        long daysRemaining
) {}
