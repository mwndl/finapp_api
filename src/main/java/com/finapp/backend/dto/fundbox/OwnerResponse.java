package com.finapp.backend.dto.fundbox;

import java.util.UUID;

public record OwnerResponse(
        UUID id,
        String name
) {}
