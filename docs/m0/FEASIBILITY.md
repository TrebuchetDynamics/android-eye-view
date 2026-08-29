# M0 Native Feasibility Evidence

- **Decision:** PENDING RUNTIME VALIDATION
- **Recorded:** 2026-08-29
- **Plan:** [M0 Native Feasibility Implementation Plan](../superpowers/plans/2026-08-29-m0-native-feasibility.md)

## Scope

M0 validates the native foundation only. It intentionally integrates no production aircraft, vessel, satellite, earthquake, fire, traffic, camera, radio, bikeshare, launch, or infrastructure provider.

## Research baseline

Research was pinned to the official `googlemaps-samples/android-maps3d-samples` repository at commit [`365c853d1bfa85a1a0d2b5d6524f4f60762d7417`](https://github.com/googlemaps-samples/android-maps3d-samples/commit/365c853d1bfa85a1a0d2b5d6524f4f60762d7417), dated 2026-08-28.

Selected baseline:

| Component | Version |
|---|---:|
| Gradle | 9.4.1 |
| Android Gradle Plugin | 9.2.1 |
| Kotlin | 2.3.20 |
| Host JDK | 21 |
| Android bytecode target | 11 |
| Compile SDK | 37 |
| Target SDK | 36 |
| Minimum SDK | 31 |
| Compose BOM | 2026.06.01 |
| Maps 3D SDK | 0.2.2 |

Primary references:

- [Maps 3D SDK overview](https://developers.google.com/maps/documentation/maps-3d/android-sdk/overview)
- [Maps 3D SDK setup](https://developers.google.com/maps/documentation/maps-3d/android-sdk/setup)
- [Add a 3D map](https://developers.google.com/maps/documentation/maps-3d/android-sdk/add-a-3d-map)
- [Official Android samples](https://github.com/googlemaps-samples/android-maps3d-samples)
- [Maps 3D release notes](https://developers.google.com/maps/documentation/maps-3d/android-sdk/release-notes)

## Confirmed API surface

Static inspection of `com.google.android.gms:play-services-maps3d:0.2.2` confirms:

- `Map3DView` exposes `onCreate`, `onStart`, `onResume`, `onPause`, `onStop`, `onDestroy`, `onLowMemory`, `onSaveInstanceState`, and asynchronous readiness.
- `GoogleMap3D` exposes markers, models, polylines, polygons, immediate camera updates, finite camera animations, map-ready/steady listeners, and map clicks.
- `Marker` exposes mutable position, label, altitude mode, collision behavior, and removal.
- `Model` exposes mutable position, orientation, scale, URL, altitude mode, and removal.
- `Polyline` exposes mutable path, stroke, altitude mode, occlusion, and removal.

## Known SDK risks

1. Maps 3D SDK 0.2.2 is experimental/pre-GA and may make incompatible changes.
2. Only one active `Map3DView` is supported.
3. SDK map state is shared across view instances and may persist after destruction; application objects require explicit cleanup.
4. The official repository’s Compose wrapper is a WIP reference, so this project uses a direct lifecycle-aware `Map3DView` adapter.
5. No official 5,000-marker limit, benchmark, or 3D clustering utility is documented.
6. Photorealistic coverage varies by region.
7. Remote GLB loading is demonstrated; local-resource loading, skeletal animation controls, and a model-load completion callback are not documented.
8. Google SDK callbacks are not assumed to run on the Android main thread.

## Static receipts

| Check | Result | Evidence |
|---|---|---|
| Repository module graph | PASS | `:app`, `:core-map`, `:maps3d-adapter` resolved with Gradle 9.4.1 |
| Android compile baseline | PASS | `compileSdk 37`, `targetSdk 36`, `minSdk 31` configuration resolves |
| Maps 3D dependency | PASS | Google Maven artifact `play-services-maps3d:0.2.2` resolves |
| Core domain tests | PASS | 24 JVM tests covering values, 5,000-contact determinism, animation, budgets, camera ownership, trail, and follow policy |
| Adapter tests | PASS | 9 JVM tests covering conversions, session state, and entity diffing |
| App tests | PASS | 7 JVM tests covering harness orchestration and presentation vocabulary |
| Unit/lint/debug build | PASS | `./gradlew test lint assembleDebug assembleDebugAndroidTest`, 2026-08-29 |
| Physical-device UI tests | PASS | 3 Compose tests on SM-S928B / Android 16, 2026-08-29 |
| Keyless launch safety | PASS | Cold launch remained alive and showed actionable `MAP KEY REQUIRED` / `UNAVAILABLE` guidance |
| Keyless synthetic harness | PASS | Generated 5,000 contacts, allocated 600 labels, and updated visible harness state; 41.82 ms observed on SM-S928B |
| Committed secret-pattern scan | PASS | `scripts/check-no-secrets.sh`, 2026-08-29 |
| Production providers integrated | PASS | None in M0 scaffold |

These receipts prove build, architecture, keyless safety, synthetic generation, and non-map UI behavior. They do not prove native map rendering performance.

## Reference device

- Model family: Samsung SM-S928B
- Android: 16 / API 36
- Google Play Services: 26.33.31 (260400-972005113)
- Initial thermal status: 0
- Harness revision: `8200b64`
- Maps 3D key: not configured; no key value was read or recorded

## Runtime receipts

| Gate | Status | Measurement |
|---|---|---|
| Keyless startup and setup guidance | PASS | Cold launch 460 ms; app stayed alive; empty crash buffer after guarded startup |
| Interactive native globe | NOT RUN | Requires restricted Maps 3D key |
| Attribution unobscured | NOT RUN | Requires restricted Maps 3D key |
| Background/resume and recreation | NOT RUN | Requires native map session |
| 5,000-contact load | PARTIAL | Generator/UI: 5,000 entities and 600 labels in 41.82 ms; native markers require restricted key |
| Ten movement ticks | NOT RUN | Native marker updates require restricted key |
| Selection latency | NOT RUN | Native marker selection requires restricted key |
| Aircraft model or fallback | NOT RUN | Requires restricted key and map scene |
| Bounded trail | NOT RUN | Requires restricted key and map scene |
| Follow and gesture cancellation | NOT RUN | Requires restricted key and map scene |
| Presentation approximations | PARTIAL | Vocabulary and Compose UI tests pass; map-overlay visual inspection requires restricted key |
| Memory/thermal soak | NOT RUN | Requires full native map run |

## Decision

**PENDING RUNTIME VALIDATION.**

The native API surface supports implementing the M0 harness, but static compilation cannot establish the 5,000-contact performance, map lifecycle, attribution visibility, remote model rendering, or touch-cancellation gates. Follow [the operator runbook](README.md) before changing this decision.
