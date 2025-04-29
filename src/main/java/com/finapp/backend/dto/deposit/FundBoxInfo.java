package com.finapp.backend.dto.deposit;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class FundBoxInfo {
    private UUID id;
    private String name;
}
