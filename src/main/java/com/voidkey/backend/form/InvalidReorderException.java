package com.voidkey.backend.form;

public class InvalidReorderException extends RuntimeException {
    public InvalidReorderException(String message) {
        super(message);
    }
}