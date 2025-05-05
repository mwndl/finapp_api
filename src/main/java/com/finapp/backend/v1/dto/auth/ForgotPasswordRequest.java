package com.finapp.backend.v1.dto.auth;

import lombok.Data;

@Data
public class ForgotPasswordRequest {
    private String email;
}