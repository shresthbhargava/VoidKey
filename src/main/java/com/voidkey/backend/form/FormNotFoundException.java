package com.voidkey.backend.form;

public class FormNotFoundException extends RuntimeException {
    public FormNotFoundException(Long formId) {
        super("No form with id: " + formId);
    }
}