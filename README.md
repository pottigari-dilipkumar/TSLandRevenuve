# Telangana Land Revenue & Registration System

A full-stack digital platform for land records management, property registration, mutation workflows, revenue collection, and public encumbrance certificate generation — built for Telangana state governance.

> **Not a developer?** Read [BUSINESS.md](./BUSINESS.md) for a plain-English explanation of what this system does and why it exists.

---

## Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3, Java 17, Spring Data JPA, Spring Security (JWT) |
| Frontend | React 18, Vite, Tailwind CSS, react-leaflet |
| Database | PostgreSQL |
| Cache | Redis |
| Blockchain (optional) | Web3j + EVM (Solidity contract) |

---

## Quick Start (Local)

**Prerequisites:** Java 17, Maven 3.9+, Node 18+, PostgreSQL at `localhost:5432/land_revenue`, Redis at `localhost:6379`

```bash
# Backend
cd app-backend
JAVA_HOME=$(/usr/libexec/java_home -v 17) PATH=$(/usr/libexec/java_home -v 17)/bin:$PATH mvn spring-boot:run

# Frontend (separate terminal)
cd frontend
npm install
npm run dev -- --host 0.0.0.0 --port 5173
```

Or use the convenience script from the repo root:
```bash
./run-local.sh
```

Or Docker (full stack):
```bash
docker compose up --build
```

| Service | URL |
|---|---|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |

---

## Default Login Credentials

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | Admin — full access |
| `officer` | `officer123` | Revenue Officer |
| `entry` | `entry123` | Data Entry |
| `citizen` | `citizen123` | Citizen |

---

## Repository Structure

```
app-backend/        Spring Boot backend (APIs, JPA, security, blockchain)
frontend/           React + Vite frontend
smart-contracts/    Solidity LandRegistry contract (optional blockchain)
backend/            Legacy API spec and schema blueprints
```

### Backend package layout

```
clean/              Primary domain (land records, owners, revenue, auth)
  controller/       REST controllers
  service/          Business logic
  repository/       Spring Data JPA
  entity/           JPA entities
  security/         JWT filter and token utils
  config/           Spring Security, cache config

controller/         Registration, Aadhaar, platform simulation controllers
service/            Registration service, blockchain sync
blockchain/         Web3j EVM integration (disabled by default)
```

---

## API Groups

The backend exposes three API groups in a single runtime:

| Group | Base Path | Auth |
|---|---|---|
| Domain CRUD | `/api/auth/*`, `/api/lands/*`, `/api/owners/*`, `/api/revenues/*` | JWT Bearer |
| Aadhaar / Registration | `/api/auth/aadhaar/*`, `/api/registrations/*`, `/api/public/*` | Verified Identity Token (30 min) |
| Platform Simulation | `/api/platform/*` | In-memory session token |

Full endpoint documentation: **http://localhost:8080/swagger-ui.html**

---

## Key Features

- **Land Records** — survey number, owner, district/village, area, land type, map polygon boundary, PLUS Code
- **Property Registration** — Aadhaar OTP identity verification → SRO review → approval auto-updates land ownership
- **Mutation Workflow** — APPLIED → MANDAL_REVIEW → APPROVED/REJECTED; auto-updates ownership on approval
- **Ownership History** — full timeline of every ownership transfer (registrations + mutations) per parcel
- **Encumbrance Certificate** — public search + EC generation without login
- **Revenue Tracking** — payment recording and dashboard summaries
- **Map Polygon Drawing** — click-to-draw parcel boundaries on OpenStreetMap; PLUS Code auto-generated from centroid
- **Blockchain Anchoring** — optional EVM anchoring of registrations (disabled by default)

---

## Environment Variables

### Backend

| Variable | Default | Purpose |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `local` | Active Spring profile |
| `JWT_SECRET` | (local default) | JWT signing key (32+ chars in prod) |
| `JWT_EXPIRATION_MS` | `86400000` | Token expiry in ms |
| `LANDREGISTRY_BLOCKCHAIN_ENABLED` | `false` | Enable EVM anchoring |
| `LANDREGISTRY_BLOCKCHAIN_RPC_URL` | `http://localhost:8545` | EVM RPC endpoint |
| `LANDREGISTRY_BLOCKCHAIN_CONTRACT_ADDRESS` | — | Deployed contract address |
| `LANDREGISTRY_BLOCKCHAIN_REGISTRAR_PRIVATE_KEY` | — | Wallet key for on-chain writes |

### Frontend

| Variable | Default | Purpose |
|---|---|---|
| `VITE_API_BASE_URL` | `http://localhost:8080/api` | Backend API base URL |

---

## Running Tests

```bash
# All tests
cd app-backend && mvn test

# Single class
cd app-backend && mvn test -Dtest=LandRecordServiceTest

# Single method
cd app-backend && mvn test -Dtest=LandRecordServiceTest#methodName
```

JaCoCo enforces **80% minimum code coverage** — build fails if coverage drops below this threshold.

---

## Production Hardening Checklist

- Replace `ddl-auto=update` with Flyway or Liquibase migrations
- Store `JWT_SECRET` and DB credentials in a secrets manager
- Use managed PostgreSQL with connection pooling
- Restrict CORS origins (remove wildcard)
- Terminate TLS at ingress / reverse proxy
- Add central logging, metrics, and alerting
- Run CI/CD with test + vulnerability scanning
