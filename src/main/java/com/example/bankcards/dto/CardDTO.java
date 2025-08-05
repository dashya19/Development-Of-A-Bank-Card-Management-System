package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CardDTO {
    @NotNull
    private Long id;
    @NotBlank
    @Pattern(regexp = "^[0-9]{16}$", message = "Номер карты должен состоять из 16 цифр")
    private String cardNumber;
    private String cardHolder;
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = "Дата истечения срока годности имеет формат ММ/гггг")
    private String expiryDate;
    private CardStatus status;
    private BigDecimal balance;
}
