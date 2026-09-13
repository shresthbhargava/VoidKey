package com.voidkey.backend.form.dto;

import com.voidkey.backend.form.SubmittedResponse;

import java.time.Instant;
import java.util.List;

public record SubmittedResponseView(Long id, Instant submittedAt, List<AnswerResponse> answers) {
    public static SubmittedResponseView from(SubmittedResponse response) {
        List<AnswerResponse> answers = response.getAnswers().stream()
                .map(AnswerResponse::from)
                .toList();

        return new SubmittedResponseView(response.getId(), response.getSubmittedAt(), answers);
    }
}