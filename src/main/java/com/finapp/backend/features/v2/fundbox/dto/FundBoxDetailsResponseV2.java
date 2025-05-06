package com.finapp.backend.features.v2.fundbox.dto;

import com.finapp.backend.features.v1.fundbox.dto.FundboxDepositResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class FundBoxDetailsResponseV2 {
    private UUID id;
    private String name;
    private Metrics metrics;
    private Collaboration collaboration;
    private List<FundboxDepositResponse> deposits;
}