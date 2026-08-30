import {copyFile, mkdir} from 'node:fs/promises';
import {fileURLToPath} from 'node:url';
import {dirname, resolve} from 'node:path';

const projectDirectory = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const distributionDirectory = resolve(projectDirectory, 'node_modules/maplibre-gl/dist');
const destinationDirectory = resolve(projectDirectory, '../app/src/main/assets/map/assets');
const runtimeModules = ['maplibre-gl-worker.mjs', 'maplibre-gl-shared.mjs'];

await mkdir(destinationDirectory, {recursive: true});
for (const moduleName of runtimeModules) {
  await copyFile(
    resolve(distributionDirectory, moduleName),
    resolve(destinationDirectory, moduleName),
  );
}
