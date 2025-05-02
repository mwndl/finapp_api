package com.finapp.backend.dto.fundbox;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CollaboratorResponse {

    private UUID id;
    private String name;
    private LocalDate joinedAt;

    public CollaboratorResponse(UUID id, String name, LocalDate joinedAt) {
        this.id = id;
        this.name = name;
        this.joinedAt = joinedAt;
    }
}
