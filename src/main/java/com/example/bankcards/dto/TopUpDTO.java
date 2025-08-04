package com.example.bankcards.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TopUpDTO {
    private Long cardId;
    private BigDecimal amount;
}