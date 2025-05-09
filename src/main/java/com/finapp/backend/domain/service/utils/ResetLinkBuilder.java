package com.finapp.backend.domain.service.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ResetLinkBuilder {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public String buildResetPasswordLink(String token) {
        return String.format("%s/reset-password?token=%s", frontendUrl, token);
    }
}

