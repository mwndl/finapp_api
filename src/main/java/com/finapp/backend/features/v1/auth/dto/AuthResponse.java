package com.finapp.backend.features.v1.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private Date accessTokenExpiresAt;
    private String refreshToken;
    private Date refreshTokenExpiresAt;
}

