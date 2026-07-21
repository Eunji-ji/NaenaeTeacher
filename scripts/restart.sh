#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [[ ! -f .env.prod ]]; then
  echo "ERROR: .env.prod is missing." >&2
  exit 1
fi

docker compose -f docker-compose.prod.yml --env-file .env.prod restart

echo "Containers restarted. Check health with: curl http://127.0.0.1:8081/actuator/health"
