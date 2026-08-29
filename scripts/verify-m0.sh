#!/usr/bin/env bash
set -euo pipefail

./scripts/check-no-secrets.sh
./gradlew --version
./gradlew test lint assembleDebug assembleDebugAndroidTest

if git grep -nE 'https?://|wss?://' -- '*.kt' \
  | grep -v 'https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/Airplane.glb'; then
  echo 'Unexpected network endpoint found in M0 Kotlin source.' >&2
  exit 1
fi

echo 'M0 static verification passed.'
