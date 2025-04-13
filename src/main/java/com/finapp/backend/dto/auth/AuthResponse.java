package com.finapp.backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Date expiresAt;
}
