package com.finapp.backend.dto.fundbox.v1;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class FundBoxSummary {
    private UUID fundBoxId;
    private String fundBoxName;
}
