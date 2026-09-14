package com.voidkey.backend.agent.nlp;

import java.util.*;

public class TfIdfVectorizer {

    private final Tokenizer tokenizer = new Tokenizer();
    private Map<String, Double> idfScores = new HashMap<>();

    public void fit(List<String> corpus) {
        if (corpus == null || corpus.isEmpty()) {
            idfScores = new HashMap<>();
            return;
        }

        int totalDocuments = corpus.size();
        Map<String, Integer> docCountMap = new HashMap<>();

        for (String document : corpus) {
            List<String> tokens = tokenizer.tokenize(document);
            Set<String> uniqueTokensInDoc = new HashSet<>(tokens);

            for (String token : uniqueTokensInDoc) {
                docCountMap.put(token, docCountMap.getOrDefault(token, 0) + 1);
            }
        }

        Map<String, Double> newIdfScores = new HashMap<>();
        for (Map.Entry<String, Integer> entry : docCountMap.entrySet()) {
            String word = entry.getKey();
            int docsContainingWord = entry.getValue();
            double idf = Math.log((double) totalDocuments / (1.0 + docsContainingWord));
            newIdfScores.put(word, idf);
        }

        this.idfScores = newIdfScores;
    }

    public TextVector transform(String text) {
        if (text == null || text.isBlank()) {
            return new TextVector(Collections.emptyMap());
        }

        List<String> tokens = tokenizer.tokenize(text);
        if (tokens.isEmpty()) {
            return new TextVector(Collections.emptyMap());
        }

        double totalWordsInText = tokens.size();

        Map<String, Double> tfMap = new HashMap<>();
        for (String token : tokens) {
            tfMap.put(token, tfMap.getOrDefault(token, 0.0) + 1.0);
        }

        Map<String, Double> tfIdfVector = new HashMap<>();
        for (Map.Entry<String, Double> entry : tfMap.entrySet()) {
            String word = entry.getKey();
            double count = entry.getValue();
            double tf = count / totalWordsInText;
            double idf = idfScores.getOrDefault(word, 0.0);
            tfIdfVector.put(word, tf * idf);
        }

        return new TextVector(tfIdfVector);
    }
}
