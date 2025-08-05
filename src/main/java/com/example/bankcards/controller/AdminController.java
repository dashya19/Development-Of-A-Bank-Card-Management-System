package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    private final CardService cardService;
    private final UserService userService;

    @GetMapping("/cards")
    public ResponseEntity<Page<CardDTO>> getAllCards(Pageable pageable) {
        return ResponseEntity.ok(cardService.getAllCards(pageable));
    }

    @GetMapping("/cards/{id}")
    public ResponseEntity<CardDTO> getAnyCardById(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getAnyCardById(id));
    }

    @PostMapping("/cards")
    public ResponseEntity<CardDTO> createCardForUser(@RequestBody CardDTO cardDTO, @RequestParam Long userId) {
        return ResponseEntity.ok(cardService.createCardForUser(cardDTO, userId));
    }

    @PutMapping("/cards/{id}/block")
    public ResponseEntity<CardDTO> adminBlockCard(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.adminBlockCard(id));
    }

    @PutMapping("/cards/{id}/activate")
    public ResponseEntity<CardDTO> adminActivateCard(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.adminActivateCard(id));
    }

    @DeleteMapping("/cards/{id}")
    public ResponseEntity<Void> adminDeleteCard(@PathVariable Long id) {
        cardService.adminDeleteCard(id);
        return ResponseEntity.noContent().build();
    }

    // Методы управления пользователями (из вашего UserController)
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
