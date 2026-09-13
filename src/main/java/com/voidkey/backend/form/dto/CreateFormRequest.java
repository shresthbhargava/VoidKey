package com.voidkey.backend.form.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateFormRequest(
        @NotBlank String title,
        String description
) {}