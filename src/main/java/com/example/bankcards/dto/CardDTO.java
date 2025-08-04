package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CardDTO {
    private Long id;
    private String cardNumber;
    private String cardHolder;
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = "Expiry date must be in MM/yy format")
    private String expiryDate;
    private CardStatus status;
    private BigDecimal balance;
}
