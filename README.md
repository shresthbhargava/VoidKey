# VoidKey Backend — Phase 1: Project Setup + Auth

This is the starting skeleton. It does exactly one thing end-to-end: **signup, login,
and a JWT-protected endpoint** — the foundation everything else (forms, responses,
the agent) will sit on top of.

## Run it locally

```bash
# 1. Start Postgres
docker compose up -d

# 2. Set the JWT secret (or rely on the insecure dev default in application.yml)
export JWT_SECRET="a-long-random-string-at-least-32-characters"

# 3. Run the app (requires Maven installed — `mvn -v` to check, or add the
#    wrapper yourself with `mvn -N io.takari:maven:wrapper` if you'd rather use ./mvnw)
mvn spring-boot:run
```

The app starts on `http://localhost:8080`. Flyway will automatically run
`V1__init_users.sql` against your local Postgres on startup — check the logs for
`Successfully applied 1 migration`.

## Verify Phase 1 actually works (do this before moving on)

```bash
# Sign up
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@voidkey.dev","password":"password123"}'
# -> 201, returns { token, name, email }

# Log in
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@voidkey.dev","password":"password123"}'
# -> 200, returns a fresh token

# Call the protected endpoint WITHOUT a token
curl http://localhost:8080/api/me
# -> should be 401/403 (this proves SecurityConfig is actually enforcing auth)

# Call it WITH the token from signup/login
curl http://localhost:8080/api/me -H "Authorization: Bearer <paste token here>"
# -> 200, returns your id/name/email/role (this proves the whole JWT chain works)
```

If all four of those behave as described, Phase 1 is genuinely done — not just
"the code compiles," but "the security model actually does what it claims to do."

API docs (Swagger UI) are at `http://localhost:8080/docs` once the app is running.

## What's deliberately NOT here yet

- `form` / `question` / `response` packages — Phase 1 of the main guide, coming next
- `logic` (conditional branching engine) — Phase 2
- `agent` (from-scratch TF-IDF / Naive Bayes / orchestrator) — Phases 3–4, and that's
  where the assignments start. This phase was full working code on purpose, since
  auth/security wiring is standard plumbing, not the part you're here to learn deeply.

## File-by-file, what to actually understand (not just copy)

| File | Understand this before moving on |
|---|---|
| `SecurityConfig` | Why CSRF is disabled here specifically (no cookies used), and the difference between "authenticated" (JwtAuthFilter) and "authorized" (authorizeHttpRequests) |
| `JwtAuthFilter` | Why it never itself rejects a request — it only establishes identity |
| `JwtService` | Why JWTs are stateless, and the revocation trade-off that comes with that |
| `User.java` | The UserDetails contract — which 4 methods Spring Security actually calls, and why |
| `V1__init_users.sql` | Why Flyway (versioned migrations) instead of `ddl-auto: update` |

If any of these don't make sense after reading the inline comments, ask about that
specific file — that's exactly the right level to dig into before writing Phase 2.
