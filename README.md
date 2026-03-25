# India Land Revenue & Registration System (Scalable Deployment)

This project now includes a **high-concurrency ready deployment topology** with:
- Dockerized services
- Nginx load balancer in front of backend instances
- Horizontally scalable Spring Boot backend
- PostgreSQL with tuned HikariCP connection pooling
- Redis-backed Spring Cache (optional via `CACHE_TYPE`)

---

## High-Scalability Architecture

```text
                        +---------------------+
Users (Web/API Clients) |   Frontend (Nginx)  |  :8081
----------------------->|  static SPA + /api  |
                        +----------+----------+
                                   |
                                   v
                        +---------------------+
                        |   Nginx Load Balancer|  :8080
                        | (least_conn, keepalive)
                        +----------+----------+
                                   |
                +------------------+------------------+
                |                  |                  |
                v                  v                  v
         +-------------+    +-------------+    +-------------+
         | Spring Boot |    | Spring Boot |    | Spring Boot |
         | backend #1  |    | backend #2  |    | backend #3  |
         +------+------+    +------+------+    +------+------+
                |                  |                  |
                +------------------+------------------+
                                   |
                    +--------------+--------------+
                    |                             |
                    v                             v
           +-------------------+        +------------------+
           | PostgreSQL        |        | Redis Cache      |
           | (system of record)|        | (hot reads cache)|
           +-------------------+        +------------------+
```

### Why this handles high concurrency
- **Horizontal API scale:** multiple backend containers share incoming requests via Nginx.
- **Smart load balancing:** `least_conn` reduces tail latency under uneven request load.
- **DB efficiency:** HikariCP settings prevent connection storms and improve reuse.
- **Caching:** frequent read paths can be served from Redis to reduce DB/API pressure.
- **Keepalive + tuned worker connections:** supports a high volume of concurrent clients.

---

## Services in `docker-compose.yml`

- `frontend`: serves UI on `http://localhost:8081`
- `nginx-lb`: API load balancer on `http://localhost:8080`
- `backend`: Spring Boot service (scale this service)
- `postgres`: durable transactional store
- `redis`: cache store for hot data

---

## Deployment Steps

### 1) Build and start all services
```bash
docker compose up -d --build
```

### 2) Scale backend instances (example: 3)
```bash
docker compose up -d --scale backend=3
```

### 3) Verify scaled backend containers
```bash
docker compose ps
```

You should see multiple `backend` containers (e.g. `backend-1`, `backend-2`, `backend-3`) behind `nginx-lb`.

### 4) Validate load-balanced API
```bash
curl http://localhost:8080/api/public/verify/REG-2026-0001
```

### 5) Access endpoints
- Frontend: `http://localhost:8081`
- Backend through load balancer: `http://localhost:8080`
- OpenAPI docs: `http://localhost:8080/swagger-ui.html`

---

## Runtime Configuration

### Database pooling (HikariCP)
Configured in `application.properties` with env overrides:
- `DB_POOL_MAX_SIZE` (default `50`)
- `DB_POOL_MIN_IDLE` (default `10`)
- `DB_POOL_CONNECTION_TIMEOUT_MS` (default `30000`)

### Cache mode
- `CACHE_TYPE=redis` for distributed cache
- `CACHE_TYPE=simple` for local in-memory cache fallback

Redis connection:
- `REDIS_HOST` (default `localhost`)
- `REDIS_PORT` (default `6379`)

---

## Notes for Production Hardening

For very high throughput in production, additionally consider:
- external managed PostgreSQL + read replicas,
- Redis Sentinel/Cluster,
- autoscaling via Kubernetes HPA,
- observability (Prometheus/Grafana, distributed tracing),
- circuit breakers + rate limiting,
- stateless auth with shared session/token strategy.
