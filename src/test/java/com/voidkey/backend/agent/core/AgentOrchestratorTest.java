package com.voidkey.backend.agent.core;

import com.voidkey.backend.agent.classifier.NaiveBayesClassifier;
import com.voidkey.backend.agent.nlp.CosineSimilarity;
import com.voidkey.backend.agent.nlp.TextVector;
import com.voidkey.backend.agent.nlp.TfIdfVectorizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AgentOrchestratorTest {

    private AgentOrchestrator orchestrator;
    private TfIdfVectorizer vectorizer;

    @BeforeEach
    void setUp() {
        vectorizer = new TfIdfVectorizer();

        List<String> corpus = List.of(
                "Need more clear dark mode docs",
                "Need clearer dark mode docs",
                "Form submission failed on mobile safari",
                "Add webhook support for response triggers"
        );
        vectorizer.fit(corpus);

        CosineSimilarity similarity = new CosineSimilarity();
        NaiveBayesClassifier classifier = new NaiveBayesClassifier();

        orchestrator = new AgentOrchestrator(vectorizer, similarity, classifier);
    }

    @Test
    @DisplayName("Diagnostic: prints raw similarity score between the two near-duplicate test strings")
    void printsRawSimilarityForDebugging() {
        TextVector v1 = vectorizer.transform("Need dark mode docs");
        TextVector v2 = vectorizer.transform("Need dark mode docs please");
        double rawSim = new CosineSimilarity().compute(v1, v2);
        System.out.println("raw similarity: " + rawSim);
    }

    @Test
    @DisplayName("Proves adaptive learning loop: suggestion is surfaced, rejected, and suppressed after threshold drop")
    void testAdaptiveLearningLoopSuppressesRejectedSuggestions() {
        List<String> submissionAnswers = List.of(
                "Need dark mode docs",
                "Need dark mode docs please"
        );

        assertEquals(0.5, orchestrator.getConfidenceWeight(AgentActionType.SUGGEST_MERGE), 0.0001);

        List<AgentDecision> initialDecisions = orchestrator.analyze(submissionAnswers);
        System.out.println("initialDecisions: " + initialDecisions);
        assertFalse(initialDecisions.isEmpty(), "Should surface merge suggestion initially");
        assertEquals(AgentActionType.SUGGEST_MERGE, initialDecisions.get(0).actionType());
        assertTrue(initialDecisions.get(0).adjustedScore() > 0.4, "Adjusted score must clear SURFACE_THRESHOLD");

        orchestrator.recordFeedback(AgentActionType.SUGGEST_MERGE, false);
        assertEquals(0.45, orchestrator.getConfidenceWeight(AgentActionType.SUGGEST_MERGE), 0.0001);

        orchestrator.recordFeedback(AgentActionType.SUGGEST_MERGE, false);
        assertEquals(0.405, orchestrator.getConfidenceWeight(AgentActionType.SUGGEST_MERGE), 0.0001);

        List<AgentDecision> decisionsAfterTwoRejections = orchestrator.analyze(submissionAnswers);
        assertFalse(decisionsAfterTwoRejections.isEmpty(), "Should still surface when weight (0.405) > 0.4");

        orchestrator.recordFeedback(AgentActionType.SUGGEST_MERGE, false);
        assertEquals(0.3645, orchestrator.getConfidenceWeight(AgentActionType.SUGGEST_MERGE), 0.0001);

        List<AgentDecision> decisionsAfterThreeRejections = orchestrator.analyze(submissionAnswers);
        assertTrue(
                decisionsAfterThreeRejections.isEmpty(),
                "Agent should suppress merge suggestion once adjusted score (0.3645 * sim) falls below 0.4 threshold"
        );
    }
}