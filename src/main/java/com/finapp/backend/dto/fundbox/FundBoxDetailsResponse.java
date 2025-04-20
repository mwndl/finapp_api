package com.finapp.backend.dto.fundbox;

import com.finapp.backend.dto.deposit.DepositResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@Data
public class FundBoxDetailsResponse {
    private Long id;
    private String name;
    private BigDecimal financialGoal;
    private LocalDate targetDate;
    private BigDecimal balance;
    private OwnerResponse owner;
    private List<CollaboratorResponse> collaborators;
    private List<DepositResponse> deposits;

    public FundBoxDetailsResponse(Long id, String name, BigDecimal financialGoal, LocalDate targetDate, OwnerResponse ownerResponse, BigDecimal balance, Page<DepositResponse> depositResponses, List<CollaboratorResponse> collaborators) {
        this.id = id;
        this.name = name;
        this.financialGoal = financialGoal;
        this.targetDate = targetDate;
        this.balance = balance;
        this.owner = ownerResponse;
        this.collaborators = collaborators;
        this.deposits = depositResponses.getContent();
    }
}