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
import com.example.bankcards.util.CardMasker;
import com.example.bankcards.util.EncryptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final EncryptionUtils encryptionUtils;

    public Page<CardDTO> getUserCards(UserDetails userDetails, Pageable pageable) {
        User user = getUserByUsername(userDetails.getUsername());
        return cardRepository.findAllByUser(user, pageable)
                .map(this::convertToDTO);
    }

    public List<CardDTO> getAllUserCards(UserDetails userDetails) {
        User user = getUserByUsername(userDetails.getUsername());
        return cardRepository.findAllByUser(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CardDTO getCardById(Long id, UserDetails userDetails) {
        User user = getUserByUsername(userDetails.getUsername());
        Card card = cardRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + id));
        return convertToDTO(card);
    }

    public CardDTO createCard(CardDTO cardDTO, UserDetails userDetails) {
        User user = getUserByUsername(userDetails.getUsername());

        if (cardRepository.existsByCardNumber(encryptionUtils.encrypt(cardDTO.getCardNumber()))) {
            throw new CardAlreadyExistsException("Card with this number already exists");
        }

        Card card = new Card();
        card.setCardNumber(encryptionUtils.encrypt(cardDTO.getCardNumber()));
        card.setCardHolder(cardDTO.getCardHolder());
        card.setExpiryDate(cardDTO.getExpiryDate());
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);
        card.setUser(user);

        Card savedCard = cardRepository.save(card);
        return convertToDTO(savedCard);
    }

    @Transactional
    public void transferBetweenCards(TransferDTO transferDTO, UserDetails userDetails) {
        User user = getUserByUsername(userDetails.getUsername());

        Card fromCard = cardRepository.findByIdAndUser(transferDTO.getFromCardId(), user)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + transferDTO.getFromCardId()));

        Card toCard = cardRepository.findByIdAndUser(transferDTO.getToCardId(), user)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + transferDTO.getToCardId()));

        if (fromCard.getStatus() != CardStatus.ACTIVE || toCard.getStatus() != CardStatus.ACTIVE) {
            throw new CardNotActiveException("One or both cards are not active");
        }

        if (fromCard.getBalance().compareTo(transferDTO.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds on the source card");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(transferDTO.getAmount()));
        toCard.setBalance(toCard.getBalance().add(transferDTO.getAmount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }

    public CardDTO blockCard(Long id, UserDetails userDetails) {
        User user = getUserByUsername(userDetails.getUsername());
        Card card = cardRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + id));

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new CardOperationException("Card is already blocked");
        }

        card.setStatus(CardStatus.BLOCKED);
        Card savedCard = cardRepository.save(card);
        return convertToDTO(savedCard);
    }

    public CardDTO activateCard(Long id, UserDetails userDetails) {
        try {
            log.info("Attempting to activate card ID: {} for user: {}", id, userDetails.getUsername());

            User user = getUserByUsername(userDetails.getUsername());
            log.info("User found: {}", user.getId());

            Card card = cardRepository.findByIdAndUser(id, user)
                    .orElseThrow(() -> {
                        log.error("Card not found with ID: {} for user: {}", id, user.getUsername());
                        return new ResourceNotFoundException("Card not found with id: " + id);
                    });

            log.info("Current card status: {}", card.getStatus());
            log.info("Card expiry date: {}", card.getExpiryDate());

            if (card.getStatus() == CardStatus.ACTIVE) {
                log.warn("Card is already active");
                throw new CardOperationException("Card is already active");
            }

            if (isCardExpired(card.getExpiryDate())) {
                log.warn("Card is expired: {}", card.getExpiryDate());
                throw new CardOperationException("Cannot activate expired card");
            }

            card.setStatus(CardStatus.ACTIVE);
            Card savedCard = cardRepository.save(card);
            log.info("Card activated successfully");
            return convertToDTO(savedCard);
        } catch (Exception e) {
            log.error("Error activating card: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void deleteCard(Long id, UserDetails userDetails) {
        User user = getUserByUsername(userDetails.getUsername());
        Card card = cardRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + id));
        cardRepository.delete(card);
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    private CardDTO convertToDTO(Card card) {
        CardDTO dto = new CardDTO();
        dto.setId(card.getId());
        dto.setCardNumber(CardMasker.maskCardNumber(encryptionUtils.decrypt(card.getCardNumber())));
        dto.setCardHolder(card.getCardHolder());
        dto.setExpiryDate(card.getExpiryDate());
        dto.setStatus(card.getStatus());
        dto.setBalance(card.getBalance());
        return dto;
    }

    private boolean isCardExpired(String expiryDate) {
        try {
            // Разделяем строку на месяц и год
            String[] parts = expiryDate.split("/");
            if (parts.length != 2) {
                throw new CardOperationException("Invalid expiry date format. Expected MM/yy");
            }

            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]) + 2000; // Преобразуем 2-значный год в 4-значный

            // Получаем последний день месяца
            LocalDate expiry = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1);
            return expiry.isBefore(LocalDate.now());
        } catch (NumberFormatException e) {
            throw new CardOperationException("Invalid expiry date format", e);
        }
    }

    @Transactional
    public CardDTO topUpCard(TopUpDTO topUpDTO, UserDetails userDetails) {
        log.info("Starting top-up for card ID: {}, amount: {}, user: {}",
                topUpDTO.getCardId(), topUpDTO.getAmount(), userDetails.getUsername());

        try {
            User user = getUserByUsername(userDetails.getUsername());
            log.info("User found: {}", user.getId());

            Card card = cardRepository.findByIdAndUser(topUpDTO.getCardId(), user)
                    .orElseThrow(() -> {
                        log.error("Card not found: {}", topUpDTO.getCardId());
                        return new ResourceNotFoundException("Card not found with id: " + topUpDTO.getCardId());
                    });

            log.info("Card found: {}, current balance: {}", card.getId(), card.getBalance());

            if (card.getStatus() != CardStatus.ACTIVE) {
                log.warn("Card is not active: {}", card.getStatus());
                throw new CardNotActiveException("Cannot top up a non-active card");
            }

            if (topUpDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("Invalid amount: {}", topUpDTO.getAmount());
                throw new CardOperationException("Amount must be positive");
            }

            card.setBalance(card.getBalance().add(topUpDTO.getAmount()));
            Card savedCard = cardRepository.save(card);
            log.info("Top-up successful, new balance: {}", savedCard.getBalance());

            return convertToDTO(savedCard);
        } catch (Exception e) {
            log.error("Error during top-up", e);
            throw e;
        }
    }
}
