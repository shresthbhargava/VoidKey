package com.voidkey.backend.agent.core;

import com.voidkey.backend.form.FormService;
import com.voidkey.backend.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forms/{formId}/agent")
public class AgentController {

    private final AgentService agentService;
    private final FormService formService;

    public AgentController(AgentService agentService, FormService formService) {
        this.agentService = agentService;
        this.formService = formService;
    }

    // Owner-only: analyzing responses is part of managing the form, same trust
    // boundary as viewing responses themselves.
    @PostMapping("/analyze")
    public List<AgentDecisionEntity> analyze(@PathVariable Long formId, @AuthenticationPrincipal User user) {
        formService.getFormOwnedBy(formId, user); // enforces ownership; throws if not owner
        return agentService.analyzeForm(formId);
    }

    @PatchMapping("/decisions/{decisionId}/feedback")
    public ResponseEntity<Void> feedback(
            @PathVariable Long formId,
            @PathVariable Long decisionId,
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, Boolean> body) {

        formService.getFormOwnedBy(formId, user);
        boolean accepted = body.getOrDefault("accepted", false);
        agentService.recordFeedback(decisionId, accepted);
        return ResponseEntity.noContent().build();
    }
}