package com.voidkey.backend.agent.core;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "agent_action_weights")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentActionWeightEntity {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type")
    private AgentActionType actionType;

    private double weight;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void onSave() {
        updatedAt = Instant.now();
    }
}