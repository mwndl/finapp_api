package com.finapp.backend.domain.model.enums;

public enum InvestmentSubtype {
    TESOURO_SELIC(InvestmentType.FIXED_INCOME),
    TESOURO_PREFIXADO(InvestmentType.FIXED_INCOME),
    CDB(InvestmentType.FIXED_INCOME),
    LCI(InvestmentType.FIXED_INCOME),
    LCA(InvestmentType.FIXED_INCOME);
    // add more in the future

    private final InvestmentType parentType;

    InvestmentSubtype(InvestmentType parentType) {
        this.parentType = parentType;
    }

    public InvestmentType getParentType() {
        return parentType;
    }
}

