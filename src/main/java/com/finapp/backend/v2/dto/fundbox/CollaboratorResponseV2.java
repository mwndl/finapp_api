package com.finapp.backend.v2.dto.fundbox;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CollaboratorResponseV2 {
    private UUID id;
    private String name;
    private String username;
    private LocalDate joinedAt;
    private UserMetrics metrics;
}
