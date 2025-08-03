package com.example.bankcards.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
    private String email; // только для регистрации
}
