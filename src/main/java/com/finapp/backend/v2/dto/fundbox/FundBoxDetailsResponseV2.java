package com.finapp.backend.v2.dto.fundbox;

import com.finapp.backend.v1.dto.fundbox.FundboxDepositResponse;
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