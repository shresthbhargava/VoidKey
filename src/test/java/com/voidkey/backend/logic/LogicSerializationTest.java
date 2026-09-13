package com.voidkey.backend.logic;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogicSerializationTest {

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = LogicEvaluator.LogicRule.class, name = "RULE"),
            @JsonSubTypes.Type(value = LogicEvaluator.LogicGroup.class, name = "GROUP")
    })
    interface JacksonLogicNodeMixin {}

    @Test
    void roundTripPreservesEvaluationResult() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(LogicEvaluator.LogicNode.class, JacksonLogicNodeMixin.class);

        LogicEvaluator.LogicNode tree = new LogicEvaluator.LogicGroup(
                List.of(
                        new LogicEvaluator.LogicRule(1L, LogicEvaluator.Operator.EQUALS, "Yes"),
                        new LogicEvaluator.LogicGroup(
                                List.of(
                                        new LogicEvaluator.LogicRule(3L, LogicEvaluator.Operator.GREATER_THAN, "5"),
                                        new LogicEvaluator.LogicRule(4L, LogicEvaluator.Operator.CONTAINS, "urgent")
                                ),
                                LogicEvaluator.Combinator.ANY
                        )
                ),
                LogicEvaluator.Combinator.ALL
        );

        String json = mapper.writeValueAsString(tree);
        System.out.println(json);

        LogicEvaluator.LogicNode roundTripped = mapper.readValue(json, LogicEvaluator.LogicNode.class);

        Map<Long, String> answers = Map.of(1L, "Yes", 3L, "2", 4L, "this is urgent");

        boolean original = LogicEvaluator.evaluateNode(tree, answers);
        boolean afterRoundTrip = LogicEvaluator.evaluateNode(roundTripped, answers);

        assertTrue(original);
        assertEquals(original, afterRoundTrip);
    }
}