CREATE TABLE responses (
                           id BIGSERIAL PRIMARY KEY,
                           form_id BIGINT NOT NULL REFERENCES forms(id) ON DELETE CASCADE,
                           submitted_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_responses_form_id ON responses(form_id);

CREATE TABLE answers (
                         id BIGSERIAL PRIMARY KEY,
                         response_id BIGINT NOT NULL REFERENCES responses(id) ON DELETE CASCADE,
                         question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
                         value TEXT
);

CREATE INDEX idx_answers_response_id ON answers(response_id);