package com.finapp.backend.dto.fundbox;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CollaboratorResponse {

    private Long id;
    private String name;
    private LocalDate joinedAt;

    public CollaboratorResponse(Long id, String name, LocalDate joinedAt) {
        this.id = id;
        this.name = name;
        this.joinedAt = joinedAt;
    }
}
