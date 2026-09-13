package com.voidkey.backend.form;

import com.voidkey.backend.form.dto.FormSubmissionResponse;
import com.voidkey.backend.form.dto.SubmitFormRequest;
import com.voidkey.backend.form.dto.SubmittedResponseView;
import com.voidkey.backend.user.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/forms")
public class FormSubmissionController {

    private final FormService formService;
    private final FormSubmissionService formSubmissionService;
    private final SubmittedResponseRepository responseRepository;

    public FormSubmissionController(FormService formService, FormSubmissionService formSubmissionService,
                                    SubmittedResponseRepository responseRepository) {
        this.formService = formService;
        this.formSubmissionService = formSubmissionService;
        this.responseRepository = responseRepository;
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

        formSubmissionService.saveSubmission(form, effectiveAnswers);

        FormSubmissionResponse response = new FormSubmissionResponse(formId, effectiveAnswers, visibilityMap);
        return ResponseEntity.ok(response);
    }

    // Owner-only: viewing responses is a different security boundary than submitting them.
    @GetMapping("/{formId}/responses")
    public List<SubmittedResponseView> getResponses(
            @PathVariable Long formId,
            @AuthenticationPrincipal User currentUser) {

        formService.getFormOwnedBy(formId, currentUser); // enforces ownership; throws if not owner

        return responseRepository.findByFormIdWithAnswers(formId).stream()
                .map(SubmittedResponseView::from)
                .toList();
    }
}