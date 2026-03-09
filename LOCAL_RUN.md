# Local Run Instructions

## Prerequisites
- Java 17+
- Maven 3.9+
- Node.js 20+
- npm

## Run backend
```bash
cd app-backend
mvn spring-boot:run
```
Backend: `http://localhost:8080`

## Run Angular frontend
```bash
cd angular-frontend
npm install
npm start
```
Frontend: `http://localhost:4200`

## One command helper
```bash
./run-local.sh
```

## Login users (2FA enabled)
- Admin: `admin / admin123`
- SRO: `sro / sro123`
- Regular user (buyer/seller portal): `user / user123`

A demo OTP is returned by sign-in response for local testing.

## Main capabilities
- 2FA sign-in for all users
- Admin user listing and notification setup (Email/SMS)
- SRO land registration flow
- Non-overlap coordinate validation for new land polygons
- Land history view
- Legal document download endpoint for buyer/seller portal
