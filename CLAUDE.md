# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TSLandRevenuve is a Land Revenue & Registration System — a monorepo with a Spring Boot 3 backend, React 18 frontend, and an optional blockchain anchoring component via Web3j. The backend exposes three API groups in a single runtime: Clean CRUD APIs, Aadhaar/Registration demo APIs, and Platform simulation APIs.

## Commands

### Backend (Spring Boot + Maven)

```bash
# Build
cd app-backend && mvn clean package

# Run tests
cd app-backend && mvn test

# Run locally (requires Java 17 on PATH)
cd app-backend && JAVA_HOME=$(/usr/libexec/java_home -v 17) PATH=$(/usr/libexec/java_home -v 17)/bin:$PATH mvn spring-boot:run

# Run with local profile explicitly
cd app-backend && SPRING_PROFILES_ACTIVE=local mvn spring-boot:run

# Run a single test class
cd app-backend && mvn test -Dtest=LandRecordServiceTest

# Run a single test method
cd app-backend && mvn test -Dtest=LandRecordServiceTest#methodName
```

### Frontend (Vite + React)

```bash
cd frontend && npm install
npm run dev -- --host 0.0.0.0 --port 5173
npm run build
```

### Docker (full stack)

```bash
docker compose up --build        # build + run all services
docker compose up --build -d     # detached
docker compose down
```

### Convenience script

```bash
./run-local.sh   # starts backend on :8080 and frontend on :5173 in background
```

## Prerequisites for Local Development

- Java 17 (exact version required)
- Maven 3.9+
- Node 18+
- PostgreSQL at `localhost:5432/land_revenue` (user: `postgres`, password: `mysecretpassword`)
- Redis at `localhost:6379`

These are configured in `app-backend/src/main/resources/application-local.yml`.

## Architecture

### Backend (`app-backend/src/main/java/in/gov/landrevenue/`)

The code is split into two top-level packages:

**`clean/`** — The primary domain model (CRUD-based, role-secured):
- `controller/` — REST controllers (Auth, LandRecord, Owner, RevenueRecord, Dashboard, AuditLog)
- `service/` — Business logic
- `repository/` — Spring Data JPA repositories
- `entity/` — JPA entities (Users, Owners, LandRecords, RevenueRecords)
- `security/` — JWT authentication filter and token utilities
- `config/` — Spring Security config, cache config

**Root package** — Supplementary API groups:
- `controller/` — RegistrationController, AadhaarController, Platform simulation controllers
- `service/` — Registration service, blockchain sync service, platform simulation service
- `blockchain/` — Web3j integration for EVM anchoring (disabled by default via `LANDREGISTRY_BLOCKCHAIN_ENABLED=false`)

### Frontend (`frontend/src/`)

- `api/` — Axios client (`client.js` sets base URL and attaches JWT bearer token), `authApi.js`, `landApi.js`
- `store/authStore.js` — Zustand store for auth state (JWT token, user role)
- `layouts/` — `AuthLayout` (login shell), `MainLayout` (sidebar + protected shell)
- `pages/` — One file per page; `DashboardPage` uses Recharts for revenue trend charts
- `components/ProtectedRoute.jsx` — Guards routes by auth state and role

### API Surface

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

| Group | Base Path | Auth |
|---|---|---|
| Clean (domain CRUD) | `/api/auth/*`, `/api/lands/*`, `/api/owners/*`, `/api/revenues/*` | JWT Bearer |
| Aadhaar/Registration | `/api/auth/aadhaar/*`, `/api/registrations/*`, `/api/public/*` | `verifiedIdentityToken` (30 min) |
| Platform simulation | `/api/platform/*` | Session token (in-memory) |

### Authentication

JWT-based for Clean APIs. Default seeded credentials:
- `admin` / `admin123` → ADMIN
- `officer` / `officer123` → REVENUE_OFFICER
- `entry` / `entry123` → DATA_ENTRY
- `citizen` / `citizen123` → CITIZEN

Roles are enforced via `@PreAuthorize` annotations in controllers.

### Blockchain (optional)

Controlled by `LANDREGISTRY_BLOCKCHAIN_ENABLED`. When enabled, registration submissions are anchored to an EVM chain via Web3j. The Solidity contract source is at `smart-contracts/LandRegistry.sol`. Default RPC target: `http://localhost:8545` (chain ID 1337).

### Spring Profiles

`local` → `dev` → `qa` → `uat` → `preprod` → `prod` / `dr`. Each has a corresponding `application-{profile}.yml`. The `local` profile uses H2 or local Postgres and disables blockchain.

## Testing

- **Unit tests**: `clean/service/` package (e.g., `LandRecordServiceTest`)
- **Integration tests**: `clean/controller/` package (e.g., `AuthControllerIntegrationTest`, `LandRecordControllerIntegrationTest`)
- JaCoCo enforces **80% minimum code coverage** — build will fail if coverage drops below this threshold.

## Key Environment Variables

| Variable | Default | Purpose |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `local` | Active Spring profile |
| `JWT_SECRET` | (local default) | JWT signing key (32+ chars in prod) |
| `VITE_API_BASE_URL` | `http://localhost:8080/api` | Frontend API base URL |
| `LANDREGISTRY_BLOCKCHAIN_ENABLED` | `false` | Enable EVM blockchain anchoring |
| `LANDREGISTRY_BLOCKCHAIN_RPC_URL` | `http://localhost:8545` | EVM RPC endpoint |
| `LANDREGISTRY_BLOCKCHAIN_CONTRACT_ADDRESS` | — | Deployed LandRegistry contract address |
| `LANDREGISTRY_BLOCKCHAIN_REGISTRAR_PRIVATE_KEY` | — | Wallet private key for on-chain writes |
