-- Flyway migration V1: users table.
-- Why Flyway instead of Hibernate ddl-auto=update: every schema change is a numbered,
-- reviewable, version-controlled file. In a team setting this is what actually runs
-- in CI/CD before deploy — "update" mode is fine for a solo weekend script, not for
-- something you want to call industry-grade on a resume.

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_email ON users(email);
