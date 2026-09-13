package com.voidkey.backend.form.dto;

import com.voidkey.backend.form.Question;

public record QuestionResponse(Long id, String type, String label, boolean required, int orderIndex) {
    public static QuestionResponse from(Question question) {
        return new QuestionResponse(
                question.getId(),
                question.getType().name(),
                question.getLabel(),
                question.isRequired(),
                question.getOrderIndex()
        );
    }
}