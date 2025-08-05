package com.example.bankcards.service;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.TopUpDTO;
import com.example.bankcards.dto.TransferDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.*;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.EncryptionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EncryptionUtils encryptionUtils;
    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private CardService cardService;

    @Test
    void getUserCards_ShouldReturnCards() {
        User user = new User();
        Card card = new Card();
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(user));
        when(cardRepository.findAllByUser(any(), any())).thenReturn(new PageImpl<>(Collections.singletonList(card)));

        Page<CardDTO> result = cardService.getUserCards(userDetails, PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void transferBetweenCards_ShouldUpdateBalances() {
        User user = new User();
        Card fromCard = new Card();
        fromCard.setBalance(BigDecimal.valueOf(100));
        fromCard.setStatus(CardStatus.ACTIVE);
        Card toCard = new Card();
        toCard.setStatus(CardStatus.ACTIVE);

        when(userRepository.findByUsername(any())).thenReturn(Optional.of(user));
        when(cardRepository.findByIdAndUser(eq(1L), any())).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndUser(eq(2L), any())).thenReturn(Optional.of(toCard));

        TransferDTO transferDTO = new TransferDTO();
        transferDTO.setFromCardId(1L);
        transferDTO.setToCardId(2L);
        transferDTO.setAmount(BigDecimal.TEN);

        cardService.transferBetweenUserCards(transferDTO, userDetails);

        assertEquals(BigDecimal.valueOf(90), fromCard.getBalance());
        assertEquals(BigDecimal.TEN, toCard.getBalance());
    }

    @Test
    void transferBetweenCards_ShouldThrowWhenInsufficientFunds() {
        User user = new User();
        Card fromCard = new Card();
        fromCard.setBalance(BigDecimal.ONE);
        fromCard.setStatus(CardStatus.ACTIVE);
        Card toCard = new Card();
        toCard.setStatus(CardStatus.ACTIVE);

        when(userRepository.findByUsername(any())).thenReturn(Optional.of(user));
        when(cardRepository.findByIdAndUser(eq(1L), any())).thenReturn(Optional.of(fromCard));

        TransferDTO transferDTO = new TransferDTO();
        transferDTO.setFromCardId(1L);
        transferDTO.setToCardId(2L);
        transferDTO.setAmount(BigDecimal.TEN);

        assertThrows(InsufficientFundsException.class,
                () -> cardService.transferBetweenUserCards(transferDTO, userDetails));
    }

    @Test
    void topUpCard_ShouldIncreaseBalance() {
        User user = new User();
        Card card = new Card();
        card.setBalance(BigDecimal.ZERO);
        card.setStatus(CardStatus.ACTIVE);

        when(userRepository.findByUsername(any())).thenReturn(Optional.of(user));
        when(cardRepository.findByIdAndUser(any(), any())).thenReturn(Optional.of(card));

        TopUpDTO topUpDTO = new TopUpDTO();
        topUpDTO.setCardId(1L);
        topUpDTO.setAmount(BigDecimal.TEN);

        CardDTO result = cardService.topUpUserCard(topUpDTO, userDetails);
        assertNotNull(result);
        assertEquals(BigDecimal.TEN, card.getBalance());
    }
}