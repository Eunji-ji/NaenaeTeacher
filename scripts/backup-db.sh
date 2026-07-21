#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [[ ! -f .env.prod ]]; then
  echo "ERROR: .env.prod is missing." >&2
  exit 1
fi

COMPOSE=(docker compose -f docker-compose.prod.yml --env-file .env.prod)
BACKUP_DIR="$ROOT_DIR/backups"
TIMESTAMP="$(date +'%Y%m%d_%H%M%S')"
BACKUP_FILE="$BACKUP_DIR/naenae_teacher_${TIMESTAMP}.sql"
TEMP_FILE="${BACKUP_FILE}.tmp"

mkdir -p "$BACKUP_DIR"
umask 077
trap 'rm -f "$TEMP_FILE"' EXIT

if ! "${COMPOSE[@]}" ps --status running db | grep -q 'naenae-teacher-db'; then
  echo "ERROR: the PostgreSQL container is not running." >&2
  exit 1
fi

# POSTGRES_USER and POSTGRES_DB are read from the container environment, so
# secrets do not need to be parsed or echoed by this script.
"${COMPOSE[@]}" exec -T db sh -c 'exec pg_dump --clean --if-exists --no-owner --no-privileges -U "$POSTGRES_USER" "$POSTGRES_DB"' > "$TEMP_FILE"

if [[ ! -s "$TEMP_FILE" ]]; then
  echo "ERROR: pg_dump produced an empty backup." >&2
  exit 1
fi

mv "$TEMP_FILE" "$BACKUP_FILE"
trap - EXIT
echo "Backup created: $BACKUP_FILE"
