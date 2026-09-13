package com.voidkey.backend.form.dto;

import com.voidkey.backend.form.Form;

import java.time.Instant;

public record FormResponse(Long id, String title, String description, String status, Instant createdAt) {

    // A static factory method for mapping entity -> DTO. Why not a constructor taking
    // a Form directly: DTOs should stay dumb data carriers with zero knowledge of your
    // entity classes. This method lives on the DTO side (not the entity) so Form never
    // has to import or know about FormResponse — dependencies point one way only.
    public static FormResponse from(Form form) {
        return new FormResponse(
                form.getId(),
                form.getTitle(),
                form.getDescription(),
                form.getStatus().name(),
                form.getCreatedAt()
        );
    }
}