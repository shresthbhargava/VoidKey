package com.voidkey.backend.agent.classifier;

import com.voidkey.backend.agent.nlp.Tokenizer;

import java.util.*;

public class NaiveBayesClassifier {

    private final Tokenizer tokenizer = new Tokenizer();

    private final Map<String, Map<String, Integer>> wordLabelCounts = new HashMap<>();
    private final Map<String, Integer> labelDocCounts = new HashMap<>();
    private final Map<String, Integer> labelWordTotals = new HashMap<>();
    private final Set<String> vocabulary = new HashSet<>();
    private int totalDocs = 0;

    public void train(List<TrainingExample> examples) {
        if (examples == null || examples.isEmpty()) {
            return;
        }

        for (TrainingExample example : examples) {
            String label = example.label();
            String text = example.text();

            List<String> tokens = tokenizer.tokenize(text);

            for (String token : tokens) {
                wordLabelCounts
                    .computeIfAbsent(token, k -> new HashMap<>())
                    .merge(label, 1, Integer::sum);

                vocabulary.add(token);
                labelWordTotals.merge(label, 1, Integer::sum);
            }

            labelDocCounts.merge(label, 1, Integer::sum);
            totalDocs++;
        }
    }

    public String predict(String text) {
        if (labelDocCounts.isEmpty() || totalDocs == 0) {
            return null;
        }

        List<String> tokens = tokenizer.tokenize(text);

        String bestLabel = null;
        double maxLogScore = -Double.MAX_VALUE;

        for (String label : labelDocCounts.keySet()) {
            int docsForLabel = labelDocCounts.get(label);
            double score = Math.log((double) docsForLabel / totalDocs);

            int totalWordsForLabel = labelWordTotals.getOrDefault(label, 0);
            int vocabSize = vocabulary.size();
            double denominator = totalWordsForLabel + vocabSize;

            for (String word : tokens) {
                int wordCount = wordLabelCounts
                    .getOrDefault(word, Collections.emptyMap())
                    .getOrDefault(label, 0);

                double wordProbability = (wordCount + 1.0) / denominator;
                score += Math.log(wordProbability);
            }

            if (score > maxLogScore) {
                maxLogScore = score;
                bestLabel = label;
            }
        }

        return bestLabel;
    }
}
