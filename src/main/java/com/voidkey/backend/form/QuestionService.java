package com.voidkey.backend.form;

import com.voidkey.backend.user.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private final FormService formService;
    private final QuestionRepository questionRepository;

    public QuestionService(FormService formService, QuestionRepository questionRepository) {
        this.formService = formService;
        this.questionRepository = questionRepository;
    }

    public Question addQuestion(Long formId, User owner, QuestionType type, String label, boolean required) {
        Form form = formService.getFormOwnedBy(formId, owner);
        int nextOrderIndex = (int) questionRepository.countByFormId(formId);

        Question question = new Question();
        question.setForm(form);
        question.setType(type);
        question.setLabel(label);
        question.setRequired(required);
        question.setOrderIndex(nextOrderIndex);

        return questionRepository.save(question);
    }

    public Question updateQuestion(Long formId, Long questionId, User owner, String label, boolean required) {
        formService.getFormOwnedBy(formId, owner); // enforces ownership; throws if not owner

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException(questionId));

        // Defense against a mismatched formId/questionId pair in the URL — e.g. a user
        // owns form 5 but passes questionId belonging to form 9. Ownership of the FORM
        // alone doesn't prove this question belongs to it.
        if (!question.getForm().getId().equals(formId)) {
            throw new QuestionNotFoundException(questionId);
        }

        question.setLabel(label);
        question.setRequired(required);
        return questionRepository.save(question);
    }

    public void deleteQuestion(Long formId, Long questionId, User owner) {
        formService.getFormOwnedBy(formId, owner);

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException(questionId));

        if (!question.getForm().getId().equals(formId)) {
            throw new QuestionNotFoundException(questionId);
        }

        questionRepository.delete(question);
    }

    public void reorderQuestions(Long formId, User owner, List<Long> orderedQuestionIds) {
        formService.getFormOwnedBy(formId, owner);

        List<Question> existing = questionRepository.findByFormIdOrderByOrderIndexAsc(formId);

        Set<Long> existingIds = existing.stream().map(Question::getId).collect(Collectors.toSet());
        Set<Long> submittedIds = Set.copyOf(orderedQuestionIds);

        // Reject silently-wrong input up front rather than partially applying it —
        // a client sending a mismatched list (missing/extra/duplicate IDs) means
        // something is out of sync, and half-applying a reorder would leave the form
        // in a worse state than rejecting outright.
        if (!existingIds.equals(submittedIds) || existingIds.size() != orderedQuestionIds.size()) {
            throw new InvalidReorderException(
                    "Submitted question IDs do not exactly match this form's existing questions"
            );
        }

        for (int i = 0; i < orderedQuestionIds.size(); i++) {
            Long id = orderedQuestionIds.get(i);
            Question q = existing.stream()
                    .filter(question -> question.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new QuestionNotFoundException(id)); // unreachable given the check above, but keeps the compiler/reader honest
            q.setOrderIndex(i);
        }

        questionRepository.saveAll(existing);
    }
}