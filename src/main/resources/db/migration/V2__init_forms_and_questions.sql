-- V2__init_forms_and_questions.sql

CREATE TABLE forms (
                       id BIGSERIAL PRIMARY KEY,
                       owner_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                       title VARCHAR(255) NOT NULL,
                       description TEXT,
                       theme_json JSONB,
                       status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
                       created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_forms_owner_id ON forms(owner_id);

CREATE TABLE questions (
                           id BIGSERIAL PRIMARY KEY,
                           form_id BIGINT NOT NULL REFERENCES forms(id) ON DELETE CASCADE,
                           type VARCHAR(30) NOT NULL,
                           label VARCHAR(500) NOT NULL,
                           required BOOLEAN NOT NULL DEFAULT false,
                           order_index INT NOT NULL,
                           config_json JSONB,
                           logic_json JSONB
);

CREATE INDEX idx_questions_form_id ON questions(form_id);