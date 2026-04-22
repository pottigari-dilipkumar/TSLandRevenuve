# Local Run Instructions

This file explains the recommended local setup for the cleaned repository.

## Prerequisites

- Java 17
- Maven 3.9+
- Node.js 18+ and npm
- PostgreSQL
- Redis
- Optional: Docker + Docker Compose

## 1) Local services expected by the backend

The local Spring profile uses fixed values from `app-backend/src/main/resources/application-local.yml`.

PostgreSQL:
- host: `localhost`
- port: `5432`
- database: `land_revenue`
- username: `postgres`
- password: `postgres`

Redis:
- host: `localhost`
- port: `6379`

Blockchain:
- configured with local defaults
- disabled by default with `landregistry.blockchain.enabled: false`

Create the local database if needed:

```bash
createdb land_revenue
```

## 2) Use Java 17 for this repo

If `java -version` shows Java 11 or something else, set Java 17 before running Maven:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"
java -version
```

## 3) Run Backend (Spring Boot)

```bash
cd app-backend
JAVA_HOME=$(/usr/libexec/java_home -v 17) PATH=$(/usr/libexec/java_home -v 17)/bin:$PATH mvn spring-boot:run
```

Backend starts at:
- `http://localhost:8080`

Useful URLs:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

Seeded local users:
- `admin / admin123`
- `officer / officer123`
- `entry / entry123`
- `citizen / citizen123`

Basic login test:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

## 4) Run Frontend (Vite)

Open a new terminal:

```bash
cd frontend
npm install
npm run dev -- --host 0.0.0.0 --port 5173
```

Frontend URL:
- `http://localhost:5173`

The frontend calls the backend at `http://localhost:8080/api` by default.

## 5) Test Aadhaar OTP + Registration Flow

### Send OTP
```bash
curl -X POST http://localhost:8080/api/auth/aadhaar/send-otp \
  -H "Content-Type: application/json" \
  -d '{"aadhaarNumber":"234567890123"}'
```

You will get a demo response with `demoOtp`.

### Verify OTP
```bash
curl -X POST http://localhost:8080/api/auth/aadhaar/verify-otp \
  -H "Content-Type: application/json" \
  -d '{"aadhaarNumber":"234567890123","otp":"<demoOtp>"}'
```

Copy `verifiedIdentityToken` from response.

### Submit Registration
```bash
curl -X POST http://localhost:8080/api/registrations \
  -H "Content-Type: application/json" \
  -d '{
    "parcelId":"PCL-1001",
    "sellerName":"Ravi Kumar",
    "buyerName":"Anita Sharma",
    "registrationRef":"REG-2026-0001",
    "deedHash":"0xabc123",
    "verifiedIdentityToken":"<verifiedIdentityToken>"
  }'
```

### Public Verify
```bash
curl http://localhost:8080/api/public/verify/REG-2026-0001
```

## 6) Optional helper script

From the repo root:

```bash
./run-local.sh
```

This starts:
- backend on `http://localhost:8080`
- frontend on `http://localhost:5173`

## 7) Optional Docker Run

```bash
docker compose up --build
```

- Frontend: `http://localhost:8081`
- Backend: `http://localhost:8080`

## Notes

- Aadhaar OTP here is **demo/mock only** for local development.
- For production, integrate with legally compliant identity providers and secure OTP delivery channels.
- If you want to test active blockchain anchoring locally, enable blockchain config and run a local EVM node plus deployed contract.
