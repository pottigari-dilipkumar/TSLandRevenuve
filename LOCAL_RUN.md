# Local Run Instructions

This file explains how to run the project **without Docker** on your local machine.

## Prerequisites

- Java 17+
- Maven 3.9+
- Python 3 (for quick static frontend hosting)
- Optional: Docker + Docker Compose (for containerized run)

## 1) Run Backend (Spring Boot)

```bash
cd app-backend
mvn spring-boot:run
```

Backend starts at:
- `http://localhost:8080`

Health check (basic):
```bash
curl http://localhost:8080/api/public/verify/test-ref
```
(Will return `Registration not found` until data is submitted, which confirms app is reachable.)

## 2) Run Frontend (Static App)

Open a new terminal:

```bash
cd frontend
python3 -m http.server 8081
```

Frontend URL:
- `http://localhost:8081`

The frontend calls backend at `http://localhost:8080`.

## 3) Test Aadhaar OTP + Registration Flow

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

## 4) Optional Docker Run

```bash
docker compose up --build
```

- Frontend: `http://localhost:8081`
- Backend: `http://localhost:8080`

## Notes

- Aadhaar OTP here is **demo/mock only** for local development.
- For production, integrate with legally compliant identity providers and secure OTP delivery channels.
