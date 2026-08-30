export type Contact = Readonly<{
  id: string;
  latitude: number;
  longitude: number;
  altitudeMeters: number;
  headingDegrees: number;
  label?: string;
}>;

export type GeoJsonPointFeature = Readonly<{
  type: 'Feature';
  id: string;
  geometry: Readonly<{type: 'Point'; coordinates: readonly [number, number]}>;
  properties: Readonly<Record<string, string | number>>;
}>;

export type GeoJsonFeatureCollection = Readonly<{
  type: 'FeatureCollection';
  features: readonly GeoJsonPointFeature[];
}>;

export type GeoJsonLineFeatureCollection = Readonly<{
  type: 'FeatureCollection';
  features: readonly Readonly<{
    type: 'Feature';
    id: string;
    geometry: Readonly<{
      type: 'LineString';
      coordinates: readonly (readonly [number, number])[];
    }>;
    properties: Readonly<{id: string}>;
  }>[];
}>;

export type CameraCommand = Readonly<{
  latitude: number;
  longitude: number;
  headingDegrees: number;
  tiltDegrees: number;
  rangeMeters: number;
}>;

export type NativeCommand =
  | Readonly<{type: 'renderEntities'; entities: readonly Contact[]}>
  | Readonly<{type: 'removeEntities'; entityIds: readonly string[]}>
  | Readonly<{type: 'renderSelectedAircraft'; entity: Contact}>
  | Readonly<{
      type: 'renderPolyline';
      id: string;
      points: readonly Readonly<{latitude: number; longitude: number}>[];
    }>
  | Readonly<{type: 'setCamera'; camera: CameraCommand}>
  | Readonly<{type: 'stopCameraMotion'}>
  | Readonly<{type: 'close'}>;

export interface RendererPort {
  renderEntities(entities: readonly Contact[]): void;
  removeEntities(entityIds: readonly string[]): void;
  renderSelectedAircraft(entity: Contact): void;
  renderPolyline(id: string, points: readonly Readonly<{latitude: number; longitude: number}>[]): void;
  setCamera(camera: CameraCommand): void;
  stopCameraMotion(): void;
  close(): void;
}

export interface AndroidBridge {
  onReady(): void;
  onError(message: string): void;
  onUserGesture(): void;
  onEntityClick(id: string): void;
  onMetric(name: string, value: number): void;
}
