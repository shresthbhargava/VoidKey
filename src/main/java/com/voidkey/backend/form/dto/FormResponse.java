package com.voidkey.backend.form.dto;

import com.voidkey.backend.form.Form;

import java.time.Instant;
import java.util.List;

public record FormResponse(
        Long id, String title, String description, String status,
        Instant createdAt, List<QuestionResponse> questions
) {
    public static FormResponse from(Form form) {
        List<QuestionResponse> questions = form.getQuestions().stream()
                .map(QuestionResponse::from)
                .toList();

        return new FormResponse(
                form.getId(), form.getTitle(), form.getDescription(),
                form.getStatus().name(), form.getCreatedAt(), questions
        );
    }
}