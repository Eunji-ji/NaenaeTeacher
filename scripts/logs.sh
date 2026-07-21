#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [[ ! -f .env.prod ]]; then
  echo "ERROR: .env.prod is missing." >&2
  exit 1
fi

# Optional first argument: app or db. With no argument, follow both services.
if [[ $# -gt 0 ]]; then
  docker compose -f docker-compose.prod.yml --env-file .env.prod logs -f --tail=200 "$1"
else
  docker compose -f docker-compose.prod.yml --env-file .env.prod logs -f --tail=200
fi
