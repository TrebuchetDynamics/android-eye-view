# Free Keyless Globe M0 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the mandatory billed Google Maps 3D path with a free-to-run, keyless globe renderer and prove the defining Android Eye View interaction at 5,000 moving contacts on the reference phone.

**Architecture:** Keep Kotlin, Compose, `core-map`, the native M0 console, and provider-neutral map contracts. Replace `maps3d-adapter` with a hardened Android WebView adapter that loads a locally bundled MapLibre GL JS renderer; use the keyless OpenFreeMap Liberty style for OSM vector data and 3D building extrusions. Kotlin owns app state and synthetic contacts; the renderer receives bounded JSON commands and reports readiness, selection, gestures, and timing through a narrow bridge.

**Tech Stack:** Kotlin 2.3.20, Compose BOM 2026.06.01, AndroidX WebKit 1.17.0, Kotlin serialization JSON 1.11.0, MapLibre GL JS 6.6.0, Vite 8.2.2, Vitest 4.1.11, TypeScript 7.0.2, OpenFreeMap Liberty.

## Global Constraints

- The default app must require no billing account, API key, token, registration, or paid service.
- The default map style is `https://tiles.openfreemap.org/styles/liberty`; preserve its OpenMapTiles/OpenStreetMap attribution control.
- Bundle executable JavaScript and CSS inside the APK; only map data, glyphs, sprites, and tiles may load from `https://tiles.openfreemap.org`.
- Keep `minSdk = 31`, `compileSdk = 37`, `targetSdk = 36`, JDK 21 host, and JVM 11 Android bytecode.
- Render exactly 5,000 deterministic contacts from seed `0xA11CE`; use one GeoJSON source, one circle layer, and one collision-managed label layer rather than 5,000 DOM or Compose nodes.
- Keep the density budget at 5,000 entities and 600 label candidates; collision and zoom rules determine how many are visible.
- User gesture ownership always outranks follow and ambient camera motion.
- Represent the selected aircraft with a locally generated vector aircraft marker during M0; document that this is the free renderer's functional fallback for an unsupported first-class glTF model layer.
- Presentation filters must say `Visual simulation — not sensor imagery` and must not cover map attribution.
- Do not integrate production public-data providers during M0.
- Do not retain `play-services-maps3d`, the Google Maps secrets plugin, a Maps API manifest key, or a committed secret/config placeholder.
- Create M1–M4 GitHub issues only after the free M0 decision is `GO` or a bounded `CONDITIONAL GO`.

## Decision Record

- Google Maps 3D SDK is rejected as the mandatory renderer because it requires a billed Google Maps Platform project and its 0.2.2 build terminates the process on missing/invalid authentication.
- MapLibre Native Android 13.6.0 is rejected for this M0 because it has no supported globe projection, terrain mesh, or first-class model layer.
- NASA WorldWind Android and WhirlyGlobe-Maply are rejected because their Android paths are not actively maintained.
- MapLibre GL JS 6.6.0 is selected because it is BSD-3-Clause, supports globe projection, globe terrain rendering paths, circles, symbols, lines, fill extrusions, custom layers, feature queries, and camera control without a proprietary account.
- OpenFreeMap is selected for M0 because its hosted Liberty style is keyless and currently advertises no registration, request limit, or billing. Its no-SLA status and self-host escape hatch must remain documented.

## Status Snapshot — 2026-08-29

- Tasks 1–5: implemented and validated locally; the billed adapter is removed, the web command core and MapLibre globe are bundled, the hardened adapter is tested, and the free renderer is integrated into Compose.
- Task 6: CI and local verification are implemented; final clean-tree execution and remote workflow verification remain.
- Task 7: partial. The reference device renders the globe with visible attribution, 5,000 contacts, a 600-label candidate budget, and ten movement ticks. Selection/trail/follow gesture cancellation, city building extrusion, lifecycle/recovery, presentation readability, and thermal/memory soak remain.
- Task 8: product documents are aligned and issue creation remains correctly gated because the decision is still `PENDING`; commit, push, and remote checks remain.
- Physical validation found and fixed an Android WebView viewport race: root percentage/viewport-unit heights initially computed to zero despite a valid `window.innerHeight`. The renderer now sizes `html`, `body`, and `#map` before MapLibre construction.

## File Structure

- `web-map/package.json`: pinned renderer/test/build dependencies and scripts.
- `web-map/package-lock.json`: reproducible npm dependency graph.
- `web-map/tsconfig.json`: strict TypeScript configuration.
- `web-map/vite.config.ts`: deterministic APK asset output to `app/src/main/assets/map`.
- `web-map/index.html`: local renderer entry point and restrictive CSP.
- `web-map/src/contracts.ts`: native command and bridge types.
- `web-map/src/entity-store.ts`: pure contact, selected-aircraft, and trail state.
- `web-map/src/renderer.ts`: MapLibre map/source/layer/camera implementation.
- `web-map/src/main.ts`: bridge installation and startup.
- `web-map/src/entity-store.test.ts`: deterministic state tests.
- `web-map/src/command-router.test.ts`: command dispatch tests with a fake renderer.
- `web-map/src/command-router.ts`: validates and routes native commands.
- `web-map-adapter/build.gradle.kts`: free Android adapter dependencies.
- `web-map-adapter/src/main/.../MapCommandEncoder.kt`: provider-neutral model to JSON command encoding.
- `web-map-adapter/src/main/.../WebMapController.kt`: `MapController` implementation over a JavaScript sink.
- `web-map-adapter/src/main/.../WebMapBridge.kt`: narrow `@JavascriptInterface` callback surface.
- `web-map-adapter/src/main/.../WebMapSession.kt`: loading/ready/failed/closed state owner.
- `web-map-adapter/src/main/.../SecureWebMapView.kt`: WebView hardening, asset loading, lifecycle, and destruction.
- `web-map-adapter/src/main/.../WebMapHost.kt`: Compose `AndroidView` wrapper.
- `web-map-adapter/src/test/...`: command, bridge, and session tests.
- `app/src/main/.../MainActivity.kt`: host the free renderer with no key gate.
- `app/src/main/.../M0FeasibilityViewModel.kt`: accept renderer metrics and preserve the synthetic harness.
- `docs/architecture/ADR-0001-free-keyless-globe.md`: permanent renderer decision.
- `docs/m0/FEASIBILITY.md`: free-renderer evidence and final decision.

---

### Task 1: Remove the mandatory billed renderer and encode the free decision

**Files:**
- Create: `docs/architecture/ADR-0001-free-keyless-globe.md`
- Modify: `settings.gradle.kts`
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/AndroidManifest.xml`
- Delete: `maps3d-adapter/`
- Delete: `local.defaults.properties`

**Interfaces:**
- Consumes: current `core-map` contracts and app module.
- Produces: module `:web-map-adapter`, AndroidX WebKit alias, Kotlin JSON alias, and a no-key manifest.

- [ ] **Step 1: Record the architecture decision**

Write the ADR with `Status: Accepted`, the free/no-billing constraint, the four candidates above, the chosen hybrid boundary, OpenFreeMap's no-SLA risk, attribution requirements, and the marker fallback for M0.

- [ ] **Step 2: Replace build wiring**

Use these exact catalog entries:

```toml
[versions]
androidxWebkit = "1.17.0"
serializationJson = "1.11.0"

[libraries]
androidx-webkit = { module = "androidx.webkit:webkit", version.ref = "androidxWebkit" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serializationJson" }
```

Remove `playServicesBase`, `playServicesMaps3d`, `secretsPlugin`, their libraries, and the secrets Gradle plugin. Replace `include(":maps3d-adapter")` with `include(":web-map-adapter")`. Replace the app project dependency and delete the `secrets { ... }` block.

- [ ] **Step 3: Make the manifest keyless**

Add `<uses-permission android:name="android.permission.INTERNET" />` and remove `com.google.android.geo.maps3d.API_KEY`. Keep the launcher activity and platform no-action-bar theme.

- [ ] **Step 4: Prove paid SDK removal**

Run:

```bash
./gradlew projects
! git grep -nE 'play-services-maps3d|mapsplatform.secrets|maps3d.API_KEY|DEFAULT_API_KEY' -- ':!docs/superpowers/plans/**'
./scripts/check-no-secrets.sh
```

Expected: `:web-map-adapter` is listed, no banned runtime dependency/config match is found, and the secret scan passes.

- [ ] **Step 5: Commit**

```bash
git add settings.gradle.kts gradle/libs.versions.toml app/build.gradle.kts app/src/main/AndroidManifest.xml web-map-adapter docs/architecture/ADR-0001-free-keyless-globe.md local.defaults.properties maps3d-adapter
git commit -m "refactor: replace billed Maps 3D foundation"
```

### Task 2: Build and test the local MapLibre command core

**Files:**
- Create: `web-map/package.json`
- Create: `web-map/package-lock.json`
- Create: `web-map/tsconfig.json`
- Create: `web-map/vite.config.ts`
- Create: `web-map/src/contracts.ts`
- Create: `web-map/src/entity-store.ts`
- Create: `web-map/src/entity-store.test.ts`
- Create: `web-map/src/command-router.ts`
- Create: `web-map/src/command-router.test.ts`

**Interfaces:**
- Consumes commands `renderEntities`, `removeEntities`, `renderSelectedAircraft`, `renderPolyline`, `setCamera`, `stopCameraMotion`, and `close`.
- Produces `EntityStore`, `CommandRouter`, `NativeCommand`, and `RendererPort` for the actual map.

- [ ] **Step 1: Pin the web toolchain**

Create a private npm package with scripts `test: vitest run`, `typecheck: tsc --noEmit`, and `build: vite build`. Pin `maplibre-gl` to `6.6.0`, `vite` to `8.2.2`, `vitest` to `4.1.11`, and `typescript` to `7.0.2`; run `npm --prefix web-map install --package-lock-only` and commit the lockfile.

- [ ] **Step 2: Write failing entity-store tests**

Cover replacement of exactly 5,000 stable IDs, removal by ID, GeoJSON `[longitude, latitude]` ordering, optional labels, selected-aircraft heading, a bounded trail, and immutable snapshots. Example assertion:

```ts
expect(store.contactsGeoJson()).toEqual({
  type: 'FeatureCollection',
  features: [{
    type: 'Feature',
    id: 'synthetic-00001',
    geometry: {type: 'Point', coordinates: [-122.379, 37.6213]},
    properties: {id: 'synthetic-00001', headingDegrees: 90, label: 'SIM-00001'}
  }]
});
```

- [ ] **Step 3: Run RED**

Run `npm --prefix web-map test -- --run src/entity-store.test.ts`.
Expected: fail because `EntityStore` does not exist.

- [ ] **Step 4: Implement the pure store**

Use a `Map<string, Contact>` for stable identity. Reject non-finite coordinates and IDs longer than 128 characters. Cap trail points at 120 and omit `label` when null.

- [ ] **Step 5: Write failing router tests**

Use a fake `RendererPort` and assert each command invokes only its matching method; malformed objects and unknown command types must throw `InvalidCommandError` without invoking the renderer.

- [ ] **Step 6: Implement the minimal router**

Define a discriminated `NativeCommand` union and a `CommandRouter.apply(input: unknown)` switch that validates the top-level object and command type before forwarding typed payloads.

- [ ] **Step 7: Verify and commit**

Run:

```bash
npm --prefix web-map ci
npm --prefix web-map test
npm --prefix web-map run typecheck
```

Expected: all web tests pass and TypeScript reports no errors.

```bash
git add web-map
git commit -m "feat: add free globe command core"
```

### Task 3: Implement the keyless globe renderer and APK asset build

**Files:**
- Create: `web-map/index.html`
- Create: `web-map/src/main.ts`
- Create: `web-map/src/renderer.ts`
- Modify: `web-map/vite.config.ts`
- Generate: `app/src/main/assets/map/**`

**Interfaces:**
- Consumes: `CommandRouter` and `RendererPort` from Task 2.
- Produces: `window.androidEyeView.applyCommand(command)`, local renderer assets, and callbacks `onReady()`, `onError(message)`, `onUserGesture()`, `onEntityClick(id)`, and `onMetric(name, value)`.

- [ ] **Step 1: Add a restrictive local document**

Set this CSP in `index.html`:

```html
<meta http-equiv="Content-Security-Policy" content="default-src 'none'; script-src 'self'; worker-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data: blob: https://tiles.openfreemap.org; connect-src https://tiles.openfreemap.org; font-src https://tiles.openfreemap.org;">
```

The body contains only `<div id="map"></div>` and `<script type="module" src="/src/main.ts"></script>`.

- [ ] **Step 2: Implement MapLibre startup**

Create one map with Liberty style, antialiasing, compact attribution, and no external token. Call `map.setProjection({type: 'globe'})`. On `load`, add:

- GeoJSON source `contacts` and circle layer `contacts-points`;
- collision-managed symbol layer `contacts-labels` using `label`;
- GeoJSON source/layer `selected-aircraft` using a locally drawn 64×64 aircraft image and `headingDegrees` rotation;
- GeoJSON source/layer `selected-trail` with a bounded line.

Keep OpenFreeMap/OpenMapTiles/OpenStreetMap attribution visible. Report `onReady()` only after sources/layers exist.

- [ ] **Step 3: Implement renderer mutations and camera**

`renderEntities` and `removeEntities` update the single contacts source through `setData`. `renderSelectedAircraft` updates only the selected source. `renderPolyline` updates only the trail source. Convert camera range to zoom with `clamp(log2(40075016.686 / rangeMeters), 1, 18)` and call `easeTo`; call `map.stop()` for cancellation.

- [ ] **Step 4: Implement interaction and metrics callbacks**

Query `contacts-points` on map click and report the feature ID. Report user ownership release on `dragstart`, `rotatestart`, `pitchstart`, and user-originated `zoomstart`. For entity render commands, record `performance.now()`, wait for two `requestAnimationFrame` callbacks, and emit `contacts-render-ms`. Emit a rolling `raf-p95-ms` every 120 frames.

- [ ] **Step 5: Build deterministic APK assets**

Configure Vite with `base: './'`, `build.outDir: '../app/src/main/assets/map'`, `emptyOutDir: true`, and sourcemaps disabled. Run:

```bash
npm --prefix web-map run build
find app/src/main/assets/map -type f -maxdepth 3 -print
```

Expected: local `index.html`, hashed JavaScript, and CSS assets; no external JavaScript URL.

- [ ] **Step 6: Verify web output and commit**

Run `npm --prefix web-map test && npm --prefix web-map run typecheck && npm --prefix web-map run build`.

```bash
git add web-map app/src/main/assets/map
git commit -m "feat: render keyless MapLibre globe"
```

### Task 4: Add the hardened Android WebView adapter with TDD

**Files:**
- Create: `web-map-adapter/build.gradle.kts`
- Create: `web-map-adapter/src/main/AndroidManifest.xml`
- Create: `web-map-adapter/src/main/java/com/trebuchetdynamics/androideyeview/webmap/MapCommandEncoder.kt`
- Create: `web-map-adapter/src/main/java/com/trebuchetdynamics/androideyeview/webmap/WebMapController.kt`
- Create: `web-map-adapter/src/main/java/com/trebuchetdynamics/androideyeview/webmap/WebMapBridge.kt`
- Create: `web-map-adapter/src/main/java/com/trebuchetdynamics/androideyeview/webmap/WebMapSession.kt`
- Create: `web-map-adapter/src/main/java/com/trebuchetdynamics/androideyeview/webmap/SecureWebMapView.kt`
- Create: `web-map-adapter/src/main/java/com/trebuchetdynamics/androideyeview/webmap/WebMapHost.kt`
- Create: corresponding tests under `web-map-adapter/src/test/`

**Interfaces:**
- Consumes: `MapController`, `MapEntity`, `MapPolyline`, and `MapCamera` from `:core-map`.
- Produces: `WebMapController : MapController`, `WebMapSession.state: StateFlow<WebMapSessionState>`, and Compose `WebMapHost`.

- [ ] **Step 1: Write failing encoder tests**

Assert exact command types, all 5,000 entities, stable IDs, longitude/latitude order, null-label omission, model URI non-forwarding for marker fallback, polyline points, finite cameras, and correct JSON escaping for hostile labels such as `"</script>\n`.

- [ ] **Step 2: Run RED**

Run `./gradlew :web-map-adapter:testDebugUnitTest --tests '*MapCommandEncoderTest'`.
Expected: unresolved `MapCommandEncoder`.

- [ ] **Step 3: Implement encoder and controller**

Build `JsonObject`/`JsonArray` values with Kotlin serialization rather than string concatenation. The controller sends only:

```kotlin
fun interface JavascriptSink {
    fun evaluate(script: String)
}
```

Each method emits `window.androidEyeView.applyCommand(<encoded object>);`. Make `close()` idempotent and refuse future commands after close.

- [ ] **Step 4: Write and implement bridge/session tests**

Tests must cover `Loading -> Ready`, `Loading -> Failed`, close idempotence, callbacks marshalled through an injected `postToMain`, entity click, gesture cancellation, metric forwarding, and ignored callbacks after close.

Use these states:

```kotlin
sealed interface WebMapSessionState {
    data object Loading : WebMapSessionState
    data class Ready(val controller: MapController) : WebMapSessionState
    data class Failed(val message: String) : WebMapSessionState
    data object Closed : WebMapSessionState
}
```

- [ ] **Step 5: Implement hardened host and lifecycle**

Use `WebViewAssetLoader` for `https://appassets.androidplatform.net/assets/map/index.html`. Enable JavaScript and DOM storage only. Disable file/content access, universal file URL access, mixed content, form data, multiple windows, and third-party cookies. Enable Safe Browsing where supported. Block top-level navigation outside the app-assets origin. Forward `onResume`, `onPause`, and final `destroy`; permit only one live WebView per session.

- [ ] **Step 6: Verify adapter and commit**

Run:

```bash
./gradlew :web-map-adapter:testDebugUnitTest :web-map-adapter:lintDebug :web-map-adapter:assembleDebug
```

Expected: all adapter tests, lint, and assembly pass.

```bash
git add web-map-adapter gradle/libs.versions.toml
git commit -m "feat: add hardened free globe adapter"
```

### Task 5: Integrate the free renderer into the Compose M0 console

**Files:**
- Modify: `app/src/main/java/com/trebuchetdynamics/androideyeview/MainActivity.kt`
- Modify: `app/src/main/java/com/trebuchetdynamics/androideyeview/M0FeasibilityViewModel.kt`
- Modify: `app/src/main/java/com/trebuchetdynamics/androideyeview/M0FeasibilityScreen.kt`
- Modify: `app/src/test/java/com/trebuchetdynamics/androideyeview/M0FeasibilityViewModelTest.kt`
- Modify: `app/src/androidTest/java/com/trebuchetdynamics/androideyeview/M0FeasibilityScreenTest.kt`

**Interfaces:**
- Consumes: `WebMapHost`, `WebMapSession`, and renderer metric callbacks.
- Produces: an immediately keyless app flow with globe readiness, 5,000 contacts, selection, trail, follow, presentation modes, and visible timing evidence.

- [ ] **Step 1: Write failing app tests**

Add ViewModel tests for `recordRendererMetric("contacts-render-ms", 42.5)` and `recordRendererMetric("raf-p95-ms", 18.2)`. Add Compose assertions for `FREE / KEYLESS`, renderer timing, and the aircraft marker fallback wording.

- [ ] **Step 2: Run RED**

Run `./gradlew :app:testDebugUnitTest :app:assembleDebugAndroidTest`.
Expected: unresolved renderer metric state/functions or missing UI copy.

- [ ] **Step 3: Replace the Google session route**

Remove package-manager API-key detection and all `maps3d` imports. Own one `WebMapSession`, pass callbacks through `WebMapHost`, and bind the ready `MapController` to the existing ViewModel. Keep controller/session closure idempotent on final activity destruction.

- [ ] **Step 4: Expose honest free-renderer state**

Show `FREE / KEYLESS`, `MapLibre globe`, renderer update time, RAF p95, and `Aircraft marker fallback — not a glTF model`. Keep the presentation disclaimer and attribution-safe bottom clipping.

- [ ] **Step 5: Validate app tests**

Run:

```bash
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest
ANDROID_SERIAL=RFCX81EJPNN ./gradlew :app:connectedDebugAndroidTest
```

Expected: JVM/lint/build pass and all Compose tests pass on SM-S928B.

- [ ] **Step 6: Commit**

```bash
git add app
git commit -m "feat: run M0 on a free keyless globe"
```

### Task 6: Make local and CI verification build both runtimes

**Files:**
- Modify: `.github/workflows/android.yml`
- Modify: `.github/workflows/secret-scan.yml`
- Modify: `scripts/verify-m0.sh`
- Modify: `.gitignore`

**Interfaces:**
- Consumes: npm lockfile, web tests/build, Gradle tests/lint/build, free Gitleaks Docker CLI.
- Produces: reproducible no-billing CI and one local M0 verification command.

- [ ] **Step 1: Add Node verification before Gradle**

Use `actions/setup-node@v4` with Node 24 and npm cache keyed to `web-map/package-lock.json`. Run `npm --prefix web-map ci`, `test`, `typecheck`, and `build` before `./gradlew test lint assembleDebug assembleDebugAndroidTest`.

- [ ] **Step 2: Expand local verification**

Make `scripts/verify-m0.sh` run the same npm commands, assert no banned Google Maps 3D runtime references outside historical plans/reports, run the pinned free Gitleaks image when Docker is available, then run Gradle.

- [ ] **Step 3: Verify and commit**

Run `./scripts/verify-m0.sh`.
Expected: `M0 static verification passed.`

```bash
git add .github scripts .gitignore
git commit -m "ci: validate bundled free globe runtime"
```

### Task 7: Execute the physical-device free M0 matrix

**Files:**
- Modify: `docs/m0/README.md`
- Modify: `docs/m0/FEASIBILITY.md`
- Do not commit: screenshots, raw logs, benchmark dumps, APKs, or device serials.

**Interfaces:**
- Consumes: debug APK and connected SM-S928B.
- Produces: quantitative GO/CONDITIONAL GO/NO-GO evidence.

- [ ] **Step 1: Establish a clean device baseline**

Record model family, Android/API level, WebView package/version, Play Services only as environment context, thermal status, app revision, and network type. Clear the crash buffer and app data before the cold run.

- [ ] **Step 2: Prove the keyless globe and attribution**

Install and cold-launch without any secret/config file. Confirm globe readiness, OpenFreeMap/OpenMapTiles/OpenStreetMap attribution visibility, gesture interaction, 3D buildings at zoom 14+, and no crash. Capture a temporary screenshot for inspection, then delete it after recording results.

- [ ] **Step 3: Benchmark 5,000 contacts**

Load contacts and record Kotlin preparation time, `contacts-render-ms`, RAF p95, and process PSS. Run ten one-second ticks and record median/p95 renderer update, missed-frame symptoms, selection response, selected marker movement, 120-point trail bound, follow behavior, and immediate gesture cancellation.

Pass targets:

- globe ready within 10 seconds on the reference network;
- initial 5,000-contact renderer completion within 2 seconds;
- median movement update within 250 ms and p95 within 500 ms;
- RAF p95 at or below 32 ms during active movement;
- selection visibly responds within 250 ms;
- no crash, ANR, or unbounded memory growth.

- [ ] **Step 4: Exercise lifecycle and failure paths**

Run background/resume, activity recreation, process kill/relaunch, network-off startup, and network recovery. Verify one WebView only, no stale callback revives a closed session, and failure copy remains actionable without suggesting a paid key.

- [ ] **Step 5: Run a ten-minute thermal/memory soak**

Keep motion enabled for ten minutes. Record start/end thermal status, PSS, renderer metrics, and whether Android reports severe thermal throttling. Stop immediately on device warning or instability.

- [ ] **Step 6: Inspect all presentation modes**

Check CRT, NVG, monochrome, snow, and thermal-inspired overlays for legibility, gesture pass-through, honest disclaimer, and unobscured attribution.

- [ ] **Step 7: Record the decision**

Set `GO` only if every mandatory target passes. Set `CONDITIONAL GO` only for a bounded, documented limitation that does not invalidate the free keyless mission. Set `NO-GO` for unstable 5,000-contact interaction, hidden attribution, lifecycle crashes, mandatory account/billing, or loss of globe behavior.

- [ ] **Step 8: Commit evidence**

```bash
git add docs/m0 README.md ROADMAP.md PRD.md
git commit -m "docs: record free globe M0 decision"
```

### Task 8: Convert the proven roadmap and ship

**Files:**
- Modify: `README.md`
- Modify: `ROADMAP.md`
- Modify: `PRD.md`
- Create remotely: M1–M4 GitHub issues only after an allowed decision.

**Interfaces:**
- Consumes: final M0 decision and committed evidence.
- Produces: an accurate public project status, independently grabbable later milestones, green CI, and pushed commits.

- [ ] **Step 1: Update product documents**

Replace mandatory Google Maps 3D language with the local MapLibre GL JS/OpenFreeMap default, the self-host escape hatch, and the aircraft marker fallback. Keep Google imagery/SDK out of required functionality. State OpenFreeMap's no-SLA risk and required attribution.

- [ ] **Step 2: Gate issue creation**

If the decision is `PENDING` or `NO-GO`, create no M1–M4 issues. If it is `GO` or bounded `CONDITIONAL GO`, use the roadmap to create separately assignable issues with acceptance criteria, dependencies, test evidence, responsible-use requirements, and no production provider credentials.

- [ ] **Step 3: Run final verification**

Run:

```bash
./scripts/verify-m0.sh
git diff --check
git status --short
gh run list --repo TrebuchetDynamics/android-eye-view --branch main --limit 5
```

Expected: local verification passes and no unexplained files are staged or untracked.

- [ ] **Step 4: Commit and push**

```bash
git add README.md ROADMAP.md PRD.md docs .github scripts web-map web-map-adapter app core-map settings.gradle.kts gradle/libs.versions.toml
git diff --cached --check
git commit -m "docs: align roadmap with free keyless globe"
git push origin main
```

- [ ] **Step 5: Verify remote checks**

Wait for the pushed Android and Secret scan workflows, inspect any failure logs, fix root causes, and repeat until both workflows are green on the final main revision.

## Self-Review Results

- Spec coverage: free/no-billing startup, globe, OSM basemap, 3D buildings, 5,000 contacts, movement, selection, marker fallback, trail, follow, gesture cancellation, presentation honesty, attribution, lifecycle, performance, thermal behavior, CI, and issue gating each map to a task.
- Intentional M0 gap: first-class glTF is replaced by an explicitly labeled marker fallback because the selected free renderer has no first-class model layer; a later custom-layer issue is allowed only after M0 GO.
- Placeholder scan: no implementation step uses `TBD`, `TODO`, or an unspecified error-handling instruction.
- Type consistency: the web command names, bridge callbacks, Kotlin `MapController`, and `WebMapSessionState` names are consistent across Tasks 2–5.
