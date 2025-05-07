package com.finapp.backend.dto.auth;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RefreshRequest {

    @NotNull(message = "Refresh token must not be null")
    private String refreshToken;

}
