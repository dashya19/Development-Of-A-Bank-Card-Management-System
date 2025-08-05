package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void authenticateUser_Success() {
        AuthRequest request = new AuthRequest();
        request.setUsername("test");
        request.setPassword("password");

        AuthResponse response = new AuthResponse("token");
        when(authService.authenticateUser(request)).thenReturn(response);

        ResponseEntity<AuthResponse> result = authController.authenticateUser(request);

        assertEquals(200, result.getStatusCodeValue());
        assertEquals(response, result.getBody());
        verify(authService).authenticateUser(request);
    }

    @Test
    void registerUser_Success() {
        AuthRequest request = new AuthRequest();
        request.setUsername("test");
        request.setPassword("password");
        request.setEmail("test@example.com");

        ResponseEntity<Void> result = authController.registerUser(request);

        assertEquals(200, result.getStatusCodeValue());
        verify(authService).registerUser(request);
    }
}