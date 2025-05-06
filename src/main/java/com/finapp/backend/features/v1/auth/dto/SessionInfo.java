package com.finapp.backend.features.v1.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SessionInfo {
    private String sessionId;
    private String createdAt;
    private String ipAddress;
    private String deviceInfo;
    private boolean isCurrentSession;
}
