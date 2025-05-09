package com.finapp.backend.dto.user;

import com.finapp.backend.dto.fundbox.FundBoxSummary;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class InviteResponse {
    private UUID inviteId;
    private FundBoxSummary fundBox;
    private UserSummary inviter;
    private UserSummary invitee;
    private String status;
    private LocalDateTime invitationDate;
}