import { cp, mkdir, rm } from 'node:fs/promises';
import { build } from 'esbuild';

await rm('dist', { recursive: true, force: true });
await mkdir('dist', { recursive: true });
await cp('web/index.html', 'dist/index.html');
await cp('web/styles.css', 'dist/styles.css');
await cp('web/vendor', 'dist/vendor', { recursive: true });
await build({
  entryPoints: ['web/app.js'],
  outfile: 'dist/app.js',
  bundle: true,
  minify: true,
  format: 'esm',
  target: ['es2022'],
});
