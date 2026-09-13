package com.voidkey.backend.form.dto;

import com.voidkey.backend.form.Answer;

public record AnswerResponse(Long questionId, String questionLabel, String value) {
    public static AnswerResponse from(Answer answer) {
        return new AnswerResponse(
                answer.getQuestion().getId(),
                answer.getQuestion().getLabel(),
                answer.getValue()
        );
    }
}