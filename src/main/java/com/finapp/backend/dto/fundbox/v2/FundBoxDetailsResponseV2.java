package com.finapp.backend.dto.fundbox.v2;

import com.finapp.backend.dto.fundbox.v1.FundboxDepositResponse;
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