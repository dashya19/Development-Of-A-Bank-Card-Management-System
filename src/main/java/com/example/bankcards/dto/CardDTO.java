package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CardDTO {
    private Long id;
    private String cardNumber;
    private String cardHolder;
    private String expiryDate;
    private CardStatus status;
    private BigDecimal balance;
}
