package com.voidkey.backend.form.dto;

import com.voidkey.backend.form.Form;

import java.time.Instant;

public record FormSummaryResponse(Long id, String title, String description, String status, Instant createdAt) {
    public static FormSummaryResponse from(Form form) {
        return new FormSummaryResponse(
                form.getId(), form.getTitle(), form.getDescription(),
                form.getStatus().name(), form.getCreatedAt()
        );
    }
}