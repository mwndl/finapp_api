package com.finapp.backend.dto.user;

import com.finapp.backend.dto.fundbox.FundBoxSummary;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InviteResponse {
    private Long inviteId;
    private FundBoxSummary fundBox;
    private UserSummary inviter;
    private UserSummary invitee;
    private String status;
    private LocalDateTime invitationDate;
}