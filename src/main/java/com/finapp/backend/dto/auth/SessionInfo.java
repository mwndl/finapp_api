package com.finapp.backend.dto.auth;

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
