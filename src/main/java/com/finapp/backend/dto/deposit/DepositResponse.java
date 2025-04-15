package com.finapp.backend.dto.deposit;

import com.finapp.backend.dto.fundbox.FundBoxInfo;
import com.finapp.backend.model.enums.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DepositResponse {
    private Long id;
    private BigDecimal amount;
    private LocalDate date;
    private String description;
    private FundBoxInfo fundBox;
    private String TransactionType;
}
