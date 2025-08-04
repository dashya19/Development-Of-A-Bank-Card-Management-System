package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.TopUpDTO;
import com.example.bankcards.dto.TransferDTO;
import com.example.bankcards.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping
    public ResponseEntity<Page<CardDTO>> getUserCards(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        return ResponseEntity.ok(cardService.getUserCards(userDetails, pageable));
    }

    @GetMapping("/all")
    public ResponseEntity<List<CardDTO>> getAllUserCards(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cardService.getAllUserCards(userDetails));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardDTO> getCardById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cardService.getCardById(id, userDetails));
    }

    @PostMapping
    public ResponseEntity<CardDTO> createCard(
            @RequestBody CardDTO cardDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cardService.createCard(cardDTO, userDetails));
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transferBetweenCards(
            @RequestBody TransferDTO transferDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        cardService.transferBetweenCards(transferDTO, userDetails);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/block")
    public ResponseEntity<CardDTO> blockCard(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cardService.blockCard(id, userDetails));
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<CardDTO> activateCard(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cardService.activateCard(id, userDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        cardService.deleteCard(id, userDetails);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/top-up")
    public ResponseEntity<CardDTO> topUpCard(
            @RequestBody TopUpDTO topUpDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cardService.topUpCard(topUpDTO, userDetails));
    }
}
