package com.example.bankcards.service;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserAlreadyExistsException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    @Test
    void authenticateUser_ShouldReturnToken() {
        AuthRequest request = new AuthRequest();
        request.setUsername("user");
        request.setPassword("pass");

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtTokenProvider.generateToken(any())).thenReturn("token");

        AuthResponse response = authService.authenticateUser(request);
        assertEquals("token", response.getToken());
    }

    @Test
    void registerUser_ShouldSaveUser() {
        AuthRequest request = new AuthRequest();
        request.setUsername("user");
        request.setPassword("pass");
        request.setEmail("user@test.com");

        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(roleRepository.findByName(any())).thenReturn(Optional.of(new Role()));

        authService.registerUser(request);
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void registerUser_ShouldThrowWhenUsernameExists() {
        AuthRequest request = new AuthRequest();
        request.setUsername("user");
        when(userRepository.existsByUsername(any())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.registerUser(request));
    }
}