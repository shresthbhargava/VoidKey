package com.voidkey.backend.agent.core;

import java.util.List;

public record AgentDecision(
        AgentActionType actionType,
        double rawScore,
        double adjustedScore,
        List<String> targets
) {}