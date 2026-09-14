package com.voidkey.backend.agent.core;

import com.voidkey.backend.agent.classifier.NaiveBayesClassifier;
import com.voidkey.backend.agent.classifier.TrainingExample;
import com.voidkey.backend.agent.nlp.CosineSimilarity;
import com.voidkey.backend.agent.nlp.TfIdfVectorizer;
import com.voidkey.backend.form.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AgentService {

    private final AgentDecisionRepository decisionRepository;
    private final AgentActionWeightRepository weightRepository;
    private final SubmittedResponseRepository responseRepository;

    private AgentOrchestrator orchestrator;

    public AgentService(AgentDecisionRepository decisionRepository,
                        AgentActionWeightRepository weightRepository,
                        SubmittedResponseRepository responseRepository) {
        this.decisionRepository = decisionRepository;
        this.weightRepository = weightRepository;
        this.responseRepository = responseRepository;
    }

    @PostConstruct
    public void init() {
        TfIdfVectorizer vectorizer = new TfIdfVectorizer();
        CosineSimilarity similarity = new CosineSimilarity();
        NaiveBayesClassifier classifier = new NaiveBayesClassifier();

        List<TrainingExample> trainingData = List.of(
                new TrainingExample("Buy cheap Rolex watches now!", "spam"),
                new TrainingExample("Earn $5000 a day working from home", "spam"),
                new TrainingExample("Free crypto giveaway click link below", "spam"),
                new TrainingExample("Special discount offer limited time deals", "spam"),
                new TrainingExample("Need more clear dark mode docs", "genuine"),
                new TrainingExample("Form submission failed on mobile safari", "genuine"),
                new TrainingExample("Add webhook support for response triggers", "genuine"),
                new TrainingExample("The app is crashing whenever I upload a file", "genuine")
        );

        classifier.train(trainingData);

        List<String> startupCorpus = trainingData.stream()
                .map(TrainingExample::text)
                .collect(Collectors.toList());
        vectorizer.fit(startupCorpus);

        this.orchestrator = new AgentOrchestrator(vectorizer, similarity, classifier);

        for (AgentActionType type : AgentActionType.values()) {
            weightRepository.findById(type).ifPresent(weightEntity ->
                    orchestrator.setConfidenceWeight(type, weightEntity.getWeight())
            );
        }
    }

    @Transactional
    public List<AgentDecisionEntity> analyzeForm(Long formId) {
        List<SubmittedResponse> responses = responseRepository.findByFormIdWithAnswers(formId);
        if (responses == null || responses.isEmpty()) {
            return List.of();
        }

        List<String> answers = responses.stream()
                .filter(r -> r.getAnswers() != null)
                .flatMap(r -> r.getAnswers().stream())
                .map(Answer::getValue)
                .filter(Objects::nonNull)
                .filter(a -> !a.isBlank())
                .collect(Collectors.toList());

        if (answers.isEmpty()) {
            return List.of();
        }

        List<AgentDecision> decisions = orchestrator.analyze(answers);

        List<AgentDecisionEntity> savedEntities = new ArrayList<>();
        for (AgentDecision decision : decisions) {
            AgentDecisionEntity entity = AgentDecisionEntity.builder()
                    .formId(formId)
                    .actionType(decision.actionType())
                    .targets(decision.targets())
                    .rawScore(decision.rawScore())
                    .adjustedScore(decision.adjustedScore())
                    .status(AgentDecisionStatus.PENDING)
                    .build();

            savedEntities.add(decisionRepository.save(entity));
        }

        return savedEntities;
    }

    @Transactional
    public void recordFeedback(Long decisionId, boolean accepted) {
        AgentDecisionEntity entity = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new IllegalArgumentException("AgentDecisionEntity not found with ID: " + decisionId));

        AgentActionType actionType = entity.getActionType();

        orchestrator.recordFeedback(actionType, accepted);

        entity.setStatus(accepted ? AgentDecisionStatus.ACCEPTED : AgentDecisionStatus.REJECTED);
        entity.setResolvedAt(Instant.now());
        decisionRepository.save(entity);

        double updatedWeight = orchestrator.getConfidenceWeight(actionType);
        AgentActionWeightEntity weightEntity = weightRepository.findById(actionType)
                .orElseGet(() -> AgentActionWeightEntity.builder().actionType(actionType).build());
        weightEntity.setWeight(updatedWeight);

        weightRepository.save(weightEntity);
    }
}