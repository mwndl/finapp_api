package com.finapp.backend.features.v1.fundbox.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CollaboratorResponse {
    private UUID id;
    private String name;
    private LocalDate joinedAt;
}
