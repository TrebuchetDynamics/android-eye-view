# M0 Native Feasibility Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Prove that a native Kotlin/Compose application using Google Maps 3D SDK for Android can support Android Eye View’s lifecycle, entity-density, selection, model, trail, camera-follow, presentation, attribution, and credential-safety requirements before any production data provider is integrated.

**Architecture:** Build a small multi-module Android application with provider-neutral map contracts in `core-map`, an isolated Google SDK implementation in `maps3d-adapter`, and an M0-only feasibility surface in `app`. Pure policies and synthetic data generation remain JVM-testable; hardware-dependent rendering is exercised through an explicit debug harness and recorded in a committed evidence report.

**Tech Stack:** Kotlin 2.3.20, Jetpack Compose BOM 2026.06.01, Android Gradle Plugin 9.2.1, Gradle 9.4.1, JDK 21 host with JVM 11 Android bytecode, compile SDK 37, target SDK 36, minimum SDK 31, Google Play services Maps 3D 0.2.2, JUnit 4, AndroidX Test, GitHub Actions.

## Global Constraints

- Android Eye View is the native Android port of God’s Eye View.
- Minimum supported platform is Android 12 / API 31.
- No production data provider is integrated during M0.
- No real API key, token, secret, local SDK path, log, benchmark output, or generated APK is committed.
- Google Maps SDK types stay inside `maps3d-adapter`; `app` and `core-map` consume provider-neutral contracts.
- The Maps 3D SDK remains marked experimental in user-facing M0 copy and evidence.
- Required Google attribution must remain unobscured.
- A user gesture outranks and cancels programmatic camera ownership.
- Synthetic contact count is exactly 5,000 for the density gate.
- Presentation modes are labeled visual simulations and never described as real sensor imagery.
- M0 runtime success requires a valid Android-restricted Maps 3D API key and supported physical reference device.
- Every task uses focused commits after its validation passes.

---

## Planned file structure

```text
.github/
├── dependabot.yml                         Dependency update policy
└── workflows/
    ├── android.yml                        Unit, lint, and debug assembly CI
    └── secret-scan.yml                    Gitleaks scan
.gitignore                                 Android, IDE, key, SDK, and output exclusions
build.gradle.kts                           Root plugin declarations
settings.gradle.kts                        Repository policy and module graph
gradle.properties                          Reproducible Gradle defaults
gradle/libs.versions.toml                  Central version catalog
gradle/wrapper/*                           Gradle 9.4.1 wrapper
gradlew, gradlew.bat                       Wrapper launchers
local.defaults.properties                  Non-secret fallback key value
app/
├── build.gradle.kts                       Android application configuration
├── proguard-rules.pro                     Release shrinker rules
└── src/
    ├── main/
    │   ├── AndroidManifest.xml             App and API-key manifest wiring
    │   └── java/com/trebuchetdynamics/androideyeview/
    │       ├── MainActivity.kt             Composition root
    │       ├── M0FeasibilityScreen.kt      Debug harness and result controls
    │       ├── M0FeasibilityViewModel.kt   Harness orchestration and state
    │       ├── presentation/
    │       │   ├── SensorMode.kt           Presentation vocabulary
    │       │   └── SensorOverlay.kt        Native visual approximations
    │       └── ui/theme/Theme.kt           Minimal application theme
    ├── test/java/com/trebuchetdynamics/androideyeview/
    │   ├── M0FeasibilityViewModelTest.kt
    │   └── presentation/SensorModeTest.kt
    └── androidTest/java/com/trebuchetdynamics/androideyeview/
        └── M0FeasibilityScreenTest.kt
core-map/
├── build.gradle.kts
└── src/
    ├── main/java/com/trebuchetdynamics/androideyeview/core/map/
    │   ├── GeoPoint.kt                    Provider-neutral coordinate
    │   ├── MapCamera.kt                   Provider-neutral camera
    │   ├── MapEntity.kt                   Provider-neutral synthetic contact/model
    │   ├── MapPolyline.kt                 Provider-neutral trail
    │   ├── MapController.kt               Rendering and camera interface
    │   ├── CameraOwnership.kt             User-first camera arbitration
    │   ├── SyntheticContactFactory.kt      Deterministic 5,000-contact fixture
    │   ├── SyntheticContactAnimator.kt     Deterministic movement ticks
    │   └── RenderBudget.kt                 Adaptive entity/label/model limits
    └── test/java/com/trebuchetdynamics/androideyeview/core/map/
        ├── CameraOwnershipTest.kt
        ├── SyntheticContactFactoryTest.kt
        ├── SyntheticContactAnimatorTest.kt
        └── RenderBudgetTest.kt
maps3d-adapter/
├── build.gradle.kts
└── src/
    ├── main/AndroidManifest.xml
    ├── main/java/com/trebuchetdynamics/androideyeview/maps3d/
    │   ├── Maps3DController.kt             Google SDK adapter
    │   ├── Maps3DHost.kt                   Compose/View lifecycle bridge
    │   ├── Maps3DConversions.kt            Domain-to-SDK mapping
    │   ├── Maps3DEntityRenderer.kt         Marker/model diff renderer
    │   └── Maps3DSession.kt                Ready/error and teardown ownership
    └── test/java/com/trebuchetdynamics/androideyeview/maps3d/
        ├── Maps3DConversionsTest.kt
        └── Maps3DSessionTest.kt
docs/m0/
├── README.md                               Operator instructions and pass criteria
└── FEASIBILITY.md                          Recorded static/runtime evidence and decision
scripts/
├── check-no-secrets.sh                     Local secret/build-output guard
└── verify-m0.sh                            Repeatable static M0 verification
```

---

### Task 1: Reproducible Android foundation and safety rails

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `gradle/libs.versions.toml`
- Create: `gradle/wrapper/gradle-wrapper.properties`
- Create: `gradle/wrapper/gradle-wrapper.jar`
- Create: `gradlew`
- Create: `gradlew.bat`
- Create: `.gitignore`
- Create: `local.defaults.properties`
- Create: `app/build.gradle.kts`
- Create: `app/proguard-rules.pro`
- Create: `core-map/build.gradle.kts`
- Create: `maps3d-adapter/build.gradle.kts`
- Create: `maps3d-adapter/src/main/AndroidManifest.xml`
- Create: `.github/workflows/android.yml`
- Create: `.github/workflows/secret-scan.yml`
- Create: `.github/dependabot.yml`
- Create: `scripts/check-no-secrets.sh`

**Interfaces:**
- Produces Gradle modules `:app`, `:core-map`, and `:maps3d-adapter`.
- Produces version aliases `libs.play.services.maps3d`, `libs.androidx.*`, and plugin aliases consumed by later tasks.
- Produces the manifest-injected `MAPS3D_API_KEY`, defaulting to `DEFAULT_API_KEY` in `local.defaults.properties`.

- [ ] **Step 1: Add the root module graph and locked dependency catalog**

Use `RepositoriesMode.FAIL_ON_PROJECT_REPOS`, `google()`, and `mavenCentral()`. Pin:

```toml
[versions]
compileSdk = "37"
minSdk = "31"
targetSdk = "36"
agp = "9.2.1"
kotlin = "2.3.20"
composeBom = "2026.06.01"
activityCompose = "1.13.0"
coreKtx = "1.19.0"
lifecycle = "2.11.0"
coroutines = "1.10.2"
playServicesBase = "18.10.0"
playServicesMaps3d = "0.2.2"
secretsPlugin = "2.0.1"
junit = "4.13.2"
androidxJunit = "1.3.0"
espresso = "3.7.0"
truth = "1.4.5"
```

- [ ] **Step 2: Add the Gradle 9.4.1 wrapper**

Set:

```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-9.4.1-bin.zip
networkTimeout=10000
validateDistributionUrl=true
```

Copy only standard wrapper launch files and wrapper JAR from the official Google Maps 3D samples revision recorded in `docs/m0/FEASIBILITY.md`.

- [ ] **Step 3: Configure modules**

`core-map` is a Kotlin/JVM module with JUnit and Truth. `maps3d-adapter` is an Android library depending on `core-map`, Compose UI, lifecycle runtime, Play Services Base, and Maps 3D. `app` is an Android application depending on both modules and applying the Maps Platform Secrets plugin.

Use namespace prefix:

```text
com.trebuchetdynamics.androideyeview
```

- [ ] **Step 4: Add key handling and ignore rules**

`local.defaults.properties` contains only:

```properties
MAPS3D_API_KEY=DEFAULT_API_KEY
```

`.gitignore` must exclude:

```gitignore
.gradle/
build/
**/build/
.idea/
*.iml
local.properties
secrets.properties
*.keystore
*.jks
captures/
benchmark-results/
```

- [ ] **Step 5: Add a local secret guard**

`scripts/check-no-secrets.sh` must fail when tracked files contain a Google key-shaped value or common private-key header:

```bash
#!/usr/bin/env bash
set -euo pipefail
if git grep -nE 'AIza[0-9A-Za-z_-]{35}|BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY' -- ':!docs/superpowers/plans/*'; then
  echo 'Potential committed secret detected.' >&2
  exit 1
fi
```

- [ ] **Step 6: Add CI**

`android.yml` runs `./gradlew test lint assembleDebug` with `MAPS3D_API_KEY=DEFAULT_API_KEY`. `secret-scan.yml` runs Gitleaks and `scripts/check-no-secrets.sh`. Dependabot covers Gradle and GitHub Actions weekly.

- [ ] **Step 7: Validate the foundation**

Run:

```bash
./scripts/check-no-secrets.sh
./gradlew projects
./gradlew test lint assembleDebug
```

Expected: secret scan exits 0; all three modules appear; Gradle exits 0 with unit, lint, and debug assembly successful.

- [ ] **Step 8: Commit**

```bash
git add .github .gitignore build.gradle.kts settings.gradle.kts gradle.properties gradle gradlew gradlew.bat local.defaults.properties app core-map maps3d-adapter scripts/check-no-secrets.sh
git commit -m "build: scaffold native Android project"
```

---

### Task 2: Provider-neutral map contracts and deterministic synthetic load

**Files:**
- Create: `core-map/src/main/java/com/trebuchetdynamics/androideyeview/core/map/GeoPoint.kt`
- Create: `core-map/src/main/java/com/trebuchetdynamics/androideyeview/core/map/MapCamera.kt`
- Create: `core-map/src/main/java/com/trebuchetdynamics/androideyeview/core/map/MapEntity.kt`
- Create: `core-map/src/main/java/com/trebuchetdynamics/androideyeview/core/map/MapPolyline.kt`
- Create: `core-map/src/main/java/com/trebuchetdynamics/androideyeview/core/map/MapController.kt`
- Create: `core-map/src/main/java/com/trebuchetdynamics/androideyeview/core/map/CameraOwnership.kt`
- Create: `core-map/src/main/java/com/trebuchetdynamics/androideyeview/core/map/SyntheticContactFactory.kt`
- Create: `core-map/src/main/java/com/trebuchetdynamics/androideyeview/core/map/SyntheticContactAnimator.kt`
- Create: `core-map/src/main/java/com/trebuchetdynamics/androideyeview/core/map/RenderBudget.kt`
- Create tests under `core-map/src/test/java/com/trebuchetdynamics/androideyeview/core/map/`

**Interfaces:**
- Produces `data class GeoPoint(val latitude: Double, val longitude: Double, val altitudeMeters: Double)`.
- Produces `data class MapEntity(val id: String, val position: GeoPoint, val headingDegrees: Double, val label: String?, val kind: EntityKind)`.
- Produces `interface MapController` with `renderEntities`, `removeEntities`, `renderModel`, `renderPolyline`, `setCamera`, `stopCameraMotion`, and `close`.
- Produces `CameraOwnership.acquire(owner)`, `release(owner)`, and `onUserGesture()`.
- Produces exactly 5,000 deterministic contacts from `SyntheticContactFactory.create(count = 5_000, seed = 0xA11CE)`.

- [ ] **Step 1: Write failing validation tests for domain values**

Test latitude `[-90, 90]`, longitude `[-180, 180]`, finite altitude/heading, stable entity IDs, and immutable value semantics.

- [ ] **Step 2: Implement minimal domain values and controller interface**

Use no Android or Google SDK imports in `core-map`.

- [ ] **Step 3: Write the failing 5,000-contact determinism test**

```kotlin
@Test fun createsExactlyFiveThousandStableContacts() {
    val first = SyntheticContactFactory.create(5_000, 0xA11CE)
    val second = SyntheticContactFactory.create(5_000, 0xA11CE)
    assertThat(first).hasSize(5_000)
    assertThat(first.map { it.id }.toSet()).hasSize(5_000)
    assertThat(second).isEqualTo(first)
}
```

- [ ] **Step 4: Implement the synthetic factory**

Distribute contacts deterministically around a bounded North American test region. Generate realistic headings and altitudes without network access.

- [ ] **Step 5: Write and implement movement tests**

`SyntheticContactAnimator.tick(entities, elapsedSeconds)` must be deterministic, keep coordinates valid, preserve IDs, and move at least 99% of contacts for a positive elapsed duration.

- [ ] **Step 6: Write and implement render-budget tests**

Define `QUALITY`, `BALANCED`, and `BATTERY_SAVER` budgets. Every budget accepts 5,000 source entities while independently limiting labels and close-range models; budgets never mutate source truth.

- [ ] **Step 7: Write and implement camera arbitration tests**

Verify direct navigation outranks follow, follow outranks ambient motion, and `onUserGesture()` releases every programmatic owner synchronously.

- [ ] **Step 8: Run tests**

```bash
./gradlew :core-map:test
```

Expected: all `core-map` tests pass.

- [ ] **Step 9: Commit**

```bash
git add core-map
git commit -m "feat: add provider-neutral map contracts"
```

---

### Task 3: Lifecycle-safe Maps 3D adapter

**Files:**
- Create: `maps3d-adapter/src/main/java/com/trebuchetdynamics/androideyeview/maps3d/Maps3DConversions.kt`
- Create: `maps3d-adapter/src/main/java/com/trebuchetdynamics/androideyeview/maps3d/Maps3DSession.kt`
- Create: `maps3d-adapter/src/main/java/com/trebuchetdynamics/androideyeview/maps3d/Maps3DController.kt`
- Create: `maps3d-adapter/src/main/java/com/trebuchetdynamics/androideyeview/maps3d/Maps3DHost.kt`
- Create: `maps3d-adapter/src/test/java/com/trebuchetdynamics/androideyeview/maps3d/Maps3DConversionsTest.kt`
- Create: `maps3d-adapter/src/test/java/com/trebuchetdynamics/androideyeview/maps3d/Maps3DSessionTest.kt`

**Interfaces:**
- Consumes all `core-map` types from Task 2.
- Produces `@Composable fun Maps3DHost(modifier, session, onUserGesture)`.
- Produces `Maps3DSession.state: StateFlow<MapSessionState>` with `Loading`, `Ready(MapController)`, `Failed(message)`, and `Closed`.
- Produces `Maps3DController : MapController` without exposing `GoogleMap3D` to `app`.

- [ ] **Step 1: Write failing conversion tests**

Verify coordinates, camera heading/tilt/range, marker altitude mode, polyline paths, and model orientation convert without axis or unit changes.

- [ ] **Step 2: Implement SDK conversions**

Keep DSL builders and every `com.google.android.gms.maps3d` import in this module.

- [ ] **Step 3: Write failing session-state tests**

Test one-way transitions:

```text
Loading -> Ready -> Closed
Loading -> Failed -> Closed
```

Reject callbacks after close and close the active controller exactly once.

- [ ] **Step 4: Implement the session**

Separate callback/state ownership from the composable so state logic is unit-testable without a map or key.

- [ ] **Step 5: Implement the Compose/View lifecycle bridge**

Create one remembered `Map3DView`, call `onCreate`, `onResume`, `onPause`, `onLowMemory`, `onSaveInstanceState`, and `onDestroy` at the matching Android lifecycle points, and detach the controller on disposal.

Initialize with a bounded camera over a representative airport and `Map3DMode.SATELLITE`. Never draw Compose content over the SDK attribution region.

- [ ] **Step 6: Wire map-ready and error callbacks**

Wait for `OnMap3DViewReadyCallback`; then use the scene-ready callback before declaring the harness ready. Surface sanitized errors to `MapSessionState.Failed`.

- [ ] **Step 7: Run adapter tests and compile**

```bash
./gradlew :maps3d-adapter:testDebugUnitTest :maps3d-adapter:lintDebug
```

Expected: tests and lint pass without requiring a valid API key.

- [ ] **Step 8: Commit**

```bash
git add maps3d-adapter
git commit -m "feat: isolate Maps 3D lifecycle adapter"
```

---

### Task 4: Synthetic contact diff renderer and benchmark harness

**Files:**
- Create: `maps3d-adapter/src/main/java/com/trebuchetdynamics/androideyeview/maps3d/Maps3DEntityRenderer.kt`
- Modify: `maps3d-adapter/src/main/java/com/trebuchetdynamics/androideyeview/maps3d/Maps3DController.kt`
- Create: `maps3d-adapter/src/test/java/com/trebuchetdynamics/androideyeview/maps3d/EntityDiffTest.kt`
- Create: `app/src/main/java/com/trebuchetdynamics/androideyeview/M0FeasibilityViewModel.kt`
- Create: `app/src/test/java/com/trebuchetdynamics/androideyeview/M0FeasibilityViewModelTest.kt`

**Interfaces:**
- Produces pure `EntityDiff.calculate(previous, next): EntityDiff` with `added`, `updated`, `removed`, and `unchangedIds`.
- `Maps3DEntityRenderer.apply(snapshot, budget)` updates only changed native objects.
- `M0FeasibilityViewModel` exposes `StateFlow<M0UiState>` and commands `loadContacts`, `startMotion`, `stopMotion`, and `select`.

- [ ] **Step 1: Write failing diff tests**

Cover first load, position update, label-only update, deletion, unchanged identity, and duplicate-ID rejection.

- [ ] **Step 2: Implement minimal immutable diffing**

Do not rebuild unchanged map objects.

- [ ] **Step 3: Write failing ViewModel tests with a fake `MapController`**

Verify exactly 5,000 contacts load, motion ticks are cancellable, background stop halts ticks, selection does not rebuild unrelated entities, and teardown closes the controller.

- [ ] **Step 4: Implement the ViewModel orchestration**

Use structured coroutines and a monotonic time source. Synthetic movement defaults to one update per second so M0 measures SDK object update cost independently from arbitrary 60 FPS simulation.

- [ ] **Step 5: Implement native marker rendering**

Use stable IDs, absolute altitude, collision behavior, and labels according to `RenderBudget`. Add or mutate objects only through the adapter. If SDK objects do not expose safe mutation, remove/re-add only changed objects and record that limitation in evidence.

- [ ] **Step 6: Add instrumentation counters**

Record load duration, tick duration, add/update/remove counts, current rendered count, and session errors in memory. Export only through an explicit user action to ignored `benchmark-results/`.

- [ ] **Step 7: Run tests**

```bash
./gradlew :core-map:test :maps3d-adapter:testDebugUnitTest :app:testDebugUnitTest
```

Expected: all tests pass and no production provider dependency exists.

- [ ] **Step 8: Commit**

```bash
git add app maps3d-adapter core-map
git commit -m "feat: add synthetic contact feasibility harness"
```

---

### Task 5: Aircraft model, trail, selection, and user-first camera follow

**Files:**
- Modify: `core-map/src/main/java/com/trebuchetdynamics/androideyeview/core/map/MapController.kt`
- Create: `core-map/src/main/java/com/trebuchetdynamics/androideyeview/core/map/TrailBuffer.kt`
- Create: `core-map/src/main/java/com/trebuchetdynamics/androideyeview/core/map/FollowCamera.kt`
- Create tests: `TrailBufferTest.kt`, `FollowCameraTest.kt`
- Modify: `maps3d-adapter/src/main/java/com/trebuchetdynamics/androideyeview/maps3d/Maps3DController.kt`
- Modify: `app/src/main/java/com/trebuchetdynamics/androideyeview/M0FeasibilityViewModel.kt`

**Interfaces:**
- Produces bounded `TrailBuffer(maxPoints = 120)`.
- Produces pure `FollowCamera.forEntity(entity): MapCamera`.
- Adds ViewModel commands `selectAircraft`, `startFollow`, `stopFollow`, and `onUserGesture`.

- [ ] **Step 1: Write failing trail tests**

Verify chronological order, exact maximum length, duplicate-position suppression, immutable snapshots, and full clear.

- [ ] **Step 2: Implement `TrailBuffer`**

Store only app-owned synthetic coordinates.

- [ ] **Step 3: Write failing follow-camera tests**

Verify finite values, heading normalization, fixed bounded range, and a reduced-motion variant without roll.

- [ ] **Step 4: Implement follow-camera policy**

Derive camera center and heading from the selected synthetic contact without Google SDK types.

- [ ] **Step 5: Add the aircraft model**

Use the official Google Maps sample airplane URL only for the feasibility spike and document its source. Render one selected model at absolute altitude with heading-aware orientation and a marker fallback on model failure.

- [ ] **Step 6: Add the bounded trail**

Render a polyline from `TrailBuffer` and replace only when its path changes.

- [ ] **Step 7: Add selection and follow**

Selection updates the harness detail panel. Follow acquires `CameraOwner.FOLLOW`; each movement tick applies `FollowCamera`. Any map gesture calls `onUserGesture()`, synchronously stops camera animation, and releases follow.

- [ ] **Step 8: Run unit and instrumentation compilation**

```bash
./gradlew test lint assembleDebug assembleDebugAndroidTest
```

Expected: Gradle exits 0.

- [ ] **Step 9: Commit**

```bash
git add app core-map maps3d-adapter
git commit -m "feat: add aircraft tracking feasibility spike"
```

---

### Task 6: Native presentation approximations and accessible M0 screen

**Files:**
- Create: `app/src/main/java/com/trebuchetdynamics/androideyeview/presentation/SensorMode.kt`
- Create: `app/src/main/java/com/trebuchetdynamics/androideyeview/presentation/SensorOverlay.kt`
- Create: `app/src/test/java/com/trebuchetdynamics/androideyeview/presentation/SensorModeTest.kt`
- Create: `app/src/main/java/com/trebuchetdynamics/androideyeview/M0FeasibilityScreen.kt`
- Create: `app/src/main/java/com/trebuchetdynamics/androideyeview/MainActivity.kt`
- Create: `app/src/main/java/com/trebuchetdynamics/androideyeview/ui/theme/Theme.kt`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/androidTest/java/com/trebuchetdynamics/androideyeview/M0FeasibilityScreenTest.kt`

**Interfaces:**
- Produces enum `SensorMode.NORMAL`, `CRT`, `NVG`, `MONOCHROME`, `SNOW`, and `THERMAL_INSPIRED` with honest labels.
- Produces `@Composable SensorOverlay(mode, modifier)` with `pointerInput` absent so map gestures pass through.
- Produces `@Composable M0FeasibilityScreen(state, actions)`.

- [ ] **Step 1: Write failing sensor vocabulary tests**

Assert only `NORMAL` lacks a simulation disclaimer and `THERMAL_INSPIRED` never uses “FLIR”, “temperature”, or “heat measurement” as a factual claim.

- [ ] **Step 2: Implement presentation modes**

Use transparent Compose overlays with tint, scanlines/noise, and contrast cues. Do not capture or transform Google map pixels. Keep the SDK attribution area clear and interactive.

- [ ] **Step 3: Build the M0 screen**

Show map/session status, 5,000-contact load, motion, selection, follow, trail, model, sensor mode, counters, and an explicit “Export M0 results” action. Include persistent copy: “Visual simulation — not sensor imagery.”

- [ ] **Step 4: Add lifecycle forwarding and reduced motion**

Use the adapter host and `LocalLifecycleOwner`. Stop motion when not resumed. Read the system animator-duration/reduced-motion preference and disable shaped follow behavior when requested.

- [ ] **Step 5: Add UI tests**

Without initializing the real map, render loading, ready-with-fake-controller, and failed states. Verify controls have content descriptions, simulation copy is visible, and the error state leaves retry/setup guidance accessible.

- [ ] **Step 6: Validate**

```bash
./gradlew test lint assembleDebug assembleDebugAndroidTest
```

Expected: all checks pass.

- [ ] **Step 7: Commit**

```bash
git add app
git commit -m "feat: add M0 native feasibility console"
```

---

### Task 7: Operator runbook, evidence capture, and go/no-go decision

**Files:**
- Create: `docs/m0/README.md`
- Create: `docs/m0/FEASIBILITY.md`
- Create: `scripts/verify-m0.sh`
- Modify: `README.md`
- Modify: `ROADMAP.md`

**Interfaces:**
- Produces one command, `./scripts/verify-m0.sh`, for all static checks.
- Produces a runtime checklist with device model, Android version, Play Services version, app revision, SDK version, and Maps 3D key restriction confirmation.
- Produces an explicit `GO`, `CONDITIONAL GO`, or `NO-GO` decision.

- [ ] **Step 1: Add the static verification script**

Run:

```bash
./scripts/check-no-secrets.sh
./gradlew --version
./gradlew test lint assembleDebug assembleDebugAndroidTest
```

Also fail if production network libraries or provider endpoint strings appear outside the approved Maps SDK dependency and documentation.

- [ ] **Step 2: Document operator setup**

Require a supported physical Android 12+ flagship, current Google Play Services, a billing-enabled Maps 3D API key restricted to application ID plus signing SHA-1, and `secrets.properties` excluded by Git.

- [ ] **Step 3: Define the runtime matrix**

Run cold launch, background/resume, process recreation, 5,000-contact load, ten movement ticks, select, model, trail, follow, gesture interruption, each presentation mode, low-memory callback, and teardown.

- [ ] **Step 4: Capture quantitative evidence**

Record interactive launch time, 5,000-contact load duration, median/worst tick duration, visible count, label count, frame-rate observation, memory high-water mark, thermal status, crashes, and map/session errors. Commit only summarized measurements with no key, device identifier, location history, raw log, screenshot-derived map content, or prohibited Google content.

- [ ] **Step 5: Apply the decision rule**

- `GO`: every M0 exit gate passes on reference hardware.
- `CONDITIONAL GO`: core map/model/trail/follow pass, but a documented nonessential presentation approximation needs later work.
- `NO-GO`: lifecycle, attribution, 5,000-contact usability, model/trail, camera cancellation, terms, or key restriction has no safe native solution.

- [ ] **Step 6: Update public status**

Mark M0 complete only for `GO` or an explicitly bounded `CONDITIONAL GO`. Keep later milestones blocked for `NO-GO`.

- [ ] **Step 7: Validate and commit**

```bash
./scripts/verify-m0.sh
git diff --check
git add README.md ROADMAP.md docs/m0 scripts/verify-m0.sh
git commit -m "docs: record M0 native feasibility evidence"
```

---

### Task 8: Post-M0 GitHub issue map

**Files:**
- Modify: `ROADMAP.md` only to add issue links after creation.

**Interfaces:**
- Consumes a `GO` or bounded `CONDITIONAL GO` from Task 7.
- Produces independently assignable GitHub issues for M1–M4; no issue may mix milestones.

- [ ] **Step 1: Stop if M0 is not proven**

Do not create later-milestone issues while `docs/m0/FEASIBILITY.md` is `PENDING` or `NO-GO`.

- [ ] **Step 2: Create one milestone-level tracking issue per M1–M4**

Each tracker links the PRD and roadmap, repeats its exit gate, and contains no implementation detail owned by child issues.

- [ ] **Step 3: Create independently assignable M1 issues**

Create bounded issues for application shell, data-state contract, layer registry, each first live layer, tracking/contacts, HUD/density, App Links, and first missions.

- [ ] **Step 4: Create M2–M4 capability issues**

Create one issue per provider layer or coherent platform capability. Include source/licensing/security prerequisites and acceptance checks from the roadmap.

- [ ] **Step 5: Link issues from the roadmap**

Add issue URLs without changing milestone scope or status.

- [ ] **Step 6: Verify issue coverage**

Every unchecked M1–M4 roadmap line must map to exactly one issue; no issue may authorize named-person tracking, production secrets, or pre-M0 provider work.

- [ ] **Step 7: Commit and push roadmap links**

```bash
git add ROADMAP.md
git commit -m "docs: link post-M0 delivery issues"
git push
```

---

## Plan self-review checklist

- [x] M0 repository, lifecycle, density, model, trail, selection, camera, presentation, attribution, credential, CI, and evidence gates are covered.
- [x] Production providers are explicitly excluded until M0 passes.
- [x] Google SDK dependencies are isolated from provider-neutral domain contracts.
- [x] Hardware-only claims require a physical-device receipt and cannot be satisfied by unit tests alone.
- [x] Later GitHub issues are gated on a recorded M0 `GO` or bounded `CONDITIONAL GO`.
- [x] File paths, interfaces, commands, and expected outcomes are explicit.
- [x] No incomplete task or unresolved architecture choice remains in the plan.
