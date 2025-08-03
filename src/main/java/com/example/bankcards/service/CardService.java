package com.example.bankcards.service;

import com.example.bankcards.dto.CardDTO;
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
        User user = getUserByUsername(userDetails.getUsername());
        Card card = cardRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + id));

        if (card.getStatus() == CardStatus.ACTIVE) {
            throw new CardOperationException("Card is already active");
        }

        if (isCardExpired(card.getExpiryDate())) {
            throw new CardOperationException("Cannot activate expired card");
        }

        card.setStatus(CardStatus.ACTIVE);
        Card savedCard = cardRepository.save(card);
        return convertToDTO(savedCard);
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
        LocalDate cardDate = LocalDate.parse("01/" + expiryDate, formatter);
        return cardDate.isBefore(LocalDate.now());
    }
}
