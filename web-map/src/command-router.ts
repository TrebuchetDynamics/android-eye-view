import type {CameraCommand, Contact, NativeCommand, RendererPort} from './contracts';

export class InvalidCommandError extends Error {
  constructor(message: string) {
    super(message);
    this.name = 'InvalidCommandError';
  }
}

export class CommandRouter {
  constructor(private readonly renderer: RendererPort) {}

  apply(input: unknown): void {
    const command = parseCommand(input);
    switch (command.type) {
      case 'renderEntities':
        this.renderer.renderEntities(command.entities);
        return;
      case 'removeEntities':
        this.renderer.removeEntities(command.entityIds);
        return;
      case 'renderSelectedAircraft':
        this.renderer.renderSelectedAircraft(command.entity);
        return;
      case 'renderPolyline':
        this.renderer.renderPolyline(command.id, command.points);
        return;
      case 'setCamera':
        this.renderer.setCamera(command.camera);
        return;
      case 'stopCameraMotion':
        this.renderer.stopCameraMotion();
        return;
      case 'close':
        this.renderer.close();
    }
  }
}

function parseCommand(input: unknown): NativeCommand {
  const value = record(input, 'command');
  const type = text(value.type, 'command.type');
  switch (type) {
    case 'renderEntities':
      return {type, entities: array(value.entities, 'entities').map(parseContact)};
    case 'removeEntities':
      return {type, entityIds: array(value.entityIds, 'entityIds').map((id) => text(id, 'entityId'))};
    case 'renderSelectedAircraft':
      return {type, entity: parseContact(value.entity)};
    case 'renderPolyline':
      return {
        type,
        id: text(value.id, 'id'),
        points: array(value.points, 'points').map(parseCoordinate),
      };
    case 'setCamera':
      return {type, camera: parseCamera(value.camera)};
    case 'stopCameraMotion':
    case 'close':
      return {type};
    default:
      throw new InvalidCommandError(`Unsupported command type: ${type}`);
  }
}

function parseContact(input: unknown): Contact {
  const value = record(input, 'contact');
  const labelValue = value.label;
  return {
    id: text(value.id, 'contact.id'),
    latitude: finite(value.latitude, 'contact.latitude'),
    longitude: finite(value.longitude, 'contact.longitude'),
    altitudeMeters: finite(value.altitudeMeters, 'contact.altitudeMeters'),
    headingDegrees: finite(value.headingDegrees, 'contact.headingDegrees'),
    ...(labelValue === undefined ? {} : {label: text(labelValue, 'contact.label')}),
  };
}

function parseCoordinate(input: unknown): Readonly<{latitude: number; longitude: number}> {
  const value = record(input, 'coordinate');
  return {
    latitude: finite(value.latitude, 'coordinate.latitude'),
    longitude: finite(value.longitude, 'coordinate.longitude'),
  };
}

function parseCamera(input: unknown): CameraCommand {
  const value = record(input, 'camera');
  return {
    latitude: finite(value.latitude, 'camera.latitude'),
    longitude: finite(value.longitude, 'camera.longitude'),
    headingDegrees: finite(value.headingDegrees, 'camera.headingDegrees'),
    tiltDegrees: finite(value.tiltDegrees, 'camera.tiltDegrees'),
    rangeMeters: finite(value.rangeMeters, 'camera.rangeMeters'),
  };
}

function record(input: unknown, name: string): Record<string, unknown> {
  if (input === null || typeof input !== 'object' || Array.isArray(input)) {
    throw new InvalidCommandError(`${name} must be an object`);
  }
  return input as Record<string, unknown>;
}

function array(input: unknown, name: string): readonly unknown[] {
  if (!Array.isArray(input)) throw new InvalidCommandError(`${name} must be an array`);
  return input;
}

function text(input: unknown, name: string): string {
  if (typeof input !== 'string' || input.length === 0 || input.length > 128) {
    throw new InvalidCommandError(`${name} must be a non-empty string of at most 128 characters`);
  }
  return input;
}

function finite(input: unknown, name: string): number {
  if (typeof input !== 'number' || !Number.isFinite(input)) {
    throw new InvalidCommandError(`${name} must be finite`);
  }
  return input;
}
