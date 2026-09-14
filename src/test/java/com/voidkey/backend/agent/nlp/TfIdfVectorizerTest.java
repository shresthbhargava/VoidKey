package com.voidkey.backend.agent.nlp;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TfIdfVectorizerTest {

    @Test
    void similarTextsScoreHigherThanDissimilarText() {
        TfIdfVectorizer vectorizer = new TfIdfVectorizer();

        List<String> corpus = List.of(
                "the service was great and fast",
                "terrible wait times and bad service",
                "great service, would come again"
        );

        vectorizer.fit(corpus);

        TextVector v1 = vectorizer.transform("great service");
        TextVector v2 = vectorizer.transform("would come again, great service");
        TextVector v3 = vectorizer.transform("terrible wait times");

        System.out.println("v1 (\"great service\"): " + v1);
        System.out.println("v2 (\"would come again, great service\"): " + v2);
        System.out.println("v3 (\"terrible wait times\"): " + v3);

        CosineSimilarity similarity = new CosineSimilarity();

        double simV1V2 = similarity.compute(v1, v2);
        double simV1V3 = similarity.compute(v1, v3);

        System.out.println("similarity(v1, v2) = " + simV1V2);
        System.out.println("similarity(v1, v3) = " + simV1V3);

        assertTrue(simV1V2 > simV1V3,
                "Expected v1 and v2 (both mention 'great service') to be more similar " +
                "than v1 and v3 (completely different topic)");
    }
}
