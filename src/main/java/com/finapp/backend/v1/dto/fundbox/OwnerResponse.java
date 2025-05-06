package com.finapp.backend.v1.dto.fundbox;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class OwnerResponse {
    UUID id;
    String name;
}
