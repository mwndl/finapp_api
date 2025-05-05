package com.finapp.backend.dto.fundbox.v1;

import com.finapp.backend.dto.deposit.DepositResponse;
import com.finapp.backend.dto.user.InviteResponse;
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