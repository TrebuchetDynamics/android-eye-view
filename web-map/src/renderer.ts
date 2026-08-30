import {
  Map as MapLibreMap,
  type GeoJSONSource,
  type GeoJSONSourceSpecification,
  type MapLayerMouseEvent,
  type MapSourceDataEvent,
} from 'maplibre-gl';
import 'maplibre-gl/dist/maplibre-gl.css';
import type {AndroidBridge, CameraCommand, Contact, RendererPort} from './contracts';
import {EntityStore} from './entity-store';

const STYLE_URL = 'https://tiles.openfreemap.org/styles/liberty';
const CONTACTS_SOURCE = 'contacts';
const CONTACTS_LAYER = 'contacts-points';
const LABELS_LAYER = 'contacts-labels';
const SELECTED_SOURCE = 'selected-aircraft';
const SELECTED_LAYER = 'selected-aircraft-marker';
const TRAIL_SOURCE = 'selected-trail';
const TRAIL_LAYER = 'selected-trail-line';
const AIRCRAFT_IMAGE = 'selected-aircraft-icon';

export class MapLibreRenderer implements RendererPort {
  private readonly store = new EntityStore();
  private readonly map: MapLibreMap;
  private ready = false;
  private closed = false;
  private metricFrame = 0;
  private rafHandle = 0;
  private lastFrameAt = performance.now();
  private readonly frameDurations: number[] = [];
  private contactMetricGeneration = 0;
  private pendingContactMetric: ((event: MapSourceDataEvent) => void) | undefined;

  constructor(private readonly bridge: AndroidBridge) {
    this.map = new MapLibreMap({
      container: 'map',
      style: STYLE_URL,
      center: [-25, 22],
      zoom: 1.35,
      bearing: 0,
      pitch: 0,
      attributionControl: {compact: true},
      fadeDuration: 0,
      maxPitch: 85,
    });
    this.map.on('load', () => this.onMapLoaded());
    this.map.on('error', (event) => {
      if (!this.ready && !this.closed) {
        const message = event.error instanceof Error ? event.error.message : 'Free map data failed to load';
        this.bridge.onError(message.slice(0, 512));
      }
    });
  }

  renderEntities(entities: readonly Contact[]): void {
    if (!this.isUsable()) return;
    this.store.replaceContacts(entities);
    const startedAt = performance.now();
    this.measureContactCommit(startedAt);
    this.source(CONTACTS_SOURCE).setData(asGeoJson(this.store.contactsGeoJson()));
  }

  removeEntities(entityIds: readonly string[]): void {
    if (!this.isUsable()) return;
    this.store.removeContacts(entityIds);
    this.source(CONTACTS_SOURCE).setData(asGeoJson(this.store.contactsGeoJson()));
  }

  renderSelectedAircraft(entity: Contact): void {
    if (!this.isUsable()) return;
    this.store.setSelectedAircraft(entity);
    this.source(SELECTED_SOURCE).setData(asGeoJson(this.store.selectedAircraftGeoJson()));
  }

  renderPolyline(id: string, points: readonly Readonly<{latitude: number; longitude: number}>[]): void {
    if (!this.isUsable()) return;
    this.store.setTrail(id, points);
    this.source(TRAIL_SOURCE).setData(asGeoJson(this.store.trailGeoJson()));
  }

  setCamera(camera: CameraCommand): void {
    if (!this.isUsable()) return;
    const zoom = clamp(Math.log2(40_075_016.686 / Math.max(camera.rangeMeters, 1)), 1, 18);
    this.map.easeTo({
      center: [camera.longitude, camera.latitude],
      bearing: camera.headingDegrees,
      pitch: clamp(camera.tiltDegrees, 0, 85),
      zoom,
      duration: 600,
      essential: true,
    });
  }

  stopCameraMotion(): void {
    if (!this.closed) this.map.stop();
  }

  close(): void {
    if (this.closed) return;
    this.closed = true;
    this.ready = false;
    this.contactMetricGeneration += 1;
    cancelAnimationFrame(this.rafHandle);
    if (this.pendingContactMetric !== undefined) {
      this.map.off('sourcedata', this.pendingContactMetric);
      this.pendingContactMetric = undefined;
    }
    this.map.remove();
  }

  private onMapLoaded(): void {
    if (this.closed) return;
    this.map.setProjection({type: 'globe'});
    this.map.addSource(CONTACTS_SOURCE, emptyGeoJsonSource());
    this.map.addLayer({
      id: CONTACTS_LAYER,
      type: 'circle',
      source: CONTACTS_SOURCE,
      paint: {
        'circle-radius': ['interpolate', ['linear'], ['zoom'], 1, 2, 8, 3.5, 14, 5],
        'circle-color': '#7cffb2',
        'circle-opacity': 0.88,
        'circle-stroke-color': '#04130c',
        'circle-stroke-width': 1,
      },
    });
    this.map.addLayer({
      id: LABELS_LAYER,
      type: 'symbol',
      source: CONTACTS_SOURCE,
      minzoom: 5,
      layout: {
        'text-field': ['coalesce', ['get', 'label'], ''],
        'text-size': 10,
        'text-offset': [0, 1.1],
        'text-anchor': 'top',
        'text-allow-overlap': false,
      },
      paint: {
        'text-color': '#e2f1ea',
        'text-halo-color': '#050a0d',
        'text-halo-width': 1,
      },
    });

    this.map.addImage(AIRCRAFT_IMAGE, createAircraftImage(), {pixelRatio: 2});
    this.map.addSource(SELECTED_SOURCE, emptyGeoJsonSource());
    this.map.addLayer({
      id: SELECTED_LAYER,
      type: 'symbol',
      source: SELECTED_SOURCE,
      layout: {
        'icon-image': AIRCRAFT_IMAGE,
        'icon-size': 0.75,
        'icon-rotate': ['get', 'headingDegrees'],
        'icon-rotation-alignment': 'map',
        'icon-allow-overlap': true,
      },
    });

    this.map.addSource(TRAIL_SOURCE, emptyGeoJsonSource());
    this.map.addLayer({
      id: TRAIL_LAYER,
      type: 'line',
      source: TRAIL_SOURCE,
      paint: {
        'line-color': '#80d8ff',
        'line-width': 3,
        'line-opacity': 0.85,
      },
    }, SELECTED_LAYER);

    this.map.on('click', CONTACTS_LAYER, (event: MapLayerMouseEvent) => {
      const feature = event.features?.[0];
      const id = feature?.properties?.id;
      if (typeof id === 'string') this.bridge.onEntityClick(id);
    });
    this.map.on('mouseenter', CONTACTS_LAYER, () => { this.map.getCanvas().style.cursor = 'pointer'; });
    this.map.on('mouseleave', CONTACTS_LAYER, () => { this.map.getCanvas().style.cursor = ''; });

    const releaseOwnership = (event: {originalEvent?: unknown}) => {
      if (event.originalEvent !== undefined) this.bridge.onUserGesture();
    };
    this.map.on('dragstart', releaseOwnership);
    this.map.on('rotatestart', releaseOwnership);
    this.map.on('pitchstart', releaseOwnership);
    this.map.on('zoomstart', releaseOwnership);

    this.ready = true;
    this.startFrameMetrics();
    this.bridge.onReady();
  }

  private source(id: string): GeoJSONSource {
    return this.map.getSource(id) as GeoJSONSource;
  }

  private isUsable(): boolean {
    return this.ready && !this.closed;
  }

  private measureContactCommit(startedAt: number): void {
    const generation = ++this.contactMetricGeneration;
    if (this.pendingContactMetric !== undefined) {
      this.map.off('sourcedata', this.pendingContactMetric);
    }
    const onSourceData = (event: MapSourceDataEvent) => {
      if (event.sourceId !== CONTACTS_SOURCE || !event.isSourceLoaded) return;
      this.map.off('sourcedata', onSourceData);
      if (this.pendingContactMetric === onSourceData) this.pendingContactMetric = undefined;
      this.map.once('render', () => {
        if (!this.closed && generation === this.contactMetricGeneration) {
          this.bridge.onMetric('contacts-render-ms', performance.now() - startedAt);
        }
      });
    };
    this.pendingContactMetric = onSourceData;
    this.map.on('sourcedata', onSourceData);
  }

  private startFrameMetrics(): void {
    const frame = (now: number) => {
      if (this.closed) return;
      const duration = now - this.lastFrameAt;
      this.lastFrameAt = now;
      if (duration > 0 && duration < 1_000) this.frameDurations.push(duration);
      this.metricFrame += 1;
      if (this.metricFrame >= 120) {
        const sorted = [...this.frameDurations].sort((a, b) => a - b);
        const index = Math.min(sorted.length - 1, Math.ceil(sorted.length * 0.95) - 1);
        const p95 = sorted[index];
        if (p95 !== undefined) this.bridge.onMetric('raf-p95-ms', p95);
        this.metricFrame = 0;
        this.frameDurations.length = 0;
      }
      this.rafHandle = requestAnimationFrame(frame);
    };
    this.rafHandle = requestAnimationFrame(frame);
  }
}

function emptyGeoJsonSource(): GeoJSONSourceSpecification {
  return {type: 'geojson', data: {type: 'FeatureCollection', features: []}};
}

function asGeoJson(value: unknown): Parameters<GeoJSONSource['setData']>[0] {
  return value as Parameters<GeoJSONSource['setData']>[0];
}

function createAircraftImage(): ImageData {
  const canvas = document.createElement('canvas');
  canvas.width = 64;
  canvas.height = 64;
  const context = canvas.getContext('2d');
  if (context === null) throw new Error('Canvas 2D is unavailable');
  context.clearRect(0, 0, 64, 64);
  context.fillStyle = '#ffca28';
  context.strokeStyle = '#050a0d';
  context.lineWidth = 3;
  context.beginPath();
  context.moveTo(32, 3);
  context.lineTo(40, 25);
  context.lineTo(59, 35);
  context.lineTo(58, 43);
  context.lineTo(38, 38);
  context.lineTo(38, 52);
  context.lineTo(46, 58);
  context.lineTo(45, 62);
  context.lineTo(32, 57);
  context.lineTo(19, 62);
  context.lineTo(18, 58);
  context.lineTo(26, 52);
  context.lineTo(26, 38);
  context.lineTo(6, 43);
  context.lineTo(5, 35);
  context.lineTo(24, 25);
  context.closePath();
  context.fill();
  context.stroke();
  return context.getImageData(0, 0, 64, 64);
}

function clamp(value: number, minimum: number, maximum: number): number {
  return Math.min(maximum, Math.max(minimum, value));
}
