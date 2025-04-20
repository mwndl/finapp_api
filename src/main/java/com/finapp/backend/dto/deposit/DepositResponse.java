package com.finapp.backend.dto.deposit;

import com.finapp.backend.dto.fundbox.OwnerResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DepositResponse {
    private Long id;
    private BigDecimal amount;
    private LocalDate date;
    private String description;
    private FundBoxInfo fundBox;
    private String TransactionType;
    private OwnerResponse owner;
}
