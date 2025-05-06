package com.finapp.backend.features.v1.fundbox.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class FundBoxSummary {
    private UUID fundBoxId;
    private String fundBoxName;
}
