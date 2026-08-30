# M0 Free-Globe Feasibility Runbook

M0 answers one question before Android Eye View integrates production data providers:

> Can the free, keyless Android globe sustain the lifecycle, density, marker fallback, trail, selection, camera-follow, attribution, recovery, and presentation foundations required by the God’s Eye View Android port?

The selected M0 renderer is locally bundled MapLibre GL JS 6.6 in one hardened Android WebView. It loads the keyless OpenFreeMap Liberty style from `tiles.openfreemap.org`. It requires no API key, token, billing account, registration, or paid map service. See [ADR-0001](../architecture/ADR-0001-free-keyless-globe.md).

## Static validation

From the repository root:

```bash
./scripts/verify-m0.sh
```

The command installs the locked web dependencies, runs web tests and TypeScript checks, verifies the generated renderer bundle, checks secret patterns and banned Maps 3D runtime references, then runs Android unit tests, lint, debug assembly, and instrumentation-test assembly.

## Runtime prerequisites

- A physical Android 12+ flagship-class device with at least 8 GB RAM.
- Current Android System WebView.
- Network access to `https://tiles.openfreemap.org`.
- USB debugging or an equivalent trusted development connection.

No map credential or cloud project is required. Do not add one.

## Build and install

```bash
npm --prefix web-map ci
npm --prefix web-map run build
./gradlew :app:installDebug
adb shell am start -n \
  com.trebuchetdynamics.androideyeview/.MainActivity
```

Expected startup state: `FREE / KEYLESS`, `MapLibre globe`, then `READY`. Confirm the spherical globe and the OpenFreeMap, OpenMapTiles, and OpenStreetMap attribution are visible.

## Runtime matrix

Record only summarized results in [FEASIBILITY.md](FEASIBILITY.md).

1. Cold-launch the application and wait for globe readiness.
2. Confirm the map is spherical, style content renders, attribution is visible, and no credential prompt appears.
3. Zoom to a mapped city and confirm Liberty style building extrusions render at an appropriate zoom.
4. Background and resume the app five times.
5. Tap **Load 5,000 contacts**; confirm 5,000 contacts and a 600-label candidate budget are reported. Visible labels may be lower because the layer is zoom-gated and collision-managed.
6. Orbit and zoom while contacts are present.
7. Run ten movement ticks and record Kotlin and renderer timings.
8. Select the synthetic aircraft; confirm the labeled marker fallback and bounded trail appear.
9. Start camera follow.
10. Touch and drag the globe; confirm follow stops immediately and direct navigation owns the camera.
11. Cycle Normal, CRT, NVG, Monochrome, Snow, and Thermal-inspired presentation modes.
12. Confirm `Visual simulation — not sensor imagery` remains visible in every simulated mode.
13. Disable connectivity while the app is running; confirm an honest failure or stale state, then restore connectivity and verify recovery.
14. Trigger an Android low-memory callback through development tooling.
15. Recreate the activity and verify one active WebView, valid camera state, and no duplicated renderer objects.
16. Terminate the renderer process where supported; confirm the app reports failure rather than a successful blank map.
17. Force-stop the app and confirm rendering and motion stop.
18. Run a ten-minute dense-scene soak while recording memory and thermal summaries.

## Required measurements

- Device model family, without hardware serial or advertising identifier.
- Android and Android System WebView versions.
- Git revision and MapLibre GL JS version.
- Cold interactive-globe time.
- 5,000-contact Kotlin load and renderer-commit durations.
- Median and worst movement-tick duration across ten ticks.
- Rendered contact and label counts.
- Rolling requestAnimationFrame p95 during interaction.
- Memory high-water mark.
- Android thermal status before and after the run.
- Selection feedback latency.
- Crash, ANR, renderer-process, and map/session error counts.

Raw captures belong under ignored `benchmark-results/` or `captures/`. Do not commit screenshots, raw logs, benchmark dumps, or physical device serials.

## Decision rule

- **GO:** every M0 exit gate passes on reference hardware.
- **CONDITIONAL GO:** the globe, marker fallback, trail, follow, lifecycle, attribution, recovery, and density gates pass; only a bounded nonessential presentation difference remains.
- **NO-GO:** lifecycle, attribution, 5,000-contact usability, marker fallback, trail, camera cancellation, keyless operation, or provider terms lack a safe solution.

OpenFreeMap is free and keyless but provides no service-level guarantee. A GO decision therefore also requires preserving the renderer/style boundary needed for a future compatible self-hosted endpoint. Later milestone issues are created only after a recorded GO or bounded CONDITIONAL GO.
