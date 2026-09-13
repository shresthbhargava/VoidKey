package com.voidkey.backend.form;

import com.voidkey.backend.user.User;
import org.springframework.stereotype.Service;

import com.voidkey.backend.logic.LogicEvaluator;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FormService {

    private final FormRepository formRepository;
    private final QuestionRepository questionRepository;

    public FormService(FormRepository formRepository, QuestionRepository questionRepository) {
        this.formRepository = formRepository;
        this.questionRepository = questionRepository;
    }

    public Form getFormWithQuestionsPublic(Long formId) {
        return formRepository.findByIdWithQuestions(formId)
                .orElseThrow(() -> new FormNotFoundException(formId));
    }
    public Form createForm(User owner, String title, String description) {
        Form form = Form.builder()
                .owner(owner)
                .title(title)
                .description(description)
                .build();
        return formRepository.save(form);
    }

    public Form getFormOwnedBy(Long formId, User user) {
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new FormNotFoundException(formId));
        checkOwnership(form, user);
        return form;
    }

    public Form getFormWithQuestions(Long formId, User user) {
        Form form = formRepository.findByIdWithQuestions(formId)
                .orElseThrow(() -> new FormNotFoundException(formId));
        checkOwnership(form, user);
        return form;
    }

    private void checkOwnership(Form form, User user) {
        if (!form.getOwner().getId().equals(user.getId())) {
            throw new FormAccessDeniedException(form.getId());
        }
    }

    public List<Form> getFormsForOwner(User owner) {
        return formRepository.findByOwnerId(owner.getId());
    }

    public Question addQuestion(Long formId, User owner, QuestionType type, String label, boolean required) {
        Form form = getFormOwnedBy(formId, owner);

        int nextOrderIndex = (int) questionRepository.countByFormId(formId);

        Question question = new Question();
        question.setForm(form);
        question.setType(type);
        question.setLabel(label);
        question.setRequired(required);
        question.setOrderIndex(nextOrderIndex);

        return questionRepository.save(question);
    }

    @Transactional
    public Question setQuestionLogic(Long formId, Long questionId, User owner, LogicEvaluator.LogicNode logicNode) {
        getFormOwnedBy(formId, owner);

        Question question = questionRepository.findByIdAndFormId(questionId, formId)
                .orElseThrow(() -> new QuestionNotFoundException(questionId));

        question.setLogicJson(logicNode);
        return questionRepository.save(question);
    }
}