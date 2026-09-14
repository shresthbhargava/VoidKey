// AgentDecisionRepository.java
package com.voidkey.backend.agent.core;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AgentDecisionRepository extends JpaRepository<AgentDecisionEntity, Long> {
    List<AgentDecisionEntity> findByFormId(Long formId);
}