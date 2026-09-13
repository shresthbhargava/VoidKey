package com.voidkey.backend.form.dto;

import java.util.Map;

public record FormSubmissionResponse(
        Long formId,
        Map<Long, String> submittedAnswers,
        Map<Long, Boolean> questionVisibility
) {}