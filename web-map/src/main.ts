import type {AndroidBridge} from './contracts';
import {CommandRouter, InvalidCommandError} from './command-router';
import {MapLibreRenderer} from './renderer';

declare global {
  interface Window {
    AndroidEyeView?: AndroidBridge;
    androidEyeView: Readonly<{applyCommand(command: unknown): void}>;
  }
}

const bridge: AndroidBridge = window.AndroidEyeView ?? {
  onReady: () => console.info('Android bridge unavailable: renderer ready'),
  onError: (message) => console.error(message),
  onUserGesture: () => undefined,
  onEntityClick: (id) => console.info('Selected entity', id),
  onMetric: (name, value) => console.debug(name, value),
};

try {
  fitDocumentToViewport();
  window.addEventListener('resize', fitDocumentToViewport);
  const renderer = new MapLibreRenderer(bridge);
  const router = new CommandRouter(renderer);
  window.androidEyeView = {
    applyCommand(command: unknown): void {
      try {
        router.apply(command);
      } catch (error) {
        const message = error instanceof InvalidCommandError || error instanceof Error
          ? error.message
          : 'Unknown map command failure';
        bridge.onError(message.slice(0, 512));
      }
    },
  };
} catch (error) {
  const message = error instanceof Error ? error.message : 'Map renderer startup failed';
  bridge.onError(message.slice(0, 512));
  window.androidEyeView = {applyCommand: () => undefined};
}

function fitDocumentToViewport(): void {
  const height = `${window.innerHeight}px`;
  document.documentElement.style.height = height;
  document.body.style.height = height;
  const map = document.getElementById('map');
  if (map !== null) map.style.height = height;
}
