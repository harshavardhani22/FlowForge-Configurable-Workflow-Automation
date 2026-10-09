# FlowForge — Configurable Workflow Automation

FlowForge is a full-stack workflow automation platform that enables teams to build and manage approval processes without hardcoding every business workflow.

## Features

- Configurable workflow definitions and stages
- Role-based task routing
- Approve, reject, return, and escalation actions
- Conditional routing using business rules
- Workflow execution tracking and audit timelines
- PostgreSQL persistence
- Spring Boot REST APIs
- Scheduled background processing with an outbox pattern
- React and TypeScript dashboard
- Docker Compose development environment

## Tech Stack

- **Frontend:** React, TypeScript, Vite
- **Backend:** Java 21, Spring Boot
- **Database:** PostgreSQL, Spring Data JPA
- **Security:** Spring Security
- **Deployment:** Docker Compose

## Getting Started

### Prerequisites

- Docker Desktop
- Git

### Run the application

```bash
docker compose up --build
```

Open the frontend at `http://localhost:5173`.

Backend health endpoint: `http://localhost:8080/api/health`.

### Demo credentials

- Username: `demo`
- Password: `demo123`

These credentials are for local demonstration only.

## Example Use Case

An employee submits an expense request. The workflow routes it to a manager for review. Based on configured business rules, the request can move to finance, be rejected, be returned, or be escalated. Each action is recorded in the execution timeline.

## API Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/health` | Check backend health |
| GET | `/api/definitions` | List workflow definitions |
| POST | `/api/definitions` | Create a workflow definition |
| GET | `/api/instances` | List workflow executions |
| POST | `/api/instances` | Start a workflow |
| GET | `/api/tasks` | List tasks |
| POST | `/api/tasks/{id}/actions` | Process a task action |
| GET | `/api/instances/{id}/timeline` | View audit history |

## Engineering Concepts

This project explores workflow state transitions, business-rule evaluation, role-based access, idempotency, auditability, and retryable background processing.

## Future Improvements

- Persistent user accounts and stronger authentication
- Automated unit and integration tests
- More advanced workflow conditions and visual workflow builder
- Real notification and webhook integrations
- Database migrations and improved observability

## Security Note

This is a portfolio starter project. Demo authentication and configuration are not intended for production deployment. Replace demo credentials, strengthen authorization, and configure secrets securely before public deployment.
