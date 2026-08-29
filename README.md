# Android Eye View

**The native Android port of [God’s Eye View](https://github.com/bilawalsidhu/gods-eye-view).**

> Public signals, spatially understood.

Android Eye View is bringing the God’s Eye View mission to Android: one immersive, inspectable interface for exploring the public signals already broadcasting around the planet.

The target experience combines a photorealistic 3D globe with live and recent aircraft, vessels, satellites, earthquakes, fires, traffic, public cameras, radio, bikeshare, launch activity, and mapped infrastructure. Every layer must make its source, freshness, coverage, and uncertainty visible.

## Project status

> [!IMPORTANT]
> Android Eye View is currently in **M0 native feasibility validation**. The buildable debug harness is not a supported APK release.

The Kotlin/Compose foundation, isolated Maps 3D adapter, deterministic 5,000-contact harness, aircraft model/trail/follow controls, and presentation approximations are implemented. Static and keyless-device checks pass; key-backed native rendering and performance gates remain before M0 can receive a GO decision.

- Read the [product requirements](PRD.md).
- Follow the [implementation roadmap](ROADMAP.md).
- Review the current [M0 feasibility evidence](docs/m0/FEASIBILITY.md).
- Track development in this repository’s [issues](https://github.com/TrebuchetDynamics/android-eye-view/issues).

## Mission

Make public signals spatially understandable on Android.

Android Eye View will:

- replace disconnected OSINT tabs with one spatial interface;
- reveal relationships among public events, assets, infrastructure, and places;
- distinguish live, delayed, stale, partial, simulated, estimated, reconstructed, and unavailable data;
- remain open, inspectable, extensible, and BYOK-friendly;
- deliver a cinematic “living planet” without implying classified access or authoritative intelligence;
- model systems and public assets—not named people.

## Planned experience

### Explore a living globe

Navigate from a full-Earth view to a city, airport, port, incident, or tracked contact using a native photorealistic 3D map.

### Fuse public data layers

The parity target includes:

| Domain | Planned layers |
|---|---|
| Air | Civilian flights and military-tagged ADS-B contacts |
| Sea | Live AIS vessels |
| Space | Satellites, orbit paths, and reconstructed launches |
| Environment | Earthquakes, active fires, weather context, and traffic |
| Public infrastructure | CCTV catalogs, radio, bikeshare, mapped installations, datacenters, dams, and cables |

Provider coverage, licensing, credentials, and rate limits vary. A layer that cannot answer must fail independently without taking down the globe or unrelated layers.

### Track and understand

Select a supported entity, inspect its public telemetry, follow its motion, view its trail, find nearby contacts, and move into a cockpit-style camera when available.

### Operate the globe

The long-term parity target includes tactical HUDs, missions, annotations, routes, scene playback, shareable views, sensor-inspired presentation, and optional voice control.

Native Android APIs do not expose every CesiumJS rendering hook used by the upstream project. Android Eye View prioritizes **functional parity** and honest native approximations over pixel-identical effects.

## Why native Android?

The app is planned around:

- **Kotlin** for application and domain code;
- **Jetpack Compose** for phone and tablet UI;
- **Google Maps 3D SDK for Android** for the photorealistic globe;
- **Coroutines and Flow** for live asynchronous state;
- **Room and DataStore** for bounded app-owned data and preferences;
- **Android Keystore-backed storage** for user-provided credentials;
- an **optional self-hosted gateway** for capabilities that require protected server-side credentials, such as short-lived realtime voice tokens.

The map SDK is isolated behind an internal adapter because it is experimental. Data layers remain provider-neutral and independently testable.

## Product principles

1. **Public signals only.** Use lawful public or user-authorized sources.
2. **Honesty over spectacle.** Label missing, stale, inferred, simulated, and reconstructed information.
3. **No people as a query type.** No named-person tracking, face recognition, plate recognition, biometrics, or private-camera ingestion.
4. **Fail by feature.** One provider outage must not disable unrelated capabilities.
5. **Budgets are behavior.** Treat rate limits, quotas, battery, memory, and thermals as product requirements.
6. **Attribution stays visible.** Preserve required source and map credits in every presentation mode.

## Networking and credentials

Android Eye View is designed to work without a mandatory vendor account or hosted service.

- Public HTTPS feeds should be accessed directly when provider policy and mobile reliability permit.
- User-supplied app keys should be restricted at the provider and stored with Keystore-backed encryption.
- OAuth client secrets, long-lived model-provider keys, shared quota enforcement, and high-risk proxy behavior belong in an optional self-hosted gateway.
- Removing a credential must stop future requests to that provider.
- Secrets must never appear in logs, deep links, exported diagnostics, crash reports, or the APK.

## Responsible use

Android Eye View is an exploratory visualization of public and third-party data. Data may be delayed, incomplete, modeled, inferred, or wrong.

Do not use it for:

- flight or maritime navigation;
- emergency response;
- military, targeting, or surveillance operations;
- medical, investment, or other safety-critical decisions;
- identifying, locating, or profiling individuals.

Verify important information with authoritative sources.

## Upstream project and attribution

Android Eye View is the Android port of [bilawalsidhu/gods-eye-view](https://github.com/bilawalsidhu/gods-eye-view). The upstream project established the mission, interaction model, source-honesty standard, and broad parity target this repository is adapting for native Android.

Upstream code is MIT-licensed, but third-party datasets, map content, models, imagery, media, and live services retain their own terms. Reuse in this repository must preserve upstream notices and document each source independently. See the upstream [data-source documentation](https://github.com/bilawalsidhu/gods-eye-view/blob/main/DATA_SOURCES.md) and [security model](https://github.com/bilawalsidhu/gods-eye-view/blob/main/SECURITY.md).

Android Eye View is maintained by [Trebuchet Dynamics](https://github.com/TrebuchetDynamics). It does not claim endorsement by Google, Cesium, data providers, governments, militaries, or emergency services.

## Contributing

The repository is at the specification stage. Early contributions are most useful as:

- Maps 3D SDK feasibility findings;
- Android performance and lifecycle experiments;
- provider licensing or mobile-access research;
- security review of the direct-first/BYOK model;
- focused proposals tied to a roadmap milestone.

Before proposing a new data layer, read the contribution requirements in [PRD.md](PRD.md#21-open-source-contribution-requirements). Implementation conventions and build instructions will be added when the Android project scaffold lands.
