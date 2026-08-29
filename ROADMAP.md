# Android Eye View Roadmap

Android Eye View is the native Android port of [God’s Eye View](https://github.com/bilawalsidhu/gods-eye-view). This roadmap translates the approved [product requirements](PRD.md) into independently testable delivery milestones.

The roadmap is ordered by technical risk and dependency, not by calendar date. No milestone is a promise of a release date. Scope moves forward only when the previous milestone’s exit gate is demonstrated on reference hardware.

## Status legend

- ✅ Complete
- 🚧 Active
- ⬜ Planned
- ⚠️ Blocked or awaiting a documented decision

## Current status

### Project definition

- ✅ Public repository created.
- ✅ Product mission and parity target documented in [PRD.md](PRD.md).
- ✅ Native Kotlin direction selected.
- ✅ Direct-first BYOK and optional-gateway boundaries selected.
- ✅ Initial [README.md](README.md) and roadmap documented.
- ✅ M0 Android scaffold, map adapter, synthetic harness, UI tests, and keyless-device safety checks.
- 🚧 Key-backed native rendering, attribution, lifecycle, density, model, trail, follow, and thermal evidence.

## Release strategy

Development is divided into five milestones:

| Milestone | Outcome | Release posture |
|---|---|---|
| M0 | Native feasibility proven | Internal prototypes |
| M1 | Living globe foundation | Developer preview |
| M2 | Broad signal fusion | Private/opt-in alpha |
| M3 | Immersive non-voice parity | Public alpha candidate |
| M4 | Voice, hardening, and release readiness | Public alpha |

## M0 — Feasibility and repository foundation 🚧

**Goal:** Prove that the native stack can sustain the defining God’s Eye View workflows before committing to full feature construction.

### Repository foundation

- ✅ Create the Gradle version catalog and multi-module Android project.
- ✅ Set Android 12 / API 31 as the minimum supported version.
- ✅ Add Kotlin, Jetpack Compose, coroutines, and the selected test stack.
- ✅ Add debug-only local configuration for a developer-provided Maps 3D key.
- ✅ Ensure no credential is packaged into version control or release artifacts.
- ✅ Add CI for static analysis, unit tests, and debug assembly.
- ✅ Add dependency update and secret-scanning automation.
- ✅ Document reproducible setup and key restriction steps.

### Map adapter spike

- ✅ Host the lifecycle-managed Maps 3D view inside Compose.
- ✅ Isolate all Google SDK types in a `maps3d-adapter` module.
- ✅ Define provider-neutral camera, entity, polyline, and model-facing interfaces required by the M0 harness.
- ⬜ Prove process recreation, background/foreground transitions, and configuration changes on a key-backed map.
- ⬜ Verify required Google attribution remains visible in every prototype mode.

### Live-entity rendering spike

- ✅ Generate exactly 5,000 deterministic synthetic moving contacts.
- ⬜ Measure native marker creation, updates, removal, selection, and label density.
- ⬜ Demonstrate one moving glTF aircraft with heading and scale on a key-backed map.
- ✅ Implement a bounded 120-point trail and native polyline adapter.
- ✅ Implement a cancellable, user-first programmatic camera owner.
- ⬜ Confirm touch cancellation against a key-backed native map.

### Presentation spike

- ✅ Implement CRT, NVG, monochrome, snow/noise, and thermal-inspired native treatments.
- ⬜ Prototype a native entity-highlight treatment when screen-space detection boxes are unavailable.
- ✅ Label all presentation treatments as visual simulations rather than sensor imagery.
- ⬜ Confirm every presentation mode remains readable with required attribution on a live map.

### Performance and policy gates

- ✅ Select and document the initial SM-S928B / Android 16 reference device.
- ✅ Document repeatable frame-time, memory, battery, and thermal measurements.
- ✅ Document Google Maps Platform setup and Android key restrictions for the sideload model.
- ✅ Confirm the experimental SDK exposes the camera, marker, model, polyline, and lifecycle APIs needed by M1.
- ⬜ Execute the measurements against an authenticated native map.

### M0 exit gate

M0 is complete only when:

- the native map survives lifecycle and process-recreation tests;
- 5,000 synthetic contacts remain usable under adaptive density rules;
- selection, trail rendering, glTF movement, and camera follow are demonstrated;
- no unresolved blocker remains for required attribution or Android key restriction;
- unsupported upstream effects have documented functional alternatives;
- results and known SDK risks are committed to the repository.

## M1 — Living globe foundation ⬜

**Goal:** Deliver the first end-to-end God’s Eye View loop on Android: open the globe, enable a public signal, select and track it, inspect its source, and return home.

### Application shell

- ⬜ Add Compose navigation and adaptive phone/tablet layouts.
- ⬜ Add first-run responsible-use acknowledgment.
- ⬜ Add settings, provider status, attribution, licenses, and diagnostics screens.
- ⬜ Add DataStore preferences and Keystore-backed secret storage.
- ⬜ Stop rendering, high-frequency polling, audio, and microphone work when backgrounded.

### Core domain and layer system

- ⬜ Define provider-neutral entities, coordinates, timestamps, and coverage metadata.
- ⬜ Implement the shared `LIVE`, `DELAYED`, `STALE`, `PARTIAL`, `SIMULATED`, `ESTIMATED`, `RECONSTRUCTED`, `LOADING`, `UNAVAILABLE`, and `DISABLED` state contract.
- ⬜ Define the independent layer lifecycle and registry.
- ⬜ Add viewport throttling, request coalescing, backoff, cache bounds, and cancellation.
- ⬜ Ensure a disabled or failed layer cannot affect independent layers.

### First live layers

- ⬜ Civilian flights with source and freshness reporting.
- ⬜ Military-tagged ADS-B contacts with coverage caveats.
- ⬜ Satellites with SGP4 propagation and orbit paths.
- ⬜ Earthquakes with magnitude, age, and source metadata.

### Core interaction

- ⬜ Place and coordinate search.
- ⬜ Reset-globe action.
- ⬜ Tap selection and detail presentation.
- ⬜ Tracking, interpolation, dead reckoning, and bounded trails.
- ⬜ Nearby contacts roster with source-aware counts.
- ⬜ Tactical HUD and adaptive label density.
- ⬜ Versioned Android App Links for camera, layers, presentation, and one tracked public entity.

### First missions

- ⬜ Live Contacts.
- ⬜ Space Watch.
- ⬜ Environmental, with earthquakes remaining useful when fire credentials are absent.
- ⬜ Explore Manually.

### M1 exit gate

M1 is complete only when a fresh installation can:

1. configure an Android-restricted map key;
2. open an interactive globe;
3. launch a mission;
4. select and track a live or propagated entity;
5. inspect source, freshness, and coverage;
6. recover cleanly from one provider failure;
7. return to the full globe;
8. restore valid state after ordinary process recreation.

The reference device must meet the PRD’s cold-start, selection, frame-rate, backgrounding, and memory requirements.

## M2 — Broad signal fusion ⬜

**Goal:** Expand from a capable globe into the broad public-signal canvas that defines God’s Eye View.

### Additional live layers

- ⬜ AIS vessels with reconnect, warm-cache, and first-answer semantics.
- ⬜ Active fires with credential and freshness status.
- ⬜ Live traffic with a persistent `SIMULATED` fallback when real traffic is unavailable.
- ⬜ Public CCTV catalogs with user-initiated frame access and estimated-pose labels.
- ⬜ Public radio directory with user-initiated playback and IP-disclosure copy.
- ⬜ Bikeshare station availability.
- ⬜ Recent launch metadata and reconstructed ascent inputs.
- ⬜ Viewport-bounded mapped installations labeled as incomplete community context.

### Infrastructure modules

- ⬜ Datacenters.
- ⬜ Dams.
- ⬜ Submarine cables behind an independently removable license boundary.
- ⬜ Build-time checks preventing non-commercial datasets from entering incompatible distributions.
- ⬜ Global level-of-detail and decluttering rules that avoid enabling every object at full density.

### Cross-layer workflows

- ⬜ Move from a selected vessel, fire, or traffic area to the nearest supported public camera.
- ⬜ Keep selected-entity context coherent when switching layers.
- ⬜ Use the same normalized snapshots for rendered entities, counts, and details.
- ⬜ Add Port Watch and performance-safe Global Infrastructure missions.

### Quota and attribution controls

- ⬜ Provider-specific refresh intervals and daily budgets where relevant.
- ⬜ Visible last-successful-update and quota state when known.
- ⬜ Complete per-layer source, license, commercial-use, cache, and attribution metadata.
- ⬜ Attribution remains visible during tracking, clean, and capture-oriented modes.

### M2 exit gate

M2 is complete only when every target data family can be enabled independently, partial configuration is usable, provider failures do not cascade, cross-layer handoffs are source-correct, and dense global layers remain within the reference-device performance budget.

## M3 — Immersive operation ⬜

**Goal:** Deliver the major non-voice interaction and presentation workflows of the upstream product with native Android behavior.

### Cockpit and contacts

- ⬜ Heading-aware cockpit-style aircraft follow.
- ⬜ Nearby contacts navigation with next/previous filters.
- ⬜ Local weather and locality briefing from documented sources.
- ⬜ Immediate, predictable exit and camera release.
- ⬜ Reduced-motion path with no banking or nonessential camera shaping.

### Models and presentation

- ⬜ Class-appropriate aircraft models at close range.
- ⬜ Distance- and device-budgeted model-to-marker fallback.
- ⬜ Tactical HUD layouts for portrait, landscape, and tablet.
- ⬜ Native CRT, NVG, monochrome, snow/noise, and thermal-inspired modes.
- ⬜ Native detection/highlight approximation with density controls.
- ⬜ Persistent wording that presentation filters are not real sensor imagery.

### Annotations and routes

- ⬜ Points, labels, lines, arrows, polygons, and distance measurement.
- ⬜ Source-backed supported boundary resolution.
- ⬜ Walking routes through an approved configured provider.
- ⬜ Cancellable route fly-through with shaped motion.
- ⬜ Import/export of app-owned annotation JSON without secrets.

### Scenes and launches

- ⬜ Versioned scene timeline for camera, layer, and presentation actions.
- ⬜ Pause, resume, seek, and exit controls.
- ⬜ Reconstructed launch ascent playback.
- ⬜ Persistent `RECONSTRUCTED ESTIMATE` labeling.
- ⬜ Clean presentation that never hides required attribution.

### M3 exit gate

M3 is complete only when the principal non-voice upstream workflows have either functional parity or a documented, tested native approximation. Accessibility, camera ownership, reduced motion, and attribution requirements must pass across all immersive modes.

## M4 — Voice, gateway, and public-alpha hardening ⬜

**Goal:** Add protected server-assisted capabilities and prove the complete Android port is safe and sustainable enough for public alpha use.

### Optional self-hosted gateway

- ⬜ Define a versioned, fixed-purpose gateway protocol.
- ⬜ Publish a reference self-hosted deployment.
- ⬜ Add health, version, and capability negotiation.
- ⬜ Keep OAuth client secrets and long-lived model-provider keys server-side.
- ⬜ Add request bounds, host allowlists, sanitized failures, throttles, and cache policy.
- ⬜ Add negative tests for SSRF, redirects, oversized payloads, and open-relay behavior.

### Voice operation

- ⬜ Mint short-lived realtime voice credentials through the configured gateway.
- ⬜ Keep microphone and connection state visible.
- ⬜ Implement a fixed, schema-defined tool surface.
- ⬜ Confirm only successful actions.
- ⬜ Treat feed-provided text as untrusted data.
- ⬜ Add source-bounded entity and current-view questions.
- ⬜ Add provider-usage warnings and a configurable session cap when metering permits.
- ⬜ Ensure all core app features remain usable without voice or a gateway.

### Release hardening

- ⬜ Run accessibility review against primary workflows.
- ⬜ Run dense-scene benchmarks and a two-hour mixed-use soak test.
- ⬜ Run static secret scanning against source, resources, diagnostics, and APKs.
- ⬜ Verify deep links and exports cannot carry credentials or unintended precise device location.
- ⬜ Complete source, licensing, privacy, responsible-use, setup, and contributor documentation.
- ⬜ Publish known native parity differences and experimental SDK risks.
- ⬜ Produce a reproducible, credential-free signed alpha artifact.

### M4 exit gate

M4 is complete only when all [PRD release criteria](PRD.md#19-release-criteria) pass, no known critical credential or microphone privacy issue remains, every uncertain data state is labeled, and the core experience works without the optional gateway.

## Post-alpha direction

Post-alpha work is evidence-driven and does not block the initial port:

- close documented native parity gaps as the Maps 3D SDK evolves;
- widen device support after performance measurements justify it;
- evaluate Google Play distribution separately from sideloading;
- add providers only when license, security, quota, and provenance requirements are complete;
- consider historical replay only as a separately scoped product effort;
- consider other platforms only after the Android architecture and source contracts stabilize.

## Cross-cutting definition of done

Every implementation slice must include:

- tests for success, empty, malformed, delayed, partial, and unavailable states where applicable;
- source, freshness, coverage, license, attribution, retention, and commercial-use metadata;
- bounded polling, cache, entity, label, trail, and memory behavior;
- cancellation and Android lifecycle handling;
- accessibility semantics and reduced-motion behavior for user-facing interactions;
- secret and diagnostic redaction checks;
- no named-person tracking or prohibited private data;
- updated documentation and roadmap status;
- a focused commit that leaves the branch passing its normal validation suite.

## Near-term implementation order

The first engineering slices should be completed in this order:

1. Gradle multi-module scaffold and CI.
2. Provider-neutral `core-map` interfaces.
3. Lifecycle-safe `maps3d-adapter` proof.
4. Synthetic moving-contact benchmark.
5. Model, trail, selection, and camera-follow proof.
6. Native presentation-filter experiments.
7. M0 evidence report and go/no-go decision for M1.

No production data provider should be integrated before the native map, entity-density, camera, attribution, and lifecycle feasibility gates are understood.
