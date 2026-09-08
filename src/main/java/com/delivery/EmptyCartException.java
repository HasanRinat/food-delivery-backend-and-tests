package com.delivery.exception;

// Наследуемся от RuntimeException, чтобы сделать ошибку Unchecked (необязательной для try-catch)
public class EmptyCartException extends RuntimeException {

    // Конструктор, который принимает текст ошибки и передает его наверх "родителю"
    public String getMessage;

    public EmptyCartException(String message) {
        super(message);
    }
}
