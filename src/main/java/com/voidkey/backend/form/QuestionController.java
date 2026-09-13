package com.voidkey.backend.form;

import com.voidkey.backend.form.dto.QuestionResponse;
import com.voidkey.backend.user.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forms/{formId}/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    public record CreateQuestionRequest(
            @NotNull(message = "Type is required") QuestionType type,
            @NotBlank(message = "Label cannot be blank") String label,
            boolean required
    ) {}

    public record UpdateQuestionRequest(
            @NotBlank(message = "Label cannot be blank") String label,
            boolean required
    ) {}

    public record ReorderQuestionsRequest(
            @NotNull List<Long> orderedQuestionIds
    ) {}

    @PostMapping
    public ResponseEntity<QuestionResponse> addQuestion(
            @PathVariable Long formId,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateQuestionRequest request) {

        Question question = questionService.addQuestion(
                formId, currentUser, request.type(), request.label(), request.required()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(QuestionResponse.from(question));
    }

    @PutMapping("/{questionId}")
    public ResponseEntity<QuestionResponse> updateQuestion(
            @PathVariable Long formId,
            @PathVariable Long questionId,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateQuestionRequest request) {

        Question updated = questionService.updateQuestion(
                formId, questionId, currentUser, request.label(), request.required()
        );
        return ResponseEntity.ok(QuestionResponse.from(updated));
    }

    @PutMapping("/reorder")
    public ResponseEntity<Void> reorderQuestions(
            @PathVariable Long formId,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ReorderQuestionsRequest request) {

        questionService.reorderQuestions(formId, currentUser, request.orderedQuestionIds());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long formId,
            @PathVariable Long questionId,
            @AuthenticationPrincipal User currentUser) {

        questionService.deleteQuestion(formId, questionId, currentUser);
        return ResponseEntity.noContent().build();
    }
}