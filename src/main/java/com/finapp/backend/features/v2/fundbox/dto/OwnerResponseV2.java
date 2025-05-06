package com.finapp.backend.features.v2.fundbox.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class OwnerResponseV2 {
    private UUID id;
    private String name;
    private String username;
    private UserMetrics metrics;
}
