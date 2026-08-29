# M0 Native Feasibility Runbook

M0 answers one question before Android Eye View integrates production data providers:

> Can the experimental native Maps 3D SDK sustain the lifecycle, density, model, trail, selection, camera-follow, attribution, and presentation foundations required by the God’s Eye View Android port?

## Static validation

From the repository root:

```bash
./scripts/verify-m0.sh
```

The command checks committed-secret patterns, unit tests, Android lint, the debug APK, and the instrumentation-test APK. Static validation does not prove rendering performance or Maps 3D service behavior.

## Runtime prerequisites

Runtime validation requires all of the following:

- a physical Android 12+ flagship-class device with at least 8 GB RAM;
- current Google Play Services;
- USB debugging or an equivalent trusted development connection;
- a billing-enabled Google Cloud project with Maps 3D SDK for Android enabled;
- an API key restricted to `com.trebuchetdynamics.androideyeview`, the signing certificate SHA-1, and Maps 3D SDK for Android;
- a root `secrets.properties` file that is ignored by Git.

Create the local secret file:

```properties
MAPS3D_API_KEY=YOUR_ANDROID_RESTRICTED_KEY
```

Never paste the key into source, Gradle files, issue comments, screenshots, logs, benchmark exports, or this documentation.

## Build and install

```bash
./gradlew :app:installDebug
adb shell am start -n \
  com.trebuchetdynamics.androideyeview/.MainActivity
```

## Runtime matrix

Record only summarized results in [FEASIBILITY.md](FEASIBILITY.md).

1. Cold-launch the application and wait for map readiness.
2. Confirm Google attribution is visible and unobscured.
3. Background and resume the app five times.
4. Enable “Load 5,000 contacts.”
5. Orbit and zoom the map while contacts are present.
6. Run ten one-second movement ticks.
7. Select the synthetic aircraft.
8. Confirm the glTF model or documented marker fallback appears.
9. Start the bounded trail.
10. Start camera follow.
11. Touch and drag the map; confirm follow stops immediately.
12. Cycle Normal, CRT, NVG, Monochrome, Snow, and Thermal-inspired presentation modes.
13. Confirm “Visual simulation — not sensor imagery” remains visible for simulated modes.
14. Trigger an Android low-memory callback through development tooling.
15. Recreate the activity and verify one active map view, valid camera state, and no duplicated map objects.
16. Force-stop the app and confirm rendering, motion, and audio work cease.

## Required measurements

- device model family, without hardware serial or advertising identifier;
- Android version;
- Google Play Services version;
- Git revision;
- Maps 3D SDK version;
- cold interactive-map time;
- 5,000-contact load duration;
- median and worst movement-tick duration across ten ticks;
- rendered contact and label counts;
- observed frame-rate range during camera movement;
- memory high-water mark;
- Android thermal status before and after the run;
- selection feedback latency;
- crash, ANR, and map/session error counts.

Raw captures belong under ignored `benchmark-results/` or `captures/`. Do not commit Google map screenshots or raw logs.

## Decision rule

- **GO:** every M0 exit gate passes on reference hardware.
- **CONDITIONAL GO:** core map, model/fallback, trail, follow, lifecycle, attribution, and density gates pass; only a bounded nonessential presentation difference remains.
- **NO-GO:** lifecycle, attribution, 5,000-contact usability, model/fallback, trail, camera cancellation, terms, or Android key restriction lacks a safe native solution.

Later milestone issues are created only after a recorded GO or bounded CONDITIONAL GO.
