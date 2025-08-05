package com.example.bankcards.config;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AdminInitializer {
    private final Environment env;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Transactional
    public CommandLineRunner initAdmin(UserRepository userRepository,
                                       RoleRepository roleRepository) {
        return args -> {
            String adminUsername = env.getProperty("app.admin.username");
            String adminPassword = env.getProperty("app.admin.password");
            String adminEmail = env.getProperty("app.admin.email");

            if (adminUsername == null || adminPassword == null || adminEmail == null) {
                return;
            }

            if (userRepository.existsByUsername(adminUsername)) {
                return;
            }

            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN не найден в базе данных"));

            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setEmail(adminEmail);
            admin.setRoles(Set.of(adminRole));

            userRepository.save(admin);
        };
    }
}