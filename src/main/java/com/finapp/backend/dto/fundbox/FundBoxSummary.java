package com.finapp.backend.dto.fundbox;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FundBoxSummary {
    private Long fundBoxId;
    private String fundBoxName;
}
