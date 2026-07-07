#!/bin/sh
set -e

GRADLE_VERSION="${GRADLE_VERSION:-8.10.2}"
BASE_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
export GRADLE_USER_HOME="$BASE_DIR/.gradle"
DIST_DIR="$BASE_DIR/.gradle/wrapper/dists/gradle-$GRADLE_VERSION-bin"
GRADLE_HOME="$DIST_DIR/gradle-$GRADLE_VERSION"
ZIP_FILE="$DIST_DIR/gradle-$GRADLE_VERSION-bin.zip"

if [ -f "$BASE_DIR/gradle.properties" ]; then
  PROJECT_JAVA_HOME="$(sed -n 's/^org.gradle.java.home=//p' "$BASE_DIR/gradle.properties" | tail -n 1)"
  if [ -n "$PROJECT_JAVA_HOME" ]; then
    export JAVA_HOME="$PROJECT_JAVA_HOME"
    export PATH="$JAVA_HOME/bin:$PATH"
  fi
fi

if [ ! -x "$GRADLE_HOME/bin/gradle" ]; then
  mkdir -p "$DIST_DIR"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o "$ZIP_FILE"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -O "$ZIP_FILE"
  else
    echo "curl or wget is required to download Gradle $GRADLE_VERSION." >&2
    exit 1
  fi
  unzip -q "$ZIP_FILE" -d "$DIST_DIR"
fi

exec "$GRADLE_HOME/bin/gradle" "$@"
