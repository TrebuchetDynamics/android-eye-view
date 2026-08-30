import type {
  Contact,
  GeoJsonFeatureCollection,
  GeoJsonLineFeatureCollection,
  GeoJsonPointFeature,
} from './contracts';

type Coordinate = Readonly<{latitude: number; longitude: number}>;

export class EntityStore {
  private readonly contacts = new Map<string, Contact>();
  private selectedAircraft?: Contact;
  private readonly trails = new Map<string, readonly Coordinate[]>();

  constructor(private readonly maxTrailPoints = 120) {
    if (!Number.isSafeInteger(maxTrailPoints) || maxTrailPoints < 2) {
      throw new RangeError('maxTrailPoints must be an integer of at least two');
    }
  }

  replaceContacts(contacts: readonly Contact[]): void {
    const validated = contacts.map(validateContact);
    this.contacts.clear();
    for (const contact of validated) {
      this.contacts.set(contact.id, contact);
    }
  }

  removeContacts(entityIds: readonly string[]): void {
    for (const id of entityIds) {
      validateId(id);
      this.contacts.delete(id);
    }
  }

  setSelectedAircraft(contact: Contact): void {
    this.selectedAircraft = validateContact(contact);
  }

  setTrail(id: string, points: readonly Coordinate[]): void {
    validateId(id);
    const validated = points.map(validateCoordinate);
    this.trails.set(id, validated.slice(-this.maxTrailPoints));
  }

  contactsGeoJson(): GeoJsonFeatureCollection {
    return featureCollection([...this.contacts.values()].map(pointFeature));
  }

  selectedAircraftGeoJson(): GeoJsonFeatureCollection {
    return featureCollection(this.selectedAircraft === undefined ? [] : [pointFeature(this.selectedAircraft)]);
  }

  trailGeoJson(): GeoJsonLineFeatureCollection {
    return {
      type: 'FeatureCollection',
      features: [...this.trails].map(([id, points]) => ({
        type: 'Feature',
        id,
        geometry: {
          type: 'LineString',
          coordinates: points.map(({longitude, latitude}) => [longitude, latitude] as const),
        },
        properties: {id},
      })),
    };
  }
}

function featureCollection(features: readonly GeoJsonPointFeature[]): GeoJsonFeatureCollection {
  return {type: 'FeatureCollection', features};
}

function pointFeature(contact: Contact): GeoJsonPointFeature {
  const properties: Record<string, string | number> = {
    id: contact.id,
    altitudeMeters: contact.altitudeMeters,
    headingDegrees: contact.headingDegrees,
  };
  if (contact.label !== undefined) properties.label = contact.label;
  return {
    type: 'Feature',
    id: contact.id,
    geometry: {type: 'Point', coordinates: [contact.longitude, contact.latitude]},
    properties,
  };
}

function validateContact(contact: Contact): Contact {
  validateId(contact.id);
  const coordinate = validateCoordinate(contact);
  assertFinite(contact.altitudeMeters, 'altitudeMeters');
  assertFinite(contact.headingDegrees, 'headingDegrees');
  if (contact.label !== undefined && contact.label.length > 128) {
    throw new RangeError('label must not exceed 128 characters');
  }
  return {
    ...contact,
    latitude: coordinate.latitude,
    longitude: coordinate.longitude,
  };
}

function validateCoordinate(coordinate: Coordinate): Coordinate {
  assertFinite(coordinate.latitude, 'latitude');
  assertFinite(coordinate.longitude, 'longitude');
  if (coordinate.latitude < -90 || coordinate.latitude > 90) {
    throw new RangeError('latitude must be between -90 and 90');
  }
  if (coordinate.longitude < -180 || coordinate.longitude > 180) {
    throw new RangeError('longitude must be between -180 and 180');
  }
  return {latitude: coordinate.latitude, longitude: coordinate.longitude};
}

function validateId(id: string): void {
  if (id.length === 0 || id.length > 128) {
    throw new RangeError('id must contain between 1 and 128 characters');
  }
}

function assertFinite(value: number, name: string): void {
  if (!Number.isFinite(value)) throw new TypeError(`${name} must be finite`);
}
