package com.finapp.backend.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InviteResponse {
    private Long inviteId;
    private Long fundBoxId;
    private String fundBoxName;
    private String status;
    private String inviterName;
    private LocalDateTime invitationDate;
}