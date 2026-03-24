# India Land Revenue & Registration System (Java + Frontend + Docker)

This project now includes a **working full-stack starter application** with:
- **Backend:** Java Spring Boot REST API
- **Frontend:** Simple web app for Aadhaar OTP flow + registration submission
- **Containers:** Dockerfiles + docker-compose for one-command startup

> Important: Aadhaar OTP in this repository is a **demo/mock flow** for development only. Production use must integrate with legally compliant UIDAI-authorized services and follow Indian regulations.

## Tech Stack

- Java 17 + Spring Boot 3 (backend)
- HTML/CSS/JavaScript (frontend)
- Docker + Docker Compose

## Features Implemented

1. **Aadhaar OTP Authentication (Demo)**
   - `POST /api/auth/aadhaar/send-otp`
   - `POST /api/auth/aadhaar/verify-otp`
   - Returns a short-lived `verifiedIdentityToken`

2. **Land Registration API**
   - `POST /api/registrations`
   - Requires `verifiedIdentityToken` from OTP verification

3. **Public Registration Verification**
   - `GET /api/public/verify/{registrationRef}`

4. **Frontend Integration**
   - Send OTP, verify OTP, submit registration, verify registration reference

## Folder Structure

- `app-backend/` Spring Boot service
- `frontend/` static frontend app served by nginx
- `docker-compose.yml` runs frontend + backend
- `smart-contracts/` and `backend/` earlier blueprint assets retained

## Run with Docker

```bash
docker compose up --build
```

Then open:
- Frontend: `http://localhost:8081`
- Backend: `http://localhost:8080`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Swagger UI (all endpoints): `http://localhost:8080/swagger-ui/index.html`

## API Documentation (OpenAPI / Swagger)

This project now exposes OpenAPI specs from the backend so integrators can discover and test all available endpoints after startup.

- **Machine-readable spec (JSON):** `http://localhost:8080/v3/api-docs`
- **Interactive docs (Swagger UI):** `http://localhost:8080/swagger-ui/index.html`

You can use Swagger UI to:
- Browse every endpoint, request body, and response schema.
- Try API calls directly from the browser.
- Share endpoint contracts with integration teams.

## Example API Calls

### 1) Send Aadhaar OTP (Demo)
```bash
curl -X POST http://localhost:8080/api/auth/aadhaar/send-otp \
  -H "Content-Type: application/json" \
  -d '{"aadhaarNumber":"234567890123"}'
```

### 2) Verify OTP
```bash
curl -X POST http://localhost:8080/api/auth/aadhaar/verify-otp \
  -H "Content-Type: application/json" \
  -d '{"aadhaarNumber":"234567890123","otp":"123456"}'
```

### 3) Submit Registration
```bash
curl -X POST http://localhost:8080/api/registrations \
  -H "Content-Type: application/json" \
  -d '{
    "parcelId":"PCL-1001",
    "sellerName":"Ravi Kumar",
    "buyerName":"Anita Sharma",
    "registrationRef":"REG-2026-0001",
    "deedHash":"0xabc123",
    "verifiedIdentityToken":"<token-from-verify-otp>"
  }'
```

## Production Notes

For real deployment in India:
- Replace mock OTP with authorized identity/KYC integrations.
- Add persistent DB (PostgreSQL), migrations, and secure secrets handling.
- Integrate digital signatures, audit trails, and role-based access controls.
- Align with Registration Act, state land revenue procedures, and DPDP requirements.
## Local (Non-Docker) Run

Please use `LOCAL_RUN.md` for step-by-step local setup and testing commands.

Quick start:
```bash
./run-local.sh
```
