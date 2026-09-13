package com.voidkey.backend.form;

public class FormAccessDeniedException extends RuntimeException {
    public FormAccessDeniedException(Long formId) {
        super("You do not have access to form: " + formId);
    }
}