package com.finapp.backend.features.v1.auth.dto;

import lombok.Data;

@Data
public class ForgotPasswordRequest {
    private String email;
}