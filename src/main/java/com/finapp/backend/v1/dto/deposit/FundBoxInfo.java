package com.finapp.backend.v1.dto.deposit;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class FundBoxInfo {
    private UUID id;
    private String name;
}
