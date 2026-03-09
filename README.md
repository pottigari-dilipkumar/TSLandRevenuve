# India Land Revenue & Registration System (Spring Boot + Angular + Docker)

## Description

This project is a full-stack starter for land registration workflows with role-based operations for **Admin**, **SRO**, and **Regular User (Buyer/Seller)**. It includes:
- Spring Boot backend APIs
- Angular frontend interface
- 2FA sign-in flow
- Land registration with geolocation polygon validation to prevent overlapping registrations
- Land history tracking
- Legal document download endpoint
- Notification settings for email/SMS

> Aadhaar/OTP and 2FA are demo-mode implementations for development. Production must use compliant identity providers and approved gateways.

## User Roles
- **Admin**: user management view, notification setup
- **SRO**: executes registration and land creation workflows
- **Regular User**: buyer/seller portal for land details, history, and document download

## Key Functional Scope
1. **2FA on sign-in** for all users
2. **Land detail capture** including seller, buyer, village, survey number
3. **Geo-coordinate polygon validation** to block overlapping land registration
4. **Land history timeline** retrieval
5. **Map-ready coordinate interface** in Angular UI
6. **Legal document download** from user portal
7. **Email/SMS notification setup** for application events

## Run with Docker
```bash
docker compose up --build
```
- Backend: `http://localhost:8080`
- Angular frontend: `http://localhost:4200`

## Local run
See `LOCAL_RUN.md`.
