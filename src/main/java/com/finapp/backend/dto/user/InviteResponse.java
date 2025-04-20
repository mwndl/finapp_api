package com.finapp.backend.dto.user;

import lombok.Data;

@Data
public class InviteResponse {
    private Long inviteId;
    private Long fundBoxId;
    private String fundBoxName;
    private String status;
    private String inviterName;
}