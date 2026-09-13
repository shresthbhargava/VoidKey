package com.voidkey.backend.logic;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LogicEvaluator {

    public enum Operator { EQUALS, CONTAINS, GREATER_THAN, LESS_THAN }
    public enum Combinator { ALL, ANY }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = LogicRule.class, name = "RULE"),
            @JsonSubTypes.Type(value = LogicGroup.class, name = "GROUP")
    })
    public sealed interface LogicNode permits LogicRule, LogicGroup {}

    public record LogicRule(Long questionId, Operator operator, String expectedValue) implements LogicNode {}

    public record LogicGroup(List<LogicNode> children, Combinator combinator) implements LogicNode {}

    public static boolean evaluateNode(LogicNode node, Map<Long, String> answers) {
        if (node == null) return false;
        if (node instanceof LogicRule rule) return evaluate(rule, answers);
        if (node instanceof LogicGroup group) return evaluateGroup(group, answers);
        return false;
    }

    private static boolean evaluate(LogicRule rule, Map<Long, String> submittedAnswers) {
        if (rule == null || submittedAnswers == null) return false;
        String submittedAnswer = submittedAnswers.get(rule.questionId());
        if (submittedAnswer == null) return false;

        return switch (rule.operator()) {
            case EQUALS -> Objects.equals(submittedAnswer, rule.expectedValue());
            case CONTAINS -> rule.expectedValue() != null && submittedAnswer.contains(rule.expectedValue());
            case GREATER_THAN -> compareNumeric(submittedAnswer, rule.expectedValue(), (a, e) -> a > e);
            case LESS_THAN -> compareNumeric(submittedAnswer, rule.expectedValue(), (a, e) -> a < e);
        };
    }

    private static boolean evaluateGroup(LogicGroup group, Map<Long, String> answers) {
        if (group == null || group.children() == null || group.children().isEmpty()) return false;
        return switch (group.combinator()) {
            case ALL -> group.children().stream().allMatch(child -> evaluateNode(child, answers));
            case ANY -> group.children().stream().anyMatch(child -> evaluateNode(child, answers));
        };
    }

    private static boolean compareNumeric(String submitted, String expected, NumericComparator comparator) {
        if (submitted == null || expected == null) return false;
        try {
            return comparator.compare(Double.parseDouble(submitted), Double.parseDouble(expected));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @FunctionalInterface
    private interface NumericComparator {
        boolean compare(double submitted, double expected);
    }
}