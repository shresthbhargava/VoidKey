package com.voidkey.backend.agent.core;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "agent_decisions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentDecisionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "form_id", nullable = false)
    private Long formId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private AgentActionType actionType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "targets_json", columnDefinition = "jsonb", nullable = false)
    private List<String> targets;

    @Column(name = "raw_score", nullable = false)
    private double rawScore;

    @Column(name = "adjusted_score", nullable = false)
    private double adjustedScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AgentDecisionStatus status = AgentDecisionStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}