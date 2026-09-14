CREATE TABLE agent_action_weights (
                                      action_type VARCHAR(30) PRIMARY KEY,
                                      weight DOUBLE PRECISION NOT NULL,
                                      updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE agent_decisions (
                                 id BIGSERIAL PRIMARY KEY,
                                 form_id BIGINT NOT NULL REFERENCES forms(id) ON DELETE CASCADE,
                                 action_type VARCHAR(30) NOT NULL,
                                 targets_json JSONB NOT NULL,
                                 raw_score DOUBLE PRECISION NOT NULL,
                                 adjusted_score DOUBLE PRECISION NOT NULL,
                                 status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                 created_at TIMESTAMP NOT NULL DEFAULT now(),
                                 resolved_at TIMESTAMP
);

CREATE INDEX idx_agent_decisions_form_id ON agent_decisions(form_id);