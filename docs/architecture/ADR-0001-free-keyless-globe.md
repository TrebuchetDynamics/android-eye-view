# ADR-0001: Free, keyless globe renderer

- **Status:** Accepted
- **Date:** 2026-08-29
- **Decision owners:** Android Eye View maintainers

## Context

Android Eye View must preserve the mission of the God’s Eye View project while remaining free to run. Its default experience cannot require a billing account, API key, token, or paid map service.

The first M0 implementation used Google Maps 3D SDK for Android 0.2.2. The adapter, synthetic harness, and static tests passed, but physical-device rendering required a billed Google Maps Platform project. A missing or invalid key caused the experimental SDK to terminate the app from an SDK background thread instead of returning a recoverable application error. This violates the new free-runtime constraint.

The renderer must support a world-scale interactive view, 5,000 batched moving contacts, selection, a selected-aircraft representation, a trail, camera follow, user-gesture cancellation, and mandatory source attribution.

## Considered options

### Google Maps 3D SDK for Android

Rejected as the required renderer. It offers the best native photorealistic scene and first-class model API, but it requires Google Maps Platform billing and authenticated use. It may not be necessary as an optional future adapter, but no required feature may depend on it.

### MapLibre Native Android 13.6.0

Rejected for this globe M0. It is maintained, keyless, and strong for Android 2D/2.5D vector maps, fill extrusions, and batched entities. Its current Android renderer has no supported spherical globe, terrain mesh, or first-class glTF/model layer. The official Compose wrapper does not remove those native-renderer limits.

### NASA WorldWind Android and WhirlyGlobe-Maply

Rejected for the production foundation. Both provide native globes and useful annotation APIs, but their Android paths are dormant or explicitly not actively maintained. Adopting either would make Android Eye View responsible for a renderer fork before proving the product.

### CesiumJS in WebView

Viable and retained as a fallback. CesiumJS is Apache-2.0, actively maintained, supports globe, terrain, 3D Tiles, glTF, and batched primitives, and does not inherently require Cesium ion. A fully keyless default still needs a legally reliable imagery/terrain source or bundled data.

### MapLibre GL JS 6.6.0 in WebView

Selected. MapLibre GL JS is BSD-3-Clause and supports globe projection, raster/vector sources, globe terrain paths, fill extrusions, batched GeoJSON layers, feature queries, and camera control. Unlike MapLibre Native, its maintained web renderer has the required globe mode. It can consume OpenFreeMap without a proprietary account.

## Decision

Bundle MapLibre GL JS 6.6.0 JavaScript and CSS inside the APK and host one hardened WebView through a dedicated `web-map-adapter` module. Load the keyless OpenFreeMap Liberty style from `https://tiles.openfreemap.org/styles/liberty`.

Kotlin remains responsible for application state, provider-neutral entities, synthetic generation, camera ownership, and the Compose UI. The renderer accepts a small discriminated command set and reports readiness, errors, user gestures, entity clicks, and metrics through a narrow JavaScript interface.

Render all contacts through one GeoJSON source and renderer layers. Never create one Android View or Compose node per entity. Use a locally generated aircraft marker for M0 because MapLibre GL JS has no first-class model style layer; label it as a functional fallback rather than claiming a glTF model.

## Security boundary

- Serve local assets with `WebViewAssetLoader` from the HTTPS app-assets origin.
- Allow renderer network access only to the OpenFreeMap HTTPS host in the document CSP.
- Disable file/content access, mixed content, multiple windows, and third-party cookies.
- Block top-level navigation outside the app-assets origin.
- Encode commands with a JSON library, not string interpolation.
- Keep the JavaScript interface callback-only and validate every command in TypeScript.

## Attribution and data terms

OpenFreeMap's TileJSON currently attributes OpenFreeMap, OpenMapTiles, and OpenStreetMap data. The MapLibre attribution control must remain visible in every presentation mode. OpenStreetMap data remains subject to ODbL attribution obligations.

OpenFreeMap's public instance is free, keyless, and currently advertises no usage limits, but it is an as-is service without an availability SLA and may change. The style endpoint is therefore a replaceable configuration boundary. A compatible self-hosted OpenMapTiles/OpenFreeMap deployment is the escape hatch; hosting infrastructure is not falsely described as costless.

## Consequences

### Positive

- No mandatory account, token, billing setup, or proprietary SDK.
- True globe behavior at world scale with OSM-derived detail and 3D building extrusions.
- Existing Kotlin/Compose app and provider-neutral map contracts remain useful.
- Renderer updates are batched and measurable.
- The renderer can be replaced without changing provider/domain code.

### Negative

- The map surface is hybrid rather than fully native Android rendering.
- M0 uses an aircraft marker fallback instead of first-class glTF.
- OpenFreeMap availability and network access affect detailed basemap loading.
- Android-to-JavaScript serialization and WebView rendering require explicit performance validation.
- Globe projection transitions toward Mercator at high zoom by MapLibre design.

## Validation gate

The decision remains provisional until the free M0 passes physical-device globe readiness, attribution, 5,000-contact load/update, selection, trail, follow, lifecycle, failure, memory, and thermal checks documented in `docs/m0/FEASIBILITY.md`.

## Primary sources

- [MapLibre GL JS repository](https://github.com/maplibre/maplibre-gl-js)
- [MapLibre GL JS globe design guide](https://github.com/maplibre/maplibre-gl-js/blob/main/developer-guides/globe.md)
- [MapLibre Native architecture limitations](https://github.com/maplibre/maplibre-native/blob/main/docs/mdbook/src/design/archictural-problems-and-recommendations.md)
- [OpenFreeMap project](https://github.com/hyperknot/openfreemap)
- [OpenFreeMap quick start](https://openfreemap.org/quick_start/)
- [OpenFreeMap hosted-service terms](https://openfreemap.org/tos/)
- [CesiumJS repository and optional-content explanation](https://github.com/CesiumGS/cesium#where-does-the-global-3d-content-come-from)
- [NASA WorldWind Android repository](https://github.com/NASAWorldWind/WorldWindAndroid)
- [WhirlyGlobe-Maply repository](https://github.com/mousebird-consulting-inc/WhirlyGlobe)
