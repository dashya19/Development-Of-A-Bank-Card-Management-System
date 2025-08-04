package com.example.bankcards.exception;

public class CardOperationException extends RuntimeException {
    public CardOperationException(String message) {
        super(message);
    }
    // Добавьте этот конструктор
    public CardOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
