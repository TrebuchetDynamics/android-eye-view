import {describe, expect, it, vi} from 'vitest';
import {CommandRouter, InvalidCommandError} from './command-router';
import type {RendererPort} from './contracts';

const fakeRenderer = (): RendererPort => ({
  renderEntities: vi.fn(),
  removeEntities: vi.fn(),
  renderSelectedAircraft: vi.fn(),
  renderPolyline: vi.fn(),
  setCamera: vi.fn(),
  stopCameraMotion: vi.fn(),
  close: vi.fn(),
});

const entity = {
  id: 'synthetic-00001',
  latitude: 37.6213,
  longitude: -122.379,
  altitudeMeters: 2_000,
  headingDegrees: 90,
};

describe('CommandRouter', () => {
  it('routes every supported command to one renderer operation', () => {
    const renderer = fakeRenderer();
    const router = new CommandRouter(renderer);

    router.apply({type: 'renderEntities', entities: [entity]});
    router.apply({type: 'removeEntities', entityIds: [entity.id]});
    router.apply({type: 'renderSelectedAircraft', entity});
    router.apply({type: 'renderPolyline', id: 'trail', points: [{latitude: 1, longitude: 2}]});
    router.apply({type: 'setCamera', camera: {
      latitude: 1,
      longitude: 2,
      headingDegrees: 3,
      tiltDegrees: 4,
      rangeMeters: 5,
    }});
    router.apply({type: 'stopCameraMotion'});
    router.apply({type: 'close'});

    expect(renderer.renderEntities).toHaveBeenCalledOnce();
    expect(renderer.removeEntities).toHaveBeenCalledOnce();
    expect(renderer.renderSelectedAircraft).toHaveBeenCalledOnce();
    expect(renderer.renderPolyline).toHaveBeenCalledWith('trail', [{latitude: 1, longitude: 2}]);
    expect(renderer.setCamera).toHaveBeenCalledOnce();
    expect(renderer.stopCameraMotion).toHaveBeenCalledOnce();
    expect(renderer.close).toHaveBeenCalledOnce();
  });

  it.each([null, [], {}, {type: 'unknown'}, {type: 'renderEntities'}])(
    'rejects malformed command %j without touching renderer',
    (input) => {
      const renderer = fakeRenderer();
      const router = new CommandRouter(renderer);

      expect(() => router.apply(input)).toThrow(InvalidCommandError);
      expect(renderer.renderEntities).not.toHaveBeenCalled();
      expect(renderer.close).not.toHaveBeenCalled();
    },
  );
});
