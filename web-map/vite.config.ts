import {defineConfig} from 'vitest/config';

export default defineConfig({
  base: './',
  build: {
    outDir: '../app/src/main/assets/map',
    emptyOutDir: true,
    sourcemap: false,
    target: 'es2022',
  },
  test: {
    environment: 'node',
  },
});
