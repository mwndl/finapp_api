package com.finapp.backend.dto.fundbox.v1;

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
