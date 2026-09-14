# VoidKey — Form Builder Backend with a From-Scratch AI Agent

A Google Forms–style backend built in Java/Spring Boot, with an autonomous
response-analysis agent built entirely from first principles — no external
LLM, no third-party AI API, anywhere in the codebase.

## What this is

VoidKey lets a user create forms, add questions, attach conditional
visibility logic ("show Q7 only if Q3 = Yes"), publish the form, and collect
public submissions. On top of that sits an agent that reads real submitted
responses and autonomously flags likely spam and near-duplicate answers,
using its own confidence in each type of judgment — a confidence that
adapts based on whether a form owner accepts or rejects its suggestions,
and that persists across restarts. Submitted responses are also indexed
into Elasticsearch, giving form owners real full-text search across every
response ever collected.

## Why build the agent from scratch instead of calling an LLM

Wrapping `POST /v1/chat/completions` around a form-analysis feature is an
API integration, not an engineering exercise. This project instead
implements the actual mechanics an intelligent system needs:

- **Perception**: turning raw text into structured signal (TF-IDF vectors)
- **Reasoning**: two independent, classical algorithms — cosine similarity
  for "how alike are these two answers", and Naive Bayes for
  "how likely is this answer to be spam"
- **Action**: combining those signals into a decision, gated by a
  confidence threshold so low-trust action types stay quiet even when
  the raw signal is technically present
- **Learning**: a bounded, multiplicative weight update — when a form
  owner accepts or rejects a suggestion, the agent's confidence in that
  *type* of suggestion moves accordingly, and the new confidence is
  persisted so it survives a restart

This is the classical "intelligent agent" architecture (perceive → reason
→ act → learn), predating and independent of large language models —
implemented here in plain Java with zero ML libraries.

## Architecture
com.voidkey.backend/
├── auth/ JWT issuing + validation, login/signup
├── user/ User entity, Spring Security UserDetails integration
├── form/ Forms, questions, submissions, responses (core CRUD)
├── logic/ Recursive conditional-logic engine (sealed-interface AST,
│ polymorphic JSON serialization via Jackson)
├── search/ Elasticsearch document mapping + repository for
│ full-text search over submitted answers
├── agent/
│ ├── nlp/ Tokenizer, TF-IDF vectorizer, cosine similarity
│ ├── classifier/ Naive Bayes spam/quality classifier
│ └── core/ AgentOrchestrator (the perceive-reason-act-learn loop),
│ AgentService (integration + persistence), controller
└── config/ Security filter chain, CORS, global exception handling

## Core technical decisions worth knowing about

- **JWT auth, stateless sessions.** No server-side session store; the
  token itself carries identity, verified per-request by a custom
  `OncePerRequestFilter`.
- **Ownership-scoped CRUD.** Editing a form requires proving ownership at
  the service layer (`FormService.getFormOwnedBy`), distinct from
  submitting a response to a form, which is intentionally public — same
  trust model as Google Forms/Typeform.
- **N+1 prevention.** Fetching a form's questions uses an explicit
  `LEFT JOIN FETCH` query, not the default lazy-loaded collection —
  loading a form's detail view is one query, not one-plus-N.
- **Polymorphic JSON for the logic engine.** `LogicNode` is a Java sealed
  interface (`LogicRule` / `LogicGroup`), serialized with a
  `@JsonTypeInfo` type discriminator so a form's visibility rules can
  nest arbitrarily deep and still round-trip through Postgres `jsonb`
  correctly.
- **Cascading visibility.** If a question is hidden by its own logic
  rule, its submitted answer is treated as absent — not just null — for
  every rule evaluated after it. This prevents a "ghost answer" a user
  never actually saw the question for from silently triggering other
  questions' visibility.
- **Summary vs. detail response shapes.** Listing forms returns a light
  DTO with no nested data; fetching one form returns the full shape with
  questions embedded — a deliberate two-tier API design, not an
  oversight.
- **Elasticsearch as a secondary, search-only store.** Postgres remains
  the source of truth for every submitted answer; each answer is also
  written to Elasticsearch immediately after its Postgres save, purely
  to power full-text search. If indexing ever failed, the submission
  itself would still be safely persisted — search is additive, not
  load-bearing.

## Known limitations (stated honestly, not hidden)

- **Conditional logic cascade assumes forward dependency.** A question's
  visibility rule is evaluated correctly against every question that
  appears *before* it in the form. A rule referencing a *later* question
  would not cascade correctly — the general case needs topological
  sorting over the rule graph, which was identified but out of scope for
  this project's size.
- **The agent's merge/duplicate detection currently compares answers
  across different questions**, not just answers to the *same* question.
  This is a simplification for the current scope; a production version
  would scope similarity comparisons per-question.
- **The Naive Bayes classifier returns a label, not a probability** — a
  known limitation of this minimal implementation. The agent currently
  treats any "spam" prediction as a fixed raw confidence of 1.0 rather
  than a graded score.
- **No stemming/lemmatization** in the tokenizer, so morphological
  variants ("clear" vs. "clearer") are treated as unrelated words — this
  measurably lowers similarity scores between paraphrased duplicates.
- **Postgres and Elasticsearch can drift out of sync.** There's no
  reconciliation job if the Elasticsearch write fails after the Postgres
  write succeeds — an edge case flagged here rather than solved, since a
  proper fix (outbox pattern, retry queue) is a bigger scope than this
  project currently needs.

## Running it locally

**Full stack (recommended) — Postgres, Elasticsearch, and the app, containerized:**
```bash
docker compose up --build
```
Runs on `http://localhost:8080`. First run takes a few minutes while Maven
resolves dependencies and Elasticsearch initializes.

**Native (for active development), Postgres and Elasticsearch still via Docker:**
```bash
docker compose up postgres elasticsearch -d
mvn spring-boot:run
```

API docs at `http://localhost:8080/docs` once running.

## Tech stack

Java 21, Spring Boot 3.2, Spring Security (JWT), Spring Data JPA,
PostgreSQL, Flyway (versioned migrations, not Hibernate auto-DDL),
Elasticsearch (Spring Data Elasticsearch), Jackson (polymorphic
deserialization), JUnit 5, Docker (multi-stage build, multi-container
Compose).
