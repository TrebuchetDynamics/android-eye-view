# Android Eye View — Product Requirements Document

- **Status:** Approved design draft
- **Platform:** Android 12+ (API 31+)
- **Implementation:** Kotlin, Jetpack Compose, Maps 3D SDK for Android
- **Distribution:** Open-source source code and sideloadable APK
- **Reference product:** [God’s Eye View](https://github.com/bilawalsidhu/gods-eye-view)

## 1. Product summary

Android Eye View is a native Android spatial-intelligence explorer inspired by the mission of God’s Eye View: turn fragmented public signals into one understandable, interactive view of Earth.

The app combines a photorealistic 3D globe with live and recent public data about aircraft, vessels, satellites, earthquakes, fires, traffic, public cameras, radio, bikeshare, launches, and mapped infrastructure. Users can move from a global overview to a tracked contact or local event without switching among specialist websites.

The experience should feel like a cinematic situational-awareness console while remaining honest about its sources and limitations. Live, delayed, stale, estimated, reconstructed, simulated, partial, and unavailable data must never be presented as equivalent states.

Android Eye View is an exploratory visualization tool. It is not intended for navigation, emergency response, targeting, surveillance of individuals, or any other safety-critical or operational decision.

## 2. Mission

Make public signals spatially understandable on Android.

Android Eye View should preserve the essential mission of God’s Eye View:

- Replace a pile of disconnected OSINT tabs with one spatial interface.
- Reveal relationships among events, assets, infrastructure, and places.
- Make provenance, freshness, uncertainty, and simulation visible.
- Keep the software inspectable, extensible, and usable with user-supplied credentials.
- Deliver a dramatic “living planet” experience without implying classified access or authoritative intelligence.
- Model systems and public assets—not named people.

### Product promise

> Open the app, move anywhere on Earth, enable a public signal, and understand what is present, where it is, how fresh it is, and where the information came from.

## 3. Goals

### G1. Native Android experience

Deliver an Android-first application with native lifecycle handling, gestures, permissions, secure local settings, deep links, accessibility semantics, and adaptive phone/tablet layouts.

### G2. Near-complete functional parity

Cover the primary workflows and data families of God’s Eye View, including global exploration, live layers, contact tracking, cockpit-style following, tactical context, missions, voice control, annotations, sensor-style presentation, and scene playback.

Pixel-identical visual parity is not required where the native Maps 3D SDK lacks CesiumJS rendering hooks. In those cases, preserve the user outcome through clearly documented native approximations.

### G3. Direct-first, BYOK operation

Access public providers directly from the device when technically safe and permitted. Store user-supplied credentials with Android Keystore-backed encrypted storage. Features that cannot safely or reliably run directly must support an optional self-hosted gateway and degrade independently when it is absent.

### G4. Honest data presentation

Every layer must expose source, update time, coverage, and state. Modeled or reconstructed visuals must carry persistent labels.

### G5. Smooth interaction on modern flagship hardware

Maintain responsive globe navigation and stable tracking while live datasets update asynchronously. Apply viewport queries, level-of-detail rules, clustering, entity caps, and adaptive effects rather than attempting to render every known object at once.

### G6. Extensible layer architecture

Make each data source an independent module with a common contract so contributors can add or replace providers without modifying unrelated features.

## 4. Non-goals

- Tracking, searching for, identifying, or profiling named individuals.
- Face recognition, license-plate recognition, biometric analysis, or private-camera ingestion.
- Classified, stolen, paywalled, or unlawfully scraped data.
- Flight, maritime, disaster, military, medical, or emergency operational use.
- Guaranteed global completeness or real-time accuracy.
- Historical world-state replay at arbitrary dates.
- Offline photorealistic map downloads or caching prohibited third-party map content.
- Pixel-identical reproduction of CesiumJS post-processing effects.
- Guaranteed support for low-end Android devices in the initial release.
- A mandatory vendor-operated account, subscription, or hosted backend.
- iOS or desktop clients in the initial product scope.

## 5. Target users

### Curious explorer

Wants to explore live public activity around familiar or newsworthy places without learning specialist tools.

### Aviation, maritime, and space enthusiast

Wants an immersive view of public aircraft, vessel, satellite, and launch data with tracking and contextual metadata.

### Researcher, journalist, or educator

Wants to compare public signals spatially while seeing provenance, freshness, coverage, and uncertainty.

### Open-source contributor

Wants a documented way to add a provider, visualization, mission, or control without understanding the entire application.

## 6. Product principles

1. **Public signals only.** The app visualizes lawfully available public or user-authorized data.
2. **Honesty over spectacle.** Missing, delayed, inferred, or simulated data is labeled even when that weakens the cinematic effect.
3. **No people as a query type.** Search and tracking operate on places, events, public infrastructure, and broadcast asset identifiers.
4. **Progressive disclosure.** The globe remains primary; detailed controls appear only when relevant.
5. **Fail by feature.** One unavailable provider must not disable unrelated layers.
6. **Budgets are product behavior.** Polling, cache lifetimes, and quotas are visible and governed.
7. **Mobile constraints are real.** Density and effects adapt before the app becomes unstable or thermally unsustainable.
8. **Attribution stays visible.** Clean, cockpit, capture, and playback modes must retain required credits.

## 7. Success metrics

The open-source project does not require telemetry. When metrics are collected during opt-in testing, they must be anonymous and documented.

### Product success

- At least 90% of first-run testers can launch a mission, select a live entity, inspect its source/freshness, and return to the globe without assistance.
- At least 80% can distinguish live, stale, simulated, reconstructed, and unavailable states in usability testing.
- A new contributor can implement a sample layer using only the layer contract and contributor documentation.

### Technical success

- Cold launch reaches an interactive globe within 8 seconds on the reference device and a normal broadband connection, excluding the first Play Services component download.
- Globe navigation maintains a target of 45 FPS and does not remain below 30 FPS for more than 5 continuous seconds under the default mission density on the reference device.
- Selection feedback appears within 150 ms for already-rendered entities.
- Live entity updates do not visibly teleport under normal feed cadence; interpolation or a clearly degraded mode is used.
- A two-hour mixed exploration session has no unbounded memory growth, crash, or sustained thermal emergency on the reference device.
- Backgrounding stops high-frequency rendering, polling, microphone capture, and media playback unless an explicit user-visible foreground service is active.

### Privacy and reliability success

- No long-lived secret appears in logs, crash reports, analytics, deep links, screenshots generated by the app, or exported diagnostics.
- Disabling or removing one provider credential immediately prevents new requests to that provider.
- Every visible data layer exposes source and last-successful-update information.

## 8. Scope

### 8.1 Core globe and navigation

- Photorealistic 3D globe powered by Google Maps Platform’s Maps 3D SDK for Android.
- Touch gestures for orbit, pan, tilt, rotate, and zoom.
- Place and coordinate search.
- Home/reset-globe action.
- Programmatic fly-to, fly-around, follow, and route camera paths.
- Camera state persistence across ordinary process recreation.
- Shareable Android App Links containing camera, enabled layers, presentation mode, and at most one public tracked-entity identifier.
- Explicit handling when a shared entity is no longer available.

### 8.2 Data layers

The product target includes:

1. Civilian flights.
2. Military-tagged ADS-B flights, subject to source coverage and terms.
3. Live vessels.
4. Satellites and orbit paths.
5. Earthquakes.
6. Traffic, with a labeled simulation when live traffic is unavailable.
7. Public CCTV catalogs and user-initiated frame viewing.
8. Geolocated public radio directories and user-initiated playback.
9. Bikeshare station availability.
10. Active fires.
11. Recent space missions and reconstructed ascent playback.
12. Viewport-bounded mapped installations, labeled as incomplete community mapping.
13. Bundled or replaceable infrastructure datasets such as datacenters, dams, and submarine cables, subject to their individual licenses.

Each layer must declare whether it is direct-capable, gateway-required, or available in both modes.

### 8.3 Entity interaction

- Tap to select supported entities.
- Show stable identity, type, source, timestamp, freshness, and available telemetry.
- Track a selected entity while preserving manual escape.
- Draw a recent trail where the source permits it.
- Use class-appropriate 3D models where density and distance permit; otherwise use native markers.
- Display a nearby contacts roster around the active subject or view target.
- Filter contacts by supported type and status.
- Jump between contacts without leaving the tracking workflow.

### 8.4 Cockpit-style follow mode

- Follow a supported aircraft from a low-offset, heading-aware camera.
- Preserve a clear exit action and release camera ownership immediately on exit.
- Show a compact briefing with selected telemetry, nearby contacts, weather, and locality when sources are available.
- Support next/previous contact navigation.
- Adapt camera motion to reduce nausea and honor Android’s reduced-motion setting.
- Never imply that the view is a live onboard camera.

### 8.5 Tactical presentation

- Optional tactical HUD with coordinates, altitude, heading, view scale, source state, and selected-contact telemetry.
- Optional visual detection treatment for currently rendered entities.
- Density control that affects labels and detection presentation without changing underlying source truth.
- Native approximations of CRT, NVG, monochrome/Noir, snow/noise, and thermal-inspired modes using supported overlays and color treatment.
- Sensor-style modes must be described as presentation filters, not real sensor imagery.
- Thermal-inspired mode must not claim actual temperature measurement unless backed by a relevant source.

### 8.6 Missions

Provide curated one-tap starting points that stage layers, camera, and UI without silently changing unrelated durable preferences.

Initial missions:

- **Live Contacts:** enable flight context and frame a populated region.
- **Space Watch:** enable satellites and offer a trackable prominent satellite.
- **Environmental:** enable earthquakes and fires; missing fire credentials must not hide earthquakes.
- **Port Watch:** enable vessels and relevant public cameras near a supported port.
- **Global Infrastructure:** enable a performance-safe level-of-detail subset of infrastructure.
- **Explore Manually:** close the launcher without enabling a layer.

Mission state rules:

- A mission may enable its own layers and move the camera.
- A mission must not overwrite unrelated visual, accessibility, credential, or privacy preferences.
- A partially unavailable mission identifies the unavailable component while preserving successful components.

### 8.7 Voice control

Voice is optional and requires a user-configured compatible gateway that mints short-lived model session credentials. A long-lived OpenAI or equivalent provider key must not be embedded in the APK or sent to other app users.

Supported intent families:

- Navigate to a place or reset to globe view.
- Enable or disable a layer.
- Select, track, stop tracking, or move to the next supported contact.
- Change presentation mode and HUD state.
- Open or close relevant panels.
- Ask source-bounded questions about selected or currently loaded entities.
- Draw, clear, measure, and fly along supported annotations.
- Start, stop, or control a scene.

Voice requirements:

- The model can invoke only a fixed, schema-defined tool set.
- Tools return structured success or failure results.
- The assistant confirms only successful actions.
- Feed-provided text is treated as untrusted data, never as executable instructions.
- Microphone state is always visible.
- Stopping voice closes audio capture and model transport.
- The UI shows a configurable session budget warning and cap when the provider exposes sufficient usage data.
- All core globe and layer functions remain usable without voice.

### 8.8 Annotations and routes

- Place labeled points.
- Draw lines, arrows, polygons, and distance measurements.
- Resolve supported administrative or natural boundaries from permitted sources.
- Request pedestrian routes from an approved routing provider when configured.
- Animate a camera along a route with reduced-motion behavior.
- Mark generated routes and boundaries with their source.
- Export and import annotations in an app-owned JSON format that excludes secrets.

### 8.9 Scene direction and capture

- Play a bounded sequence of camera, layer, and presentation actions.
- Pause, resume, seek, and exit reconstructed launch playback.
- Show `RECONSTRUCTED ESTIMATE` throughout non-live ascent playback.
- Offer Android screen-capture guidance rather than bypassing operating-system capture protections.
- Keep required data and map attribution visible during clean/capture modes.

### 8.10 Settings and onboarding

- First-run mission launcher.
- Provider-status screen showing configuration, reachability, quota state when known, and last sanitized error.
- BYOK setup with links to provider documentation and key-restriction guidance.
- Optional gateway base URL with connection test and certificate validation.
- Rendering quality presets: Adaptive, Quality, Balanced, and Battery Saver.
- Controls for reduced motion, label density, background refresh, and diagnostics.
- Acknowledgment of responsible-use and non-operational-use limitations.
- License, attribution, privacy, source, and third-party terms screens.

## 9. Data-state contract

Every layer and entity must map its state into the following vocabulary:

| State | Meaning | UI requirement |
|---|---|---|
| `LIVE` | Data is within the provider-specific freshness window | Show source and last update |
| `DELAYED` | Source intentionally trails real time | Show delay semantics |
| `STALE` | Last successful data remains visible beyond normal freshness | Show age prominently |
| `PARTIAL` | Data is valid but coverage or response is incomplete | Describe known boundary |
| `SIMULATED` | Visualization is generated rather than sourced live | Persistent label |
| `ESTIMATED` | Position, pose, or value is inferred | Label affected value or view |
| `RECONSTRUCTED` | Playback is generated from event metadata/modeling | Persistent label |
| `LOADING` | First usable answer has not arrived | Never display a confident zero |
| `UNAVAILABLE` | Provider cannot currently answer | Explain configuration or network cause |
| `DISABLED` | User has turned the layer off | No polling except explicit status checks |

Provider adapters define freshness windows, but UI vocabulary and semantics are shared.

## 10. Architecture

### 10.1 Technology choice

- Kotlin.
- Jetpack Compose for application UI.
- A lifecycle-managed native `Map3DView` hosted within Compose.
- Google Maps 3D SDK for Android for photorealistic terrain, camera movement, markers, polylines, polygons, popovers, and supported glTF models.
- Kotlin coroutines and `Flow` for asynchronous state.
- Room for app-owned metadata, bounded caches, and offline configuration—not prohibited map content.
- DataStore for non-secret preferences.
- Android Keystore-backed encrypted storage for user credentials.
- Media3/ExoPlayer for user-initiated radio streams where compatible.
- WebRTC/native audio transport for optional realtime voice.
- WorkManager only for user-enabled, policy-compliant low-frequency refresh jobs.

The Maps 3D SDK and Compose integration must be isolated behind internal interfaces because the SDK and its Compose examples are experimental. No domain or data layer may depend directly on Google SDK classes.

### 10.2 Module boundaries

```text
app/                 Composition root, navigation, lifecycle, deep links
core-model/          Provider-neutral entities, coordinates, freshness states
core-map/            Internal map/camera/rendering interfaces
maps3d-adapter/      Google Maps 3D SDK integration
layer-api/           Layer contracts, registry, lifecycle, query interfaces
layers/               One independent module per live or bundled layer
tracking/             Selection, interpolation, trails, nearby contacts
missions/             Declarative mission definitions and execution
presentation/         HUD, filters, density, adaptive quality
voice/                Session transport, tool schemas, guarded dispatcher
annotations/          Geometry, measurement, boundary and route workflows
gateway-client/       Optional self-hosted gateway API client
storage/              Room, DataStore, encrypted secrets, bounded caches
diagnostics/          Redacted logs, health state, export
```

### 10.3 Layer contract

Each layer must expose:

- Stable ID and display metadata.
- Source and attribution metadata.
- Credential and gateway requirements.
- Enable, disable, refresh, and retry lifecycle.
- Viewport or global query policy.
- Typed records normalized into provider-neutral models.
- Freshness, coverage, and error state.
- Optional selection, tracking, history, nearby-query, and statistics capabilities.
- Rendering hints and safe entity caps.
- Cache and retention policy.
- Sanitized diagnostics.

Enabling one layer must not initialize every layer. Disabled layers must release high-frequency jobs and renderer objects.

### 10.4 Direct access and optional gateway

| Capability | Preferred path | Gateway expectation |
|---|---|---|
| Google Maps 3D | Native SDK with Android-restricted app key | Not used |
| Anonymous/public HTTPS feeds | Direct with bounded client caching | Optional |
| User-keyed HTTPS feeds | Direct when provider permits; key encrypted locally | Optional/recommended for quota sharing |
| AIS WebSocket | Direct BYOK when supported | Recommended for stable reconnect/cache behavior |
| OpenSky OAuth | Gateway | Required for client-secret OAuth; anonymous direct mode may remain available |
| OpenAI-compatible realtime voice | Short-lived token from gateway | Required |
| CCTV catalogs/frames | Direct only to adapter-owned allowlisted hosts | Recommended where upstream compatibility requires it |
| Shared quota governance | Not available across devices | Required for multi-device deployments |

Gateway endpoints must be fixed-purpose. The gateway must not expose arbitrary URL fetching or act as an open relay.

### 10.5 Data flow

1. The camera state produces a throttled viewport and altitude stream.
2. Enabled layer coordinators decide whether a refresh is needed.
3. Provider adapters request bounded data directly or through the configured gateway.
4. Responses are validated, normalized, deduplicated, and timestamped.
5. Repositories publish immutable layer snapshots and state.
6. Tracking interpolates eligible movement between known fixes.
7. The presentation coordinator applies level-of-detail, clustering, and device budgets.
8. The map adapter applies a minimal diff to native map objects.
9. UI surfaces consume the same snapshots used for rendered entities so counts and details cannot drift silently.

### 10.6 Camera ownership

Only one subsystem may own programmatic camera motion at a time. Priority is:

1. Explicit user gesture.
2. Exit or safety action.
3. Direct navigation command.
4. Tracking or cockpit follow.
5. Scene or mission playback.
6. Ambient animation.

A higher-priority action cancels the lower-priority owner and leaves the camera in a valid, level state.

## 11. Native parity decisions

| Reference capability | Android requirement |
|---|---|
| Photorealistic globe | Native Maps 3D SDK |
| Entity billboards and labels | Native markers/popovers with density budgets |
| Aircraft models | SDK-supported glTF models at close range |
| Trails and orbits | Native polylines, simplified by zoom/distance |
| Polygons and viewsheds | Native geometry where supported; clearly label estimated camera poses |
| CRT/NVG/Noir/Snow | Compose/native overlays and supported color treatment |
| FLIR/thermal | Thermal-inspired presentation only; never claim measured heat |
| Screen-space detection boxes | Native marker/highlight approximation if reliable projection is unavailable |
| Cockpit | Heading-aware camera follow with native telemetry overlays |
| Scene director | Serialized camera and app-state timeline |
| Share links | Android App Links with a versioned compact state payload |
| Voice tools | Native guarded command dispatcher |
| Visual grounding from viewport | Deferred unless capture, consent, privacy, and provider terms are explicitly satisfied |

## 12. Performance requirements

### Reference device class

- Android 12 or newer.
- Vulkan-capable flagship-class GPU.
- 8 GB RAM or more.
- Current Google Play Services.

### Budgets

- Default visible moving entities: adaptive, with a hard safety cap per layer.
- Labels: clustered and capped separately from entities.
- 3D models: distance- and device-budgeted; markers remain the fallback.
- Trails: bounded by time, point count, and simplification tolerance.
- Network refresh: provider-specific minimum intervals with request coalescing.
- Database/cache: explicit maximum size and retention per source.
- UI state updates: diffed; no full layer rebuild on each telemetry fix.
- Hidden/background app: stop render-driven updates and high-frequency polling.

### Adaptive degradation order

1. Reduce labels.
2. Reduce decorative effects.
3. Replace distant 3D models with markers.
4. Simplify trails and polygons.
5. Reduce visible low-priority entities.
6. Lower update/render cadence.

The app must not silently alter source records or label dropped rendering as missing source data.

## 13. Security and privacy

### Credentials

- Never commit, bundle, or log real credentials.
- Restrict the Google Maps key to the Android package name, signing certificate, and required API.
- Store user-provided secrets using Keystore-backed encryption.
- Exclude secrets from Android backup where appropriate.
- Clear a credential and related cached authorization material when the user removes it.
- Long-lived model provider keys and OAuth client secrets belong on the optional gateway, not in the distributed APK.

### Networking

- HTTPS only except explicitly documented local development endpoints.
- Validate schemas, content types, response sizes, redirects, and timeouts.
- Adapter-owned host allowlists for any URL-bearing catalog such as CCTV or radio.
- Do not provide arbitrary remote URL fetching.
- Apply exponential backoff with jitter and provider-specific rate limits.
- Redact credentials, tokens, image payloads, and precise user-origin data from diagnostics.

### User privacy

- No account required.
- No location permission required for globe exploration.
- “Go to my location” is optional, foreground-only by default, and explains why permission is needed.
- Microphone permission is requested only when voice is started.
- Radio playback connects to third-party broadcasters and must disclose that the broadcaster can observe the device IP address.
- Exported links must not include keys, microphone data, local file paths, private annotations by default, or precise device location unless the user explicitly chooses to share that map position.
- Analytics and crash reporting are off by default unless a build documents and requests opt-in.

## 14. Legal, licensing, and attribution

- Preserve the upstream MIT license for reused code and document modifications.
- Do not imply endorsement by or affiliation with God’s Eye View, Google, Cesium, data providers, governments, militaries, or emergency services.
- Treat each dataset and model as independently licensed.
- Keep required attribution visible in every map mode.
- Do not cache, export, or redistribute Google Maps content contrary to Google Maps Platform terms.
- Flag non-commercial data at build time and make it removable as one bounded module.
- The TeleGeography submarine-cable dataset must not be included in a commercial build without an appropriate license.
- OpenSky and news-source terms must be reviewed before any commercial or operational deployment.
- Every new source requires a documented license, use purpose, attribution text, retention rule, and commercial-use status before merge.

## 15. Accessibility and mobile UX

- Meet WCAG 2.2 AA-equivalent contrast and target-size goals for native controls where practical.
- Provide content descriptions and traversal order for controls and selected-entity details.
- Do not rely on color alone for source or freshness state.
- Support system font scaling without obscuring the primary exit and safety actions.
- Support reduced motion by disabling orbit ambience, banking, and nonessential camera shaping.
- Provide portrait exploration and an expanded landscape/tablet console layout.
- Keep tap targets at least 48 dp.
- Ensure Back exits transient modes in a predictable order before leaving the app.
- Provide keyboard shortcuts for tablets and external keyboards where Android conventions allow.

## 16. Error handling

- Preserve the last valid snapshot as `STALE` during transient failure when provider terms allow caching.
- Reject malformed responses atomically unless an adapter explicitly supports validated partial records.
- Show actionable configuration errors without exposing raw provider responses or secrets.
- Distinguish no records from no answer.
- Retry automatically only within bounded policy; always offer a manual retry.
- Surface gateway incompatibility with supported protocol version information.
- If the Maps 3D SDK is unavailable, show a blocking globe error with diagnostics and setup guidance; do not represent a blank map as successful startup.
- If a layer fails, retain the globe and all independent layers.

## 17. Testing strategy

### Unit tests

- Provider response parsing and schema rejection.
- Coordinate, altitude, heading, interpolation, dead-reckoning, orbit, and distance calculations.
- Freshness-state transitions.
- Layer lifecycle and cancellation.
- Mission persistence boundaries.
- Camera ownership arbitration.
- Tool schemas and voice authorization.
- Deep-link serialization, validation, and size limits.
- Secret redaction.
- Level-of-detail and entity-budget policy.

### Contract tests

- Recorded, licensed-safe provider fixtures.
- Gateway protocol compatibility.
- Every provider’s empty, delayed, partial, malformed, throttled, and unavailable response.
- Attribution and credential requirement metadata for every registered layer.

### Instrumented tests

- App launch and map readiness.
- Permission flows.
- Background/foreground lifecycle.
- Process recreation.
- Selection, tracking, cockpit entry/exit, and gesture interruption.
- Missions with fully configured, partially configured, and keyless states.
- App Links with valid, expired, unsupported, and hostile payloads.
- Keystore credential add/remove behavior.
- Accessibility checks for primary workflows.

### Visual and performance tests

- Golden tests for Compose panels and state badges.
- Reference-device frame-time, memory, battery, and thermal runs.
- Dense airport, global satellite, port, and infrastructure stress scenes.
- Two-hour soak test with repeated layer and mode transitions.
- Network shaping for latency, packet loss, offline recovery, and rate limiting.

### Security tests

- Static secret scan of source, resources, APK, and diagnostics fixtures.
- TLS and redirect handling.
- Oversized and malformed payload rejection.
- Gateway SSRF and open-relay negative tests.
- Voice prompt-injection tests using hostile feed text.
- Export/deep-link privacy tests.

## 18. Delivery milestones

Near-complete parity is the release target, but work is accepted through independently usable milestones.

### M0 — Feasibility gates

- Prove Maps 3D SDK rendering and lifecycle stability on reference devices.
- Render and update at least 5,000 synthetic markers under adaptive density rules.
- Demonstrate one moving glTF aircraft, a trail, selection, and camera follow.
- Validate supported camera and annotation APIs.
- Prototype native presentation-filter approximations.
- Confirm Google Maps Platform terms and key restriction flow for sideloaded builds.

**Exit:** no unresolved blocker to the core globe, live entities, camera tracking, or required attribution.

### M1 — Living globe foundation

- App shell, onboarding, settings, provider status, and attribution.
- Globe navigation, search, camera controller, and adaptive quality.
- Civilian flights, military flights, satellites, and earthquakes.
- Selection, trails, contacts, tracking, HUD, and share links.
- Live Contacts, Space Watch, and Environmental missions.

**Exit:** a user can install, configure one map key, complete the core mission loop, and identify source/freshness for every visible record.

### M2 — Broad signal fusion

- Vessels, fires, traffic, CCTV, radio, bikeshare, launches, installations, and licensed infrastructure modules.
- Provider-specific budget controls and cache policies.
- Cross-layer nearest-camera and context handoffs.
- Port Watch and Global Infrastructure missions.

**Exit:** all target data families function independently and survive partial configuration.

### M3 — Immersive operation

- Cockpit mode and briefing.
- 3D model level-of-detail.
- Tactical and sensor-style presentation.
- Annotations, distance, supported boundary resolution, routes, and route fly-through.
- Scene direction and reconstructed launch playback.

**Exit:** the principal non-voice interactive workflows have functional parity or a documented native approximation.

### M4 — Voice and hardening

- Versioned self-hosted gateway contract and reference deployment.
- Realtime voice session, fixed tools, source-bounded questions, and budget controls.
- Security review, accessibility audit, performance tuning, soak testing, and contributor documentation.

**Exit:** near-complete functional parity requirements are met, known visual differences are documented, and release checks pass.

## 19. Release criteria

A public alpha may be published when:

- M0 through M4 exit criteria pass on the reference device set.
- All required attributions and licenses have been reviewed.
- No known critical secret exposure, arbitrary-fetch, remote-code-execution, or microphone privacy issue remains.
- The APK contains no real provider credentials.
- Core use remains possible without voice or the optional gateway.
- Every modeled, simulated, estimated, partial, stale, and reconstructed state is labeled.
- Unsupported reference features and experimental SDK risks are listed in release notes.
- Setup, key restriction, gateway, responsible-use, and data-source documentation are complete.

## 20. Risks and mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Maps 3D SDK remains experimental or changes API | Rework or release instability | Isolate adapter; lock tested version; maintain M0 gates; avoid SDK types in domain modules |
| Native SDK lacks CesiumJS post-processing/projection hooks | Reduced visual parity | Functional overlays, native highlights, honest differences, defer only nonessential effects |
| Dense live layers overwhelm mobile GPU/CPU | Jank, heat, crashes | Adaptive LOD, clustering, caps, diff updates, reference-device stress gates |
| Direct BYOK exposes recoverable user secrets on a compromised device | Credential abuse | Keystore encryption, restricted keys, provider budgets, gateway requirement for high-risk secrets, clear threat disclosure |
| Provider terms prohibit a desired access or cache pattern | Feature removal | Per-adapter legal metadata, direct/gateway switches, replaceable providers, no prohibited caching |
| Public feeds are incomplete or delayed | Misleading conclusions | Visible provenance, freshness, coverage, and non-operational-use warnings |
| Optional gateway increases setup complexity | Voice and some feeds remain unavailable | Core app stays gateway-free; provide a versioned reference gateway and connection diagnostics |
| Sideload signing changes break Google key restrictions | Map fails after install | Document signing fingerprints; provide build-specific key setup and actionable diagnostics |
| Third-party radio/camera content creates privacy or content risk | Unexpected exposure or content | User-initiated playback only, source disclosure, allowlists, no recording by default |
| Tactical visual language is mistaken for authority | Misuse or reputational harm | Persistent public-data framing, no classified claims, no named-person features, clear disclaimers |

## 21. Open-source contribution requirements

A new layer is mergeable only when it includes:

- Provider adapter and normalized model mapping.
- Source, license, terms, attribution, and commercial-use documentation.
- Credential and gateway classification.
- Freshness and coverage semantics.
- Cache, polling, quota, and failure policies.
- Unit and contract fixtures.
- Performance caps and level-of-detail behavior.
- UI states for loading, live, stale, partial, unavailable, and disabled as applicable.
- Confirmation that it does not introduce named-person tracking or prohibited data.

## 22. Reference documentation

- [God’s Eye View repository](https://github.com/bilawalsidhu/gods-eye-view)
- [God’s Eye View data sources](https://github.com/bilawalsidhu/gods-eye-view/blob/main/DATA_SOURCES.md)
- [God’s Eye View security model](https://github.com/bilawalsidhu/gods-eye-view/blob/main/SECURITY.md)
- [Google Maps 3D SDK for Android overview](https://developers.google.com/maps/documentation/maps-3d/android-sdk/overview)
- [Google Maps 3D SDK Android samples](https://github.com/googlemaps-samples/android-maps3d-samples)
- [Google Photorealistic 3D Tiles documentation](https://developers.google.com/maps/documentation/tile/3d-tiles)

## 23. Product decision record

- **Native over wrapper:** Kotlin and native Android were selected over a CesiumJS WebView, Flutter, or React Native foundation.
- **Functional over pixel parity:** Native approximations are accepted where the Maps 3D SDK lacks equivalent rendering hooks.
- **Near-complete target:** The product targets the broad mission and principal workflows of God’s Eye View rather than a narrow three-layer demo.
- **Direct-first networking:** The app connects directly where safe and allowed; a self-hosted gateway is optional for the core experience and required for protected server-side capabilities such as realtime voice tokens.
- **Open-source sideloading:** Initial distribution prioritizes source availability, reproducible APK builds, and user-supplied credentials.
- **Flagship baseline:** Initial quality and density targets assume modern Android 12+ flagship-class devices.
