package com.finapp.backend.features.v1.fundbox.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FundBoxResponse(
        UUID id,
        String name,
        BigDecimal financialGoal,
        LocalDate targetDate,
        OwnerResponse owner
) {}
