#!/usr/bin/env bash
# MDir Image & Video Organizer - Debug launcher (module-path)
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TARGET_DIR="$REPO_ROOT/target"
CLASSES_DIR="$TARGET_DIR/classes"
MODULE_DEPS_DIR="$TARGET_DIR/module-deps"
BUILD_ARGS=(-Pdebug-modulepath package)

if [[ "${1:-}" == "-SkipTests" ]]; then
  BUILD_ARGS+=( -DskipTests )
fi

log() { printf '[debug] %s\n' "$*"; }

need_cmd() {
  command -v "$1" >/dev/null 2>&1 || { echo "Required command not found: $1" >&2; exit 1; }
}

need_cmd mvn
need_cmd java

log "Building project with Maven (debug-modulepath profile)"
mvn "${BUILD_ARGS[@]}"

[[ -d "$CLASSES_DIR" ]] || { echo "Compiled classes directory not found: $CLASSES_DIR" >&2; exit 1; }
[[ -d "$MODULE_DEPS_DIR" ]] || { echo "Module dependency directory not found: $MODULE_DEPS_DIR" >&2; exit 1; }

log "Launching module com.girbola/com.girbola.Launcher"
exec java --enable-native-access=javafx.graphics --enable-native-access=ALL-UNNAMED \
  --module-path "$CLASSES_DIR:$MODULE_DEPS_DIR" \
  -m com.girbola/com.girbola.Launcher

