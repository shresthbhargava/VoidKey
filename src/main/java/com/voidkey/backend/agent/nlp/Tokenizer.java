package com.voidkey.backend.agent.nlp;

import java.util.*;
import java.util.regex.Pattern;

public class Tokenizer {

    private static final Pattern NON_WORD = Pattern.compile("[^a-zA-Z0-9']+");

    private static final Set<String> STOPWORDS = Set.of(
            "the", "is", "at", "a", "an", "and", "or", "of", "to", "in", "it", "this", "that"
    );

    public List<String> tokenize(String text) {
        if (text == null || text.isBlank()) return List.of();

        String[] rawTokens = NON_WORD.split(text.toLowerCase().trim());
        List<String> tokens = new ArrayList<>();
        for (String t : rawTokens) {
            if (t.isBlank() || STOPWORDS.contains(t) || t.length() < 2) continue;
            tokens.add(t);
        }
        return tokens;
    }
}