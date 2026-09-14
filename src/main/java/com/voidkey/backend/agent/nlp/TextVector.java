package com.voidkey.backend.agent.nlp;

import java.util.Map;

public record TextVector(Map<String, Double> weights) {}