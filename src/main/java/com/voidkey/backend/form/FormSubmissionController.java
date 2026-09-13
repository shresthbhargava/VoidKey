package com.voidkey.backend.form;

import com.voidkey.backend.form.dto.FormSubmissionResponse;
import com.voidkey.backend.form.dto.SubmitFormRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/forms")
public class FormSubmissionController {

    private final FormService formService;
    private final FormSubmissionService formSubmissionService;

    public FormSubmissionController(FormService formService, FormSubmissionService formSubmissionService) {
        this.formService = formService;
        this.formSubmissionService = formSubmissionService;
    }

    @PostMapping("/{formId}/submit")
    public ResponseEntity<FormSubmissionResponse> submitForm(
            @PathVariable Long formId,
            @Valid @RequestBody SubmitFormRequest request) {

        Form form = formService.getFormWithQuestionsPublic(formId);
        Map<Long, Boolean> visibilityMap = formSubmissionService.evaluateVisibility(form, request.answers());

        Map<Long, String> effectiveAnswers = request.answers().entrySet().stream()
                .filter(entry -> visibilityMap.getOrDefault(entry.getKey(), true))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        FormSubmissionResponse response = new FormSubmissionResponse(formId, effectiveAnswers, visibilityMap);
        return ResponseEntity.ok(response);
    }
}