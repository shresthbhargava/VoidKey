package com.voidkey.backend.form;

public class QuestionNotFoundException extends RuntimeException {
    public QuestionNotFoundException(Long questionId) {
        super("No question with id: " + questionId);
    }
}