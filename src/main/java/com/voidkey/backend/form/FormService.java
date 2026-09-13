package com.voidkey.backend.form;

import com.voidkey.backend.user.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FormService {

    private final FormRepository formRepository;
    private final QuestionRepository questionRepository;

    public FormService(FormRepository formRepository, QuestionRepository questionRepository) {
        this.formRepository = formRepository;
        this.questionRepository = questionRepository;
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

        if (!form.getOwner().getId().equals(user.getId())) {
            throw new FormAccessDeniedException(formId);
        }

        return form;
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
}