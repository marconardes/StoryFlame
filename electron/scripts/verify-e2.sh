#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
ELECTRON_DIR="$ROOT_DIR/electron"
PACKAGED_APP_DIR="$ELECTRON_DIR/dist/linux-unpacked"
PACKAGED_BRIDGE_DIR="$PACKAGED_APP_DIR/resources/storyflame-bridge"
PACKAGED_EXECUTABLE="$PACKAGED_APP_DIR/storyflame"

cd "$ROOT_DIR"
./gradlew :app:test :desktop:compileJava

cd "$ELECTRON_DIR"
node --check main.js
node --check preload.js
node --check renderer/renderer.js
node ./scripts/copy-bridge.js

if [[ -d "$PACKAGED_APP_DIR" ]]; then
  [[ -x "$PACKAGED_EXECUTABLE" ]]
  [[ -f "$PACKAGED_BRIDGE_DIR/bin/storyflame-bridge" ]]
  [[ -f "$PACKAGED_BRIDGE_DIR/lib/app-0.1.0-SNAPSHOT.jar" ]]
fi

echo "E2 verification completed."
