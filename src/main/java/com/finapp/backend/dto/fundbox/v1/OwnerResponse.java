package com.finapp.backend.dto.fundbox.v1;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class OwnerResponse {
    UUID id;
    String name;
}
