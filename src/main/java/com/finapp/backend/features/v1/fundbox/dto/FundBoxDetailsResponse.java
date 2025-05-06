package com.finapp.backend.features.v1.fundbox.dto;

import com.finapp.backend.features.v1.user.dto.InviteResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class FundBoxDetailsResponse {
    private UUID id;
    private String name;
    private BigDecimal financialGoal;
    private LocalDate targetDate;
    private BigDecimal balance;
    private OwnerResponse owner;
    private List<CollaboratorResponse> collaborators;
    private List<InviteResponse> invites;
    private Page<FundboxDepositResponse> deposits;

}