package com.example.bankcards.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferDTO {
    private Long fromCardId;
    private Long toCardId;
    private BigDecimal amount;
}
