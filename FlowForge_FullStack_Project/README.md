# FlowForge — Configurable Workflow Automation

A portfolio-oriented full-stack starter for configurable approvals and task routing.

## Stack
- React + TypeScript + Vite
- Java 21 + Spring Boot 3.4
- Spring Security (HTTP Basic demo authentication)
- Spring Data JPA + PostgreSQL
- Scheduled background worker for retryable outbox actions
- Docker Compose

## Features in this starter
- Create workflow definitions with configurable stages and role requirements
- Start workflow instances from a definition
- See tasks assigned to a role/user and act on them
- Approve, reject, return, and escalate task actions
- Conditional routing based on simple JSON context conditions
- Persist workflow state, task history, and audit events
- Idempotency key support for workflow actions
- Background outbox worker with bounded retry behavior
- React dashboard with definitions, instances, tasks, and timeline views

## Run with Docker
Requirements: Docker Desktop with Compose enabled.

```bash
docker compose up --build
```

- Frontend: http://localhost:5173
- API: http://localhost:8080/api/health
- PostgreSQL: localhost:5432

Demo accounts (HTTP Basic):
- `demo` / `demo123`
- `admin` / `admin123`

The frontend uses these demo credentials when calling the API. This is a learning/demo setup, not production authentication. Before public deployment, replace in-memory users and demo password handling with a persistent user model, BCrypt password hashing, CSRF/session or token strategy appropriate to the client, and secret management.

## Run locally without Docker
1. Start PostgreSQL and create database `flowforge`.
2. Set `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD` if your local credentials differ.
3. Backend:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
4. Frontend:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

## API overview
- `GET /api/health`
- `GET /api/definitions`
- `POST /api/definitions`
- `POST /api/instances` — body: `{"definitionId":1,"title":"Laptop purchase","context":{"amount":650,"department":"engineering"}}`
- `GET /api/instances`
- `GET /api/instances/{id}`
- `GET /api/tasks?status=PENDING`
- `POST /api/tasks/{id}/actions` — body: `{"action":"APPROVE","comment":"Looks good","actor":"demo","idempotencyKey":"unique-key"}`
- `GET /api/instances/{id}/timeline`

## Workflow definition JSON example
```json
{
  "name": "Expense approval",
  "description": "Approval flow for employee expenses",
  "stages": [
    {"name":"Manager review","requiredRole":"MANAGER","onApprove":"Finance review","onReject":"REJECTED","onReturn":"RETURNED","onEscalate":"Finance review"},
    {"name":"Finance review","requiredRole":"FINANCE","onApprove":"APPROVED","onReject":"REJECTED","onReturn":"Manager review","onEscalate":"ADMIN"}
  ]
}
```

Routing conditions can be added per stage with `conditionKey`, `conditionOperator`, and `conditionValue`. Supported operators: `GT`, `GTE`, `EQ`, `NE`, `CONTAINS`. If the condition matches, the configured `conditionTarget` stage is selected; otherwise `onApprove` is used.

## Important limitations
This is a starter implementation intended for local learning and portfolio development. It is not a certified production workflow engine. The demo auth uses in-memory users; role identity from the request is not a substitute for a full identity/authorization model. The outbox worker demonstrates retry-safe processing but does not send external notifications. Add integration tests, stronger input validation, migrations (Flyway), pagination, observability, and production-grade identity before presenting it as production-ready.
