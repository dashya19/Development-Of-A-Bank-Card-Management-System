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

    // Методы для администратора
    public Page<CardDTO> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable).map(this::convertToDTO);
    }

    public CardDTO getAnyCardById(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Карта с id не найдена: " + id));
        return convertToDTO(card);
    }

    public CardDTO createCardForUser(CardDTO cardDTO, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с id не найден: " + userId));

        if (cardRepository.existsByCardNumber(encryptionUtils.encrypt(cardDTO.getCardNumber()))) {
            throw new CardAlreadyExistsException("Карта с этим номером уже существует");
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

    public CardDTO adminBlockCard(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Карта с id не найдена: " + id));

        card.setStatus(CardStatus.BLOCKED);
        Card savedCard = cardRepository.save(card);
        return convertToDTO(savedCard);
    }

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
                .orElseThrow(() -> new ResourceNotFoundException("Карта с id не найдена: " + id));
        return convertToDTO(card);
    }

    public CardDTO createCard(CardDTO cardDTO, UserDetails userDetails) {
        User user = getUserByUsername(userDetails.getUsername());

        if (cardRepository.existsByCardNumber(encryptionUtils.encrypt(cardDTO.getCardNumber()))) {
            throw new CardAlreadyExistsException("Карта с этим номером уже существует");
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
                .orElseThrow(() -> new ResourceNotFoundException("Карта с id не найдена: " + transferDTO.getFromCardId()));

        Card toCard = cardRepository.findByIdAndUser(transferDTO.getToCardId(), user)
                .orElseThrow(() -> new ResourceNotFoundException("Карта с id не найдена: " + transferDTO.getToCardId()));

        if (fromCard.getStatus() != CardStatus.ACTIVE || toCard.getStatus() != CardStatus.ACTIVE) {
            throw new CardNotActiveException("Одна или обе карты не активны");
        }

        if (fromCard.getBalance().compareTo(transferDTO.getAmount()) < 0) {
            throw new InsufficientFundsException("Недостаточно средств на исходной карте");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(transferDTO.getAmount()));
        toCard.setBalance(toCard.getBalance().add(transferDTO.getAmount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }

    public CardDTO blockCard(Long id, UserDetails userDetails) {
        User user = getUserByUsername(userDetails.getUsername());
        Card card = cardRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Карта с id не найдена: " + id));

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new CardOperationException("Карта уже заблокирована");
        }

        card.setStatus(CardStatus.BLOCKED);
        Card savedCard = cardRepository.save(card);
        return convertToDTO(savedCard);
    }

    public CardDTO activateCard(Long id, UserDetails userDetails) {
        try {
            log.info("Попытка активировать id карты: {} пользователя: {}", id, userDetails.getUsername());

            User user = getUserByUsername(userDetails.getUsername());
            log.info("Пользователь найден: {}", user.getId());

            Card card = cardRepository.findByIdAndUser(id, user)
                    .orElseThrow(() -> {
                        log.error("Не найдена карточка с id: {} для пользователя: {}", id, user.getUsername());
                        return new ResourceNotFoundException("Карта с id не найдена: " + id);
                    });

            log.info("Текущий статус карты: {}", card.getStatus());
            log.info("Дата истечения срока действия карты: {}", card.getExpiryDate());

            if (card.getStatus() == CardStatus.ACTIVE) {
                log.warn("Карта уже активна");
                throw new CardOperationException("Карта уже активна");
            }

            if (isCardExpired(card.getExpiryDate())) {
                log.warn("Срок действия карты истек: {}", card.getExpiryDate());
                throw new CardOperationException("Не удается активировать карту с истекшим сроком действия");
            }

            card.setStatus(CardStatus.ACTIVE);
            Card savedCard = cardRepository.save(card);
            log.info("Карта успешно активирована");
            return convertToDTO(savedCard);
        } catch (Exception e) {
            log.error("Ошибка при активации карты: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void deleteCard(Long id, UserDetails userDetails) {
        User user = getUserByUsername(userDetails.getUsername());
        Card card = cardRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Карта с id не найдена: " + id));
        cardRepository.delete(card);
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с именем пользователя не найден: " + username));
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
                throw new CardOperationException("Неверный формат даты истечения срока действия. Ожидаемый срок годности, ММ/гг");
            }

            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]) + 2000; // Преобразуем 2-значный год в 4-значный

            // Получаем последний день месяца
            LocalDate expiry = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1);
            return expiry.isBefore(LocalDate.now());
        } catch (NumberFormatException e) {
            throw new CardOperationException("Недопустимый формат даты истечения срока действия", e);
        }
    }

    @Transactional
    public CardDTO topUpCard(TopUpDTO topUpDTO, UserDetails userDetails) {
        log.info("Начало пополнения счета для идентификатора карты: {}, суммы: {}, пользователя: {}",
                topUpDTO.getCardId(), topUpDTO.getAmount(), userDetails.getUsername());

        try {
            User user = getUserByUsername(userDetails.getUsername());
            log.info("Пользователь найден: {}", user.getId());

            Card card = cardRepository.findByIdAndUser(topUpDTO.getCardId(), user)
                    .orElseThrow(() -> {
                        log.error("Карта не найдена: {}", topUpDTO.getCardId());
                        return new ResourceNotFoundException("Карта с id не найдена: " + topUpDTO.getCardId());
                    });

            log.info("Найдена карта: {}, текущий баланс: {}", card.getId(), card.getBalance());

            if (card.getStatus() != CardStatus.ACTIVE) {
                log.warn("Карта не активна: {}", card.getStatus());
                throw new CardNotActiveException("Невозможно пополнить счет с неактивной карты");
            }

            if (topUpDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("Недопустимая сумма: {}", topUpDTO.getAmount());
                throw new CardOperationException("Сумма должна быть положительной");
            }

            card.setBalance(card.getBalance().add(topUpDTO.getAmount()));
            Card savedCard = cardRepository.save(card);
            log.info("Успешное пополнение счета, новый баланс: {}", savedCard.getBalance());

            return convertToDTO(savedCard);
        } catch (Exception e) {
            log.error("Ошибка при пополнении счета", e);
            throw e;
        }
    }

    public CardDTO adminActivateCard(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Карта с идентификатором не найдена: " + id));

        if (isCardExpired(card.getExpiryDate())) {
            throw new CardOperationException("Не удается активировать карту с истекшим сроком действия");
        }

        card.setStatus(CardStatus.ACTIVE);
        Card savedCard = cardRepository.save(card);
        return convertToDTO(savedCard);
    }

    public void adminDeleteCard(Long id) {
        if (!cardRepository.existsById(id)) {
            throw new ResourceNotFoundException("Карта с id не найдена: " + id);
        }
        cardRepository.deleteById(id);
    }

    // Новые методы для пользователя
    public CardDTO getUserCardById(Long id, UserDetails userDetails) {
        User user = getUserByUsername(userDetails.getUsername());
        Card card = cardRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Карта с id не найдена: " + id));
        return convertToDTO(card);
    }

    @Transactional
    public void transferBetweenUserCards(TransferDTO transferDTO, UserDetails userDetails) {
        User user = getUserByUsername(userDetails.getUsername());

        Card fromCard = cardRepository.findByIdAndUser(transferDTO.getFromCardId(), user)
                .orElseThrow(() -> new ResourceNotFoundException("Карта с id не найдена: " + transferDTO.getFromCardId()));

        Card toCard = cardRepository.findByIdAndUser(transferDTO.getToCardId(), user)
                .orElseThrow(() -> new ResourceNotFoundException("Карта с id не найдена: " + transferDTO.getToCardId()));

        if (fromCard.getStatus() != CardStatus.ACTIVE || toCard.getStatus() != CardStatus.ACTIVE) {
            throw new CardNotActiveException("Одна или обе карты не активны");
        }

        if (fromCard.getBalance().compareTo(transferDTO.getAmount()) < 0) {
            throw new InsufficientFundsException("Недостаточно средств на исходной карте");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(transferDTO.getAmount()));
        toCard.setBalance(toCard.getBalance().add(transferDTO.getAmount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }

    public void requestBlockCard(Long id, UserDetails userDetails) {
        User user = getUserByUsername(userDetails.getUsername());
        Card card = cardRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Карта с id не найдена: " + id));

        // Здесь можно добавить логику отправки запроса на блокировку
        // Например, отправить уведомление администратору
        log.info("Пользователь {} запросил заблокировать карту {}", user.getUsername(), id);
    }

    @Transactional
    public CardDTO topUpUserCard(TopUpDTO topUpDTO, UserDetails userDetails) {
        User user = getUserByUsername(userDetails.getUsername());
        Card card = cardRepository.findByIdAndUser(topUpDTO.getCardId(), user)
                .orElseThrow(() -> new ResourceNotFoundException("Карта с id не найдена: " + topUpDTO.getCardId()));

        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new CardNotActiveException("Невозможно пополнить счет с неактивной карты");
        }

        if (topUpDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CardOperationException("Сумма должна быть положительной");
        }

        card.setBalance(card.getBalance().add(topUpDTO.getAmount()));
        Card savedCard = cardRepository.save(card);
        return convertToDTO(savedCard);
    }
}
