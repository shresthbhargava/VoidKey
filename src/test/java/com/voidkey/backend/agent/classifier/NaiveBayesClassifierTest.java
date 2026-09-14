package com.voidkey.backend.agent.classifier;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NaiveBayesClassifierTest {

    @Test
    void classifiesGenuineAndSpamCorrectly() {
        NaiveBayesClassifier classifier = new NaiveBayesClassifier();

        List<TrainingExample> examples = List.of(
                // genuine
                new TrainingExample("The service was excellent and the staff were friendly", "genuine"),
                new TrainingExample("I really enjoyed my experience, would recommend to a friend", "genuine"),
                new TrainingExample("Great product quality but shipping took a while", "genuine"),
                new TrainingExample("The support team resolved my issue quickly and politely", "genuine"),
                new TrainingExample("Overall a positive experience, will buy again", "genuine"),
                new TrainingExample("The app is easy to use and the design looks clean", "genuine"),
                new TrainingExample("Customer service answered all my questions clearly", "genuine"),

                // spam / low-effort
                new TrainingExample("asdf", "spam"),
                new TrainingExample("n/a", "spam"),
                new TrainingExample("test test test", "spam"),
                new TrainingExample("aaaaaaaa", "spam"),
                new TrainingExample("xyz xyz", "spam"),
                new TrainingExample("qwerty qwerty qwerty", "spam"),
                new TrainingExample("no comment no comment", "spam")
        );

        classifier.train(examples);

        String genuinePrediction = classifier.predict("The staff were helpful and the product works great");
        String spamPrediction = classifier.predict("asdf asdf test");

        System.out.println("genuine input predicted as: " + genuinePrediction);
        System.out.println("spam input predicted as: " + spamPrediction);

        assertEquals("genuine", genuinePrediction);
        assertEquals("spam", spamPrediction);
    }
}