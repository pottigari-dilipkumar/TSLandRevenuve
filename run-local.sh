#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "Starting backend (Spring Boot) in background..."
(cd "$ROOT_DIR/app-backend" && mvn spring-boot:run) &
BACKEND_PID=$!

echo "Starting frontend (python http.server) in background..."
(cd "$ROOT_DIR/frontend" && python3 -m http.server 8081) &
FRONTEND_PID=$!

cleanup() {
  echo "Stopping services..."
  kill "$BACKEND_PID" "$FRONTEND_PID" >/dev/null 2>&1 || true
}

trap cleanup EXIT INT TERM

echo "Frontend: http://localhost:8081"
echo "Backend : http://localhost:8080"
echo "Press Ctrl+C to stop."

wait
