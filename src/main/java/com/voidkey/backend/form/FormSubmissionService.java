package com.voidkey.backend.form;

import com.voidkey.backend.logic.LogicEvaluator;
import com.voidkey.backend.search.AnswerDocument;
import com.voidkey.backend.search.AnswerSearchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class FormSubmissionService {

    private final QuestionRepository questionRepository;
    private final SubmittedResponseRepository responseRepository;
    private final AnswerSearchRepository answerSearchRepository;

    public FormSubmissionService(QuestionRepository questionRepository,
                                 SubmittedResponseRepository responseRepository,
                                 AnswerSearchRepository answerSearchRepository) {
        this.questionRepository = questionRepository;
        this.responseRepository = responseRepository;
        this.answerSearchRepository = answerSearchRepository;
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

        SubmittedResponse saved = responseRepository.save(response);

        // Index into Elasticsearch AFTER the Postgres save, so each Answer has a
        // real generated id. Indexing failure here shouldn't roll back the DB save —
        // search is a secondary concern, not the source of truth.
        for (Answer answer : saved.getAnswers()) {
            AnswerDocument doc = new AnswerDocument(
                    answer.getId().toString(),
                    form.getId(),
                    answer.getQuestion().getId(),
                    answer.getQuestion().getLabel(),
                    answer.getValue()
            );
            answerSearchRepository.save(doc);
        }

        return saved;
    }
}