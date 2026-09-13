package com.voidkey.backend.form.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record SubmitFormRequest(
        @NotNull(message = "Answers map cannot be null")
        Map<Long, String> answers
) {}