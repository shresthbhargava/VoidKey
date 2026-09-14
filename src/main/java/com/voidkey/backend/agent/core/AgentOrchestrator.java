package com.voidkey.backend.agent.core;

import com.voidkey.backend.agent.classifier.NaiveBayesClassifier;
import com.voidkey.backend.agent.nlp.CosineSimilarity;
import com.voidkey.backend.agent.nlp.TextVector;
import com.voidkey.backend.agent.nlp.TfIdfVectorizer;

import java.util.*;

public class AgentOrchestrator {

    private final TfIdfVectorizer vectorizer;
    private final CosineSimilarity similarity;
    private final NaiveBayesClassifier classifier;

    private final Map<AgentActionType, Double> confidenceWeights = new HashMap<>();

    private static final double SURFACE_THRESHOLD = 0.4;
    private static final double SIMILARITY_THRESHOLD = 0.8;

    public AgentOrchestrator(TfIdfVectorizer vectorizer, CosineSimilarity similarity,
                             NaiveBayesClassifier classifier) {
        this.vectorizer = vectorizer;
        this.similarity = similarity;
        this.classifier = classifier;

        for (AgentActionType type : AgentActionType.values()) {
            confidenceWeights.put(type, 0.5);
        }
    }

    public List<AgentDecision> analyze(List<String> answers) {
        if (answers == null || answers.isEmpty()) {
            return Collections.emptyList();
        }

        List<AgentDecision> candidates = new ArrayList<>();

        int n = answers.size();
        if (n >= 2) {
            List<TextVector> vectors = new ArrayList<>(n);
            for (String ans : answers) {
                vectors.add(vectorizer.transform(ans));
            }

            double mergeWeight = confidenceWeights.getOrDefault(AgentActionType.SUGGEST_MERGE, 0.5);

            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    double simScore = similarity.compute(vectors.get(i), vectors.get(j));

                    if (simScore > SIMILARITY_THRESHOLD) {
                        double adjustedScore = simScore * mergeWeight;
                        if (adjustedScore > SURFACE_THRESHOLD) {
                            candidates.add(new AgentDecision(
                                    AgentActionType.SUGGEST_MERGE,
                                    simScore,
                                    adjustedScore,
                                    List.of(answers.get(i), answers.get(j))
                            ));
                        }
                    }
                }
            }
        }

        double spamWeight = confidenceWeights.getOrDefault(AgentActionType.FLAG_SPAM, 0.5);

        for (String answer : answers) {
            String predictedLabel = classifier.predict(answer);
            if ("spam".equalsIgnoreCase(predictedLabel)) {
                double rawScore = 1.0;
                double adjustedScore = rawScore * spamWeight;

                if (adjustedScore > SURFACE_THRESHOLD) {
                    candidates.add(new AgentDecision(
                            AgentActionType.FLAG_SPAM,
                            rawScore,
                            adjustedScore,
                            List.of(answer)
                    ));
                }
            }
        }

        return candidates;
    }

    public void recordFeedback(AgentActionType actionType, boolean accepted) {
        if (actionType == null) {
            return;
        }

        double oldWeight = confidenceWeights.getOrDefault(actionType, 0.5);
        double multiplier = accepted ? 1.1 : 0.9;
        double unclampedWeight = oldWeight * multiplier;

        double newWeight = Math.max(0.05, Math.min(0.95, unclampedWeight));

        confidenceWeights.put(actionType, newWeight);
    }

    public double getConfidenceWeight(AgentActionType type) {
        return confidenceWeights.get(type);
    }
}