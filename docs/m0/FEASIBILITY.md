# M0 Free-Globe Feasibility Evidence

- **Decision:** PENDING REMAINING RUNTIME GATES
- **Recorded:** 2026-08-29
- **Current plan:** [M0 Free Globe Pivot Plan](../superpowers/plans/2026-08-29-m0-free-globe-pivot.md)
- **Architecture decision:** [ADR-0001 — Free Keyless Globe](../architecture/ADR-0001-free-keyless-globe.md)

## Scope

M0 validates the Android globe foundation only. It intentionally integrates no production aircraft, vessel, satellite, earthquake, fire, traffic, camera, radio, bikeshare, launch, or infrastructure provider.

The default app must require no map key, token, billing account, registration, or paid map service.

## Renderer decision

The original Google Maps 3D SDK path is **NO-GO under the free constraint**. Its map configuration requires an authenticated Google Maps Platform project and billing-backed key, so it cannot be the mandatory renderer.

M0 now uses:

| Component | Version / choice |
|---|---|
| Gradle | 9.4.1 |
| Android Gradle Plugin | 9.2.1 |
| Kotlin | 2.3.20 |
| Host JDK | 21 |
| Compile / target / minimum SDK | 37 / 36 / 31 |
| Compose BOM | 2026.06.01 |
| Map renderer | MapLibre GL JS 6.6.0, locally bundled |
| Basemap | OpenFreeMap Liberty, keyless |
| Android bridge | AndroidX WebKit 1.17.0 |

MapLibre GL JS is bundled into the APK and hosted from `appassets.androidplatform.net` by `WebViewAssetLoader`. A restrictive content-security policy permits local executable assets and network map resources only from `https://tiles.openfreemap.org`. Kotlin commands are serialized as JSON and sent to one renderer entry point; the JavaScript interface exposes only bounded ready, error, gesture, selection, and metric callbacks.

OpenFreeMap provides no service-level guarantee. The style endpoint is therefore a replaceable boundary and a compatible self-hosted deployment remains the escape hatch.

## Static receipts

| Check | Result | Evidence |
|---|---|---|
| Repository module graph | PASS | `:app`, `:core-map`, and `:web-map-adapter` resolve |
| Billed map runtime removed | PASS | No Maps 3D dependency, API-key metadata, secret property, or production adapter remains |
| Web command/store tests | PASS | 11 Vitest tests cover validation, exact 5,000-contact storage, removal, selected marker, and trail |
| Core domain tests | PASS | 24 JVM tests cover values, deterministic generation/animation, budgets, camera ownership, trails, and follow policy |
| Web adapter tests | PASS | 12 JVM tests cover JSON command encoding, controller closure, duplicate readiness, runtime failure, session state, and callback validation |
| App tests | PASS | 10 JVM tests cover harness orchestration, current-scene replay, offline-start retry, renderer metrics, and presentation vocabulary |
| Physical-device UI tests | PASS | 3 Compose tests on the reference device |
| Unit/lint/debug build | PASS | Web tests/typecheck/build plus Android tests, lint, APK, and test-APK assembly |
| Secret scan | PASS | `scripts/check-no-secrets.sh` |
| Production providers integrated | PASS | None in M0 |

## Reference device

- Model family: Samsung SM-S928B
- Android: 16 / API 36
- Map credential: none configured or required
- Renderer: MapLibre GL JS 6.6.0 through Android System WebView

The physical device serial, screenshots, and raw logs are not committed.

## Runtime receipts

| Gate | Status | Measurement / observation |
|---|---|---|
| Free/keyless cold launch | PASS | Cold activity launch completed in 595 ms; process remained alive; empty crash buffer |
| Interactive globe | PASS | True spherical globe rendered with the OpenFreeMap Liberty style |
| Attribution unobscured | PASS | OpenFreeMap, OpenMapTiles, and OpenStreetMap attribution visible at the bottom of the globe |
| Renderer readiness honesty | PASS | Startup reports `LOADING`, then `READY`; an initial zero-height viewport defect produced a blank canvas and was fixed by sizing the document from `window.innerHeight` before renderer construction |
| 5,000-contact load | PASS | Exactly 5,000 deterministic contacts and a 600-label candidate budget; visible labels remain zoom- and collision-dependent; observed Kotlin load 181.27 ms |
| Ten movement ticks | PASS | Tick count reached 10; final observed Kotlin tick 76.29 ms; aggregate median/worst still to be captured |
| Renderer completion timing | RE-RUN REQUIRED | Earlier 17.40/11.90 ms samples measured two animation frames, not source processing completion. Instrumentation now waits for the contacts `sourcedata` completion event and a following MapLibre `render`; new values are not yet recorded. |
| Animation frame sample | PASS | Rolling requestAnimationFrame p95 reported 8.40 ms during the observed run |
| Aircraft representation | PASS | Renderer and UI use an honestly labeled aircraft marker fallback, not a glTF model |
| Selection and bounded trail | NOT RUN | Automated contracts pass; physical interaction evidence remains |
| Follow and gesture cancellation | NOT RUN | Core ownership/follow tests pass; physical interaction evidence remains |
| 3D building extrusion | NOT RUN | Liberty style includes a `building-3d` extrusion layer; physical city-level inspection remains |
| Background/resume and recreation | NOT RUN | Physical lifecycle matrix remains |
| Offline failure and recovery | NOT RUN | Failure state and explicit globe retry are implemented; physical network recovery matrix remains |
| Renderer-process loss | NOT RUN | Ready-to-failed state transition, controller closure, retained error state, and explicit retry are covered by automated contracts; physical validation remains |
| Presentation modes | PARTIAL | Vocabulary and Compose tests pass; physical readability/attribution inspection remains |
| Memory/thermal soak | NOT RUN | Ten-minute dense-scene run remains |

The viewport defect discovered during physical validation is now a regression concern: Android WebView initially exposed a valid `window.innerHeight` while percentage and viewport-unit root heights computed to zero. `web-map/src/main.ts` explicitly sizes `html`, `body`, and `#map` before constructing MapLibre.

## Decision

**PENDING REMAINING RUNTIME GATES.**

The free renderer now proves two high-risk properties: a true keyless globe renders on the reference device, and a single batched GeoJSON path visibly accepts 5,000 moving contacts. The corrected source-completion timing must be re-recorded before the performance gate is decided. M0 cannot receive GO yet because selection/trail/follow gesture cancellation, lifecycle and renderer-process recovery, city-level building extrusion, presentation readability, and the memory/thermal soak remain unverified.

Do not integrate production providers or create M1–M4 implementation issues until those gates produce GO or a bounded CONDITIONAL GO.
