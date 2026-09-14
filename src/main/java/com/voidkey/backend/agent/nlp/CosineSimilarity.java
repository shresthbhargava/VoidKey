package com.voidkey.backend.agent.nlp;

import java.util.HashSet;
import java.util.Set;

public class CosineSimilarity {

    public double compute(TextVector a, TextVector b) {
        Set<String> allWords = new HashSet<>();
        allWords.addAll(a.weights().keySet());
        allWords.addAll(b.weights().keySet());

        double dotProduct = 0.0;
        double magnitudeA = 0.0;
        double magnitudeB = 0.0;

        for (String word : allWords) {
            double wa = a.weights().getOrDefault(word, 0.0);
            double wb = b.weights().getOrDefault(word, 0.0);
            dotProduct += wa * wb;
            magnitudeA += wa * wa;
            magnitudeB += wb * wb;
        }

        if (magnitudeA == 0 || magnitudeB == 0) return 0.0;
        return dotProduct / (Math.sqrt(magnitudeA) * Math.sqrt(magnitudeB));
    }
}