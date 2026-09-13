package com.voidkey.backend.form;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voidkey.backend.logic.LogicEvaluator.LogicNode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class LogicNodeConverter implements AttributeConverter<LogicNode, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(LogicNode node) {
        if (node == null) return null;
        try {
            return MAPPER.writeValueAsString(node);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize logic node", e);
        }
    }

    @Override
    public LogicNode convertToEntityAttribute(String json) {
        if (json == null) return null;
        try {
            return MAPPER.readValue(json, LogicNode.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize logic node", e);
        }
    }
}