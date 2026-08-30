#!/usr/bin/env bash
set -euo pipefail

./scripts/check-no-secrets.sh

npm --prefix web-map ci
npm --prefix web-map test
npm --prefix web-map run typecheck

bundle_snapshot="$(mktemp -d)"
trap 'rm -rf "$bundle_snapshot"' EXIT
cp -a app/src/main/assets/map/. "$bundle_snapshot/"
npm --prefix web-map run build
diff -ru "$bundle_snapshot" app/src/main/assets/map

./gradlew --version
./gradlew test lint assembleDebug assembleDebugAndroidTest

if grep -RInE \
  --exclude-dir=build --exclude-dir=node_modules \
  'play-services-maps3d|com\.google\.android\.gms\.maps3d|MAPS3D_API_KEY|maps3d-adapter' \
  settings.gradle.kts build.gradle.kts gradle/libs.versions.toml \
  app/build.gradle.kts app/src/main core-map/src/main \
  web-map-adapter/build.gradle.kts web-map-adapter/src/main \
  web-map/package.json web-map/src; then
  echo 'Billed Google Maps 3D runtime reference found in production scope.' >&2
  exit 1
fi

if grep -RInE --include='*.kt' 'https?://|wss?://' \
  app/src/main core-map/src/main web-map-adapter/src/main; then
  echo 'Unexpected network endpoint found in Kotlin source.' >&2
  exit 1
fi

unexpected_web_endpoint="$({
  grep -RInE 'https?://|wss?://' web-map/src web-map/index.html || true
} | grep -v 'https://tiles.openfreemap.org' || true)"
if [[ -n "$unexpected_web_endpoint" ]]; then
  printf '%s\n' "$unexpected_web_endpoint"
  echo 'Unexpected endpoint found in the bundled renderer source.' >&2
  exit 1
fi

echo 'M0 static verification passed.'
