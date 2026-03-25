# Land Revenue & Registration System

A multi-module repository containing:

- **Spring Boot backend** (`app-backend/`) with:
  - Clean architecture land/revenue APIs (`/api/auth`, `/api/owners`, `/api/lands`, `/api/revenues`)
  - Aadhaar OTP demo flow (`/api/auth/aadhaar/*`)
  - Registration demo flow (`/api/registrations`, `/api/public/verify/*`)
  - Platform simulation APIs (`/api/platform/*`)
- **React frontend** (`frontend/`) built with Vite + Tailwind
- **Legacy blueprint artifacts** (`backend/api_spec.yaml`, `backend/schema.sql`)
- **Smart contract artifact** (`smart-contracts/LandRegistry.sol`)

---

## 1) Backend Documentation

## 1.1 API Endpoints (request/response)

Base URL (local): `http://localhost:8080`

OpenAPI docs from Spring Boot:
- JSON: `http://localhost:8080/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

> The backend currently exposes **three API groups** in one runtime: Clean APIs, Aadhaar/Registration demo APIs, and Platform simulation APIs.

### A. Clean APIs (secured with JWT + role checks)

#### Auth

##### `POST /api/auth/login`
**Request**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Response 200**
```json
{
  "token": "<jwt>",
  "role": "ADMIN"
}
```

#### Owners

##### `POST /api/owners` (roles: `ADMIN`, `DATA_ENTRY`)
**Request**
```json
{
  "name": "Ravi Kumar",
  "nationalId": "ID-12345"
}
```

**Response 200**
```json
{
  "id": 1,
  "name": "Ravi Kumar",
  "nationalId": "ID-12345"
}
```

##### `GET /api/owners` (roles: `ADMIN`, `REVENUE_OFFICER`, `DATA_ENTRY`)
Returns Spring `Page<OwnerResponse>`.

#### Land Records

##### `POST /api/lands` (roles: `ADMIN`, `DATA_ENTRY`)
**Request**
```json
{
  "surveyNumber": "SV-2026-001",
  "district": "Pune",
  "village": "Mulshi",
  "areaInAcres": 1.75,
  "ownerId": 1
}
```

**Response 200**
```json
{
  "id": 10,
  "surveyNumber": "SV-2026-001",
  "district": "Pune",
  "village": "Mulshi",
  "areaInAcres": 1.75,
  "ownerId": 1,
  "ownerName": "Ravi Kumar"
}
```

##### `GET /api/lands` (roles: `ADMIN`, `REVENUE_OFFICER`, `DATA_ENTRY`, `CITIZEN`)
Returns Spring `Page<LandRecordResponse>`.

##### `GET /api/lands/{id}` (roles: `ADMIN`, `REVENUE_OFFICER`, `DATA_ENTRY`, `CITIZEN`)
Returns `LandRecordResponse`.

##### `PUT /api/lands/{id}` (roles: `ADMIN`, `DATA_ENTRY`)
Request schema same as create.

##### `DELETE /api/lands/{id}` (role: `ADMIN`)
Response: empty body.

#### Revenue Records

##### `POST /api/revenues` (roles: `ADMIN`, `REVENUE_OFFICER`)
**Request**
```json
{
  "amount": 5000,
  "paymentDate": "2026-03-25",
  "paymentReference": "TXN-1001",
  "landRecordId": 10
}
```

**Response 200**
```json
{
  "id": 22,
  "amount": 5000,
  "paymentDate": "2026-03-25",
  "paymentReference": "TXN-1001",
  "landRecordId": 10
}
```

##### `GET /api/revenues` (roles: `ADMIN`, `REVENUE_OFFICER`, `CITIZEN`)
Returns Spring `Page<RevenueRecordResponse>`.

---

### B. Aadhaar + Registration Demo APIs (mock flow)

#### Aadhaar OTP

##### `POST /api/auth/aadhaar/send-otp`
**Request**
```json
{
  "aadhaarNumber": "234567890123"
}
```

**Response 200**
```json
{
  "message": "OTP sent successfully (demo mode)",
  "demoOtp": "123456"
}
```

##### `POST /api/auth/aadhaar/verify-otp`
**Request**
```json
{
  "aadhaarNumber": "234567890123",
  "otp": "123456"
}
```

**Response 200**
```json
{
  "message": "Identity verified",
  "verifiedIdentityToken": "<uuid>"
}
```

#### Registrations

##### `POST /api/registrations`
Requires a valid `verifiedIdentityToken` from OTP flow.

**Request**
```json
{
  "parcelId": "PCL-1001",
  "sellerName": "Ravi Kumar",
  "buyerName": "Anita Sharma",
  "registrationRef": "REG-2026-0001",
  "deedHash": "0xabc123",
  "verifiedIdentityToken": "<token>"
}
```

**Response 201**
```json
{
  "registrationRef": "REG-2026-0001",
  "parcelId": "PCL-1001",
  "sellerName": "Ravi Kumar",
  "buyerName": "Anita Sharma",
  "deedHash": "0xabc123",
  "verifiedIdentityToken": "<token>",
  "createdAt": "2026-03-25T12:00:00Z",
  "status": "SUBMITTED"
}
```

##### `GET /api/public/verify/{registrationRef}`
Returns the same `RegistrationRecord` payload.

---

### C. Platform Simulation APIs (`/api/platform/*`)

##### `POST /api/platform/auth/sign-in`
Request:
```json
{ "username": "admin", "password": "admin123" }
```
Response includes `demoOtp`, contact details.

##### `POST /api/platform/auth/verify-2fa`
Request:
```json
{ "username": "admin", "otp": "123456" }
```
Response:
```json
{ "sessionToken": "<uuid>", "role": "ADMIN" }
```

##### `GET /api/platform/users`
Returns array of `SystemUser`.

##### `POST /api/platform/lands`
Creates land with polygon and starter history/documents.

##### `GET /api/platform/lands`
Returns all platform lands.

##### `GET /api/platform/lands/{landId}`
Returns one land parcel.

##### `GET /api/platform/lands/{landId}/history`
Returns land event history.

##### `GET /api/platform/lands/{landId}/documents/{documentId}/download`
Downloads simulated document content as binary attachment.

##### `POST /api/platform/notifications/config`
Request:
```json
{ "emailEnabled": true, "smsEnabled": true }
```

##### `GET /api/platform/notifications/config`
Returns current notification config.

---

## 1.2 Authentication Flow

### Clean API JWT flow
1. Client calls `POST /api/auth/login`.
2. Backend validates credentials from `users` table (BCrypt).
3. Backend returns JWT with `sub=username` and `role` claim.
4. Client sends `Authorization: Bearer <token>` on protected API calls.
5. `JwtAuthenticationFilter` parses JWT and sets Spring Security context.
6. `@PreAuthorize` validates role authorization.

Default seeded users (local/dev):
- `admin` / `admin123` → `ADMIN`
- `officer` / `officer123` → `REVENUE_OFFICER`
- `entry` / `entry123` → `DATA_ENTRY`
- `citizen` / `citizen123` → `CITIZEN`

### Aadhaar demo flow
1. `send-otp` returns a demo OTP in API response.
2. `verify-otp` returns `verifiedIdentityToken` (UUID) valid for 30 minutes.
3. `POST /api/registrations` accepts only valid, non-expired token.

### Platform simulation 2FA flow
1. `sign-in` validates static in-memory credentials and generates demo OTP.
2. `verify-2fa` returns `sessionToken` and role.
3. Token is stored in memory (demo behavior).

---

## 1.3 DB Schema

### Runtime schema (from JPA entities in `app-backend`)

`users`
- `id` BIGINT PK
- `username` UNIQUE NOT NULL
- `password` NOT NULL
- `role` VARCHAR NOT NULL

`owners`
- `id` BIGINT PK
- `name` NOT NULL
- `national_id` UNIQUE NOT NULL

`land_records`
- `id` BIGINT PK
- `survey_number` UNIQUE NOT NULL
- `district` NOT NULL
- `village` NOT NULL
- `area_in_acres` DECIMAL(12,2) NOT NULL
- `owner_id` FK → owners.id NOT NULL

`revenue_records`
- `id` BIGINT PK
- `amount` DECIMAL(12,2) NOT NULL
- `payment_date` DATE NOT NULL
- `payment_reference` NOT NULL
- `land_record_id` FK → land_records.id NOT NULL

> Hibernate is currently set to `spring.jpa.hibernate.ddl-auto=update`.

### Legacy blueprint schema
`backend/schema.sql` contains an expanded design with:
- `owners`, `parcels`, `registrations`, `mutations`, `revenue_demands`

This is a broader conceptual schema and is not the direct source for current JPA runtime table generation.

---

## 2) Frontend Documentation

Primary frontend is the React app in `frontend/`.

## 2.1 Folder Structure

```text
frontend/
├── src/
│   ├── api/
│   │   ├── client.js
│   │   ├── authApi.js
│   │   └── landApi.js
│   ├── components/
│   │   ├── Alert.jsx
│   │   ├── ProtectedRoute.jsx
│   │   └── StatCard.jsx
│   ├── layouts/
│   │   ├── AuthLayout.jsx
│   │   └── MainLayout.jsx
│   ├── pages/
│   │   ├── DashboardPage.jsx
│   │   ├── LandFormPage.jsx
│   │   ├── LandRecordsPage.jsx
│   │   ├── LoginPage.jsx
│   │   ├── NotFoundPage.jsx
│   │   ├── RegisterPage.jsx
│   │   ├── RevenueDetailsPage.jsx
│   │   └── UserManagementPage.jsx
│   ├── store/
│   │   └── authStore.js
│   ├── utils/
│   │   └── roles.js
│   ├── App.jsx
│   ├── main.jsx
│   └── index.css
├── Dockerfile
├── nginx.conf
├── package.json
├── vite.config.js
└── tailwind.config.js
```

## 2.2 Component Explanation

- `src/main.jsx`: React bootstrap + router mounting.
- `src/App.jsx`: route map, nested layouts, role-based route protection.
- `src/components/ProtectedRoute.jsx`: auth and role guard.
- `src/layouts/AuthLayout.jsx`: public auth page shell.
- `src/layouts/MainLayout.jsx`: authenticated shell, sidebar nav, logout.
- `src/store/authStore.js`: persisted auth state, login/register actions.
- `src/api/client.js`: Axios client with bearer-token interceptor.
- `src/pages/DashboardPage.jsx`: KPI cards + revenue trend chart (with fallback data).
- `src/pages/LandRecordsPage.jsx`: land table view (with fallback data).
- `src/pages/LandFormPage.jsx`: form to create/update land records.
- `src/pages/RevenueDetailsPage.jsx`: revenue summary cards.
- `src/pages/UserManagementPage.jsx`: user table.

> Note: some frontend calls (`/auth/register`, `/dashboard/stats`, `/revenue/trend`, `/revenue`, `/users`) are UI placeholders and may require backend alignment.

---

## 3) Setup

## 3.1 How to run backend

### Option A: Maven
```bash
cd app-backend
mvn spring-boot:run
```

Backend URL: `http://localhost:8080`

To pick an environment profile:
```bash
cd app-backend
SPRING_PROFILES_ACTIVE=local mvn spring-boot:run
```

### Option B: helper script (backend + static frontend)
```bash
./run-local.sh
```

## 3.2 How to run frontend

### Dev mode (Vite)
```bash
cd frontend
npm install
npm run dev
```

Vite URL: `http://localhost:5173`

### Static mode
```bash
cd frontend
python3 -m http.server 8081
```

Static URL: `http://localhost:8081`

## 3.3 Environment variables

### Backend env vars

| Variable | Default | Description |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `local` | Active Spring profile (`local`, `dev`, `qa`, `uat`, `preprod`, `prod`, `dr`) |
| `DB_URL` | `jdbc:postgresql://localhost:5432/land_revenue` | PostgreSQL JDBC URL |
| `DB_USERNAME` | `postgres` | PostgreSQL username |
| `DB_PASSWORD` | `postgres` | PostgreSQL password |
| `JWT_SECRET` | `ReplaceWithASecretKeyOfAtLeast32Characters` | JWT signing key |
| `JWT_EXPIRATION_MS` | `86400000` | JWT expiry in milliseconds |

### Frontend env vars

| Variable | Default | Description |
|---|---|---|
| `VITE_API_BASE_URL` | `http://localhost:8080/api` | Axios API base URL |

Example `.env` for frontend:
```bash
VITE_API_BASE_URL=http://localhost:8080/api
```

---

## 4) Deployment

## 4.1 Docker steps

From repository root:

```bash
docker compose up --build
```

Services:
- Backend: `http://localhost:8080`
- Frontend: `http://localhost:8081`

Detached mode:
```bash
docker compose up --build -d
```

Stop services:
```bash
docker compose down
```

## 4.2 Production setup

Recommended for production hardening:

1. Use managed PostgreSQL and secure networking.
2. Store `JWT_SECRET` and DB credentials in secret manager.
3. Replace `ddl-auto=update` with Flyway/Liquibase migrations.
4. Restrict CORS origins (avoid wildcard in production).
5. Add central logging/metrics/alerts.
6. Terminate TLS at ingress/reverse proxy.
7. Add CI/CD with test + vulnerability scanning.

---

## Additional Notes

- `backend/api_spec.yaml` documents legacy Aadhaar/registration APIs.
- `backend/schema.sql` contains expanded conceptual schema.
- `angular-frontend/` exists in repository but current primary UI is `frontend/` React app.
