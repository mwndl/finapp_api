package com.finapp.backend.dto.fundbox;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateFundBoxRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotNull(message = "Financial goal is required")
        @Positive(message = "Financial goal must be greater than zero")
        BigDecimal financialGoal,

        @NotNull(message = "Target date is required")
        @Future(message = "Target date must be in the future")
        LocalDate targetDate
) {}
