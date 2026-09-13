package com.voidkey.backend.form;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.voidkey.backend.logic.LogicEvaluator;

import java.util.HashMap;
import java.util.Map;

@Service
public class FormSubmissionService {

    private final SubmittedResponseRepository responseRepository;
    private final QuestionRepository questionRepository;

    public FormSubmissionService(SubmittedResponseRepository responseRepository, QuestionRepository questionRepository) {
        this.responseRepository = responseRepository;
        this.questionRepository = questionRepository;
    }

    public Map<Long, Boolean> evaluateVisibility(Form form, Map<Long, String> submittedAnswers) {
        Map<Long, Boolean> visibilityMap = new HashMap<>();
        Map<Long, String> effectiveAnswers = new HashMap<>(submittedAnswers);

        for (Question question : form.getQuestions()) {
            LogicEvaluator.LogicNode logic = question.getLogicJson();

            boolean isVisible = (logic == null) || LogicEvaluator.evaluateNode(logic, effectiveAnswers);
            visibilityMap.put(question.getId(), isVisible);

            if (!isVisible) {
                effectiveAnswers.remove(question.getId());
            }
        }

        return visibilityMap;
    }

    // Why @Transactional here specifically: we're creating one SubmittedResponse and
    // multiple Answer rows as a single logical operation. If saving answer 3 of 5 fails
    // partway through, we don't want a half-saved submission sitting in the database —
    // the transaction rolls back everything in this method as one atomic unit.
    @Transactional
    public SubmittedResponse saveSubmission(Form form, Map<Long, String> effectiveAnswers) {
        SubmittedResponse response = SubmittedResponse.builder()
                .form(form)
                .build();

        for (Map.Entry<Long, String> entry : effectiveAnswers.entrySet()) {
            Question question = questionRepository.findById(entry.getKey())
                    .orElseThrow(() -> new QuestionNotFoundException(entry.getKey()));

            Answer answer = Answer.builder()
                    .response(response)
                    .question(question)
                    .value(entry.getValue())
                    .build();

            response.getAnswers().add(answer);
        }

        return responseRepository.save(response);
    }
}