package com.voidkey.backend.form;

import com.voidkey.backend.form.dto.CreateFormRequest;
import com.voidkey.backend.form.dto.FormResponse;
import com.voidkey.backend.form.dto.FormSummaryResponse;
import com.voidkey.backend.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forms")
@RequiredArgsConstructor
public class FormController {

    private final FormService formService;

    @PostMapping
    public ResponseEntity<FormSummaryResponse> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateFormRequest request
    ) {
        Form form = formService.createForm(user, request.title(), request.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(FormSummaryResponse.from(form));
    }

    @GetMapping
    public List<FormSummaryResponse> listMine(@AuthenticationPrincipal User user) {
        return formService.getFormsForOwner(user).stream()
                .map(FormSummaryResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public FormResponse getOne(@AuthenticationPrincipal User user, @PathVariable Long id) {
        Form form = formService.getFormWithQuestions(id, user);
        return FormResponse.from(form);
    }
}