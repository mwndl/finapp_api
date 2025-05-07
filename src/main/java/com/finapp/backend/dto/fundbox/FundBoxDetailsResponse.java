package com.finapp.backend.dto.fundbox;

import com.finapp.backend.dto.deposit.DepositResponse;
import com.finapp.backend.dto.user.InviteResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Data
public class FundBoxDetailsResponse {
    private UUID id;
    private String name;
    private BigDecimal financialGoal;
    private LocalDate targetDate;
    private BigDecimal balance;
    private OwnerResponse owner;
    private List<CollaboratorResponse> collaborators;
    private List<InviteResponse> invites;
    private List<DepositResponse> deposits;

    public FundBoxDetailsResponse(UUID id, String name, BigDecimal financialGoal, LocalDate targetDate, OwnerResponse ownerResponse, BigDecimal balance, Page<DepositResponse> depositResponses, List<CollaboratorResponse> collaborators, List<InviteResponse> invites) {
        this.id = id;
        this.name = name;
        this.financialGoal = financialGoal;
        this.targetDate = targetDate;
        this.balance = balance;
        this.owner = ownerResponse;
        this.collaborators = collaborators;
        this.invites = invites;
        this.deposits = depositResponses.getContent();
    }
}