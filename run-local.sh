#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
JAVA17_HOME="$(/usr/libexec/java_home -v 17)"

echo "Starting backend (Spring Boot) in background..."
(cd "$ROOT_DIR/app-backend" && JAVA_HOME="$JAVA17_HOME" PATH="$JAVA17_HOME/bin:$PATH" mvn spring-boot:run) &
BACKEND_PID=$!

echo "Starting frontend (Vite) in background..."
(cd "$ROOT_DIR/frontend" && npm run dev -- --host 0.0.0.0 --port 5173) &
FRONTEND_PID=$!

cleanup() {
  echo "Stopping services..."
  kill "$BACKEND_PID" "$FRONTEND_PID" >/dev/null 2>&1 || true
}

trap cleanup EXIT INT TERM

echo "Frontend: http://localhost:5173"
echo "Backend : http://localhost:8080"
echo "Press Ctrl+C to stop."

wait
