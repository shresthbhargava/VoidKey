package com.voidkey.backend.form;

import org.springframework.stereotype.Service;
import com.voidkey.backend.logic.LogicEvaluator;

import java.util.HashMap;
import java.util.Map;

@Service
public class FormSubmissionService {

    public Map<Long, Boolean> evaluateVisibility(Form form, Map<Long, String> submittedAnswers) {
        Map<Long, Boolean> visibilityMap = new HashMap<>();
        Map<Long, String> effectiveAnswers = new HashMap<>(submittedAnswers);

        for (Question question : form.getQuestions()) {
            LogicEvaluator.LogicNode logic = question.getLogicJson();

            boolean isVisible;
            if (logic == null) {
                isVisible = true;
            } else {
                isVisible = LogicEvaluator.evaluateNode(logic, effectiveAnswers);
            }

            visibilityMap.put(question.getId(), isVisible);

            if (!isVisible) {
                effectiveAnswers.remove(question.getId());
            }
        }

        return visibilityMap;
    }
}