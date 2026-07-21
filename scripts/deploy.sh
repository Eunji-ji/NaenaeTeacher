#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

COMPOSE=(docker compose -f docker-compose.prod.yml --env-file .env.prod)
HEALTH_URL="http://127.0.0.1:8081/actuator/health"

if [[ ! -f .env.prod ]]; then
  echo "ERROR: .env.prod is missing. Copy .env.prod.example and set production secrets first." >&2
  exit 1
fi

command -v git >/dev/null || { echo "ERROR: git is not installed." >&2; exit 1; }
command -v docker >/dev/null || { echo "ERROR: docker is not installed." >&2; exit 1; }
command -v curl >/dev/null || { echo "ERROR: curl is not installed." >&2; exit 1; }
docker compose version >/dev/null

echo "Starting NaenaeTeacher deployment..."
git pull --ff-only origin main

"${COMPOSE[@]}" config --quiet
"${COMPOSE[@]}" build --pull app
"${COMPOSE[@]}" up -d --remove-orphans

echo "Waiting for the application health check on port 8081..."
for attempt in {1..60}; do
  if curl --fail --silent --show-error "$HEALTH_URL" >/dev/null; then
    echo "Deployment completed successfully. Health status: UP"
    exit 0
  fi
  sleep 2
done

echo "ERROR: Health check failed after 120 seconds." >&2
echo "Review the application logs with: bash scripts/logs.sh app" >&2
"${COMPOSE[@]}" ps
"${COMPOSE[@]}" logs --tail=100 app
exit 1
