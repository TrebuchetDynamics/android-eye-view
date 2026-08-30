import {describe, expect, it} from 'vitest';
import {EntityStore} from './entity-store';
import type {Contact} from './contracts';

const contact = (index: number, label?: string): Contact => ({
  id: `synthetic-${String(index).padStart(5, '0')}`,
  latitude: 37.6213 + index / 10_000,
  longitude: -122.379 - index / 10_000,
  altitudeMeters: 2_000 + index,
  headingDegrees: 90,
  ...(label === undefined ? {} : {label}),
});

describe('EntityStore', () => {
  it('replaces exactly five thousand stable contacts', () => {
    const store = new EntityStore();
    const contacts = Array.from({length: 5_000}, (_, index) => contact(index + 1));

    store.replaceContacts(contacts);

    expect(store.contactsGeoJson().features).toHaveLength(5_000);
    expect(store.contactsGeoJson().features[0]?.id).toBe('synthetic-00001');
  });

  it('encodes longitude before latitude and omits absent labels', () => {
    const store = new EntityStore();
    store.replaceContacts([contact(1), contact(2, 'SIM-00002')]);

    const features = store.contactsGeoJson().features;

    expect(features[0]?.geometry.coordinates[0]).toBeCloseTo(-122.3791, 8);
    expect(features[0]?.geometry.coordinates[1]).toBeCloseTo(37.6214, 8);
    expect(features[0]?.properties).toEqual({
      id: 'synthetic-00001',
      altitudeMeters: 2001,
      headingDegrees: 90,
    });
    expect(features[1]?.properties.label).toBe('SIM-00002');
  });

  it('removes only requested IDs', () => {
    const store = new EntityStore();
    store.replaceContacts([contact(1), contact(2), contact(3)]);

    store.removeContacts(['synthetic-00002', 'missing']);

    expect(store.contactsGeoJson().features.map((feature) => feature.id)).toEqual([
      'synthetic-00001',
      'synthetic-00003',
    ]);
  });

  it('tracks a selected aircraft and bounded immutable trail', () => {
    const store = new EntityStore(3);
    store.setSelectedAircraft(contact(1, 'SIM-00001'));
    store.setTrail('primary', [
      {latitude: 1, longitude: 2},
      {latitude: 3, longitude: 4},
      {latitude: 5, longitude: 6},
      {latitude: 7, longitude: 8},
    ]);
    const first = store.trailGeoJson();
    store.setTrail('primary', [{latitude: 9, longitude: 10}, {latitude: 11, longitude: 12}]);
    store.setTrail('alternate', [{latitude: 13, longitude: 14}, {latitude: 15, longitude: 16}]);

    expect(store.selectedAircraftGeoJson().features[0]?.properties.headingDegrees).toBe(90);
    expect(first.features[0]?.geometry.coordinates).toEqual([[4, 3], [6, 5], [8, 7]]);
    expect(store.trailGeoJson().features).toEqual([
      expect.objectContaining({
        id: 'primary',
        geometry: expect.objectContaining({coordinates: [[10, 9], [12, 11]]}),
      }),
      expect.objectContaining({
        id: 'alternate',
        geometry: expect.objectContaining({coordinates: [[14, 13], [16, 15]]}),
      }),
    ]);
  });

  it('rejects unsafe IDs and non-finite coordinates', () => {
    const store = new EntityStore();

    expect(() => store.replaceContacts([{...contact(1), id: 'x'.repeat(129)}])).toThrow();
    expect(() => store.replaceContacts([{...contact(1), latitude: Number.NaN}])).toThrow();
  });
});
