package com.finapp.backend.dto.fundbox;

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
