import { createServer } from 'node:http';
import { readFile, stat } from 'node:fs/promises';
import { extname, resolve, sep } from 'node:path';
import auth from '../netlify/functions/auth.mjs';

const port = Number(process.env.PORT || 8888);
const root = resolve('dist');
const contentTypes = {
  '.css': 'text/css; charset=utf-8',
  '.html': 'text/html; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.svg': 'image/svg+xml',
};

async function requestBody(request) {
  const chunks = [];
  for await (const chunk of request) chunks.push(chunk);
  return Buffer.concat(chunks);
}

async function sendWebResponse(nodeResponse, webResponse) {
  nodeResponse.writeHead(webResponse.status, Object.fromEntries(webResponse.headers));
  nodeResponse.end(Buffer.from(await webResponse.arrayBuffer()));
}

const server = createServer(async (request, response) => {
  try {
    const url = new URL(request.url, `http://${request.headers.host || `localhost:${port}`}`);
    if (url.pathname === '/.netlify/functions/auth') {
      const body = await requestBody(request);
      const webRequest = new Request(url, {
        method: request.method,
        headers: request.headers,
        body: ['GET', 'HEAD'].includes(request.method) ? undefined : body,
      });
      return sendWebResponse(response, await auth(webRequest));
    }

    const requestedPath = url.pathname === '/' ? 'index.html' : decodeURIComponent(url.pathname.slice(1));
    let filePath = resolve(root, requestedPath);
    if (filePath !== root && !filePath.startsWith(`${root}${sep}`)) {
      response.writeHead(403).end('Forbidden');
      return;
    }
    try {
      if (!(await stat(filePath)).isFile()) throw new Error('Not a file');
    } catch {
      filePath = resolve(root, 'index.html');
    }
    const data = await readFile(filePath);
    response.writeHead(200, {
      'Content-Type': contentTypes[extname(filePath)] || 'application/octet-stream',
      'Cache-Control': 'no-store',
    });
    response.end(data);
  } catch (error) {
    console.error(error);
    response.writeHead(500, { 'Content-Type': 'application/json' });
    response.end(JSON.stringify({ error: 'Local server error' }));
  }
});

server.listen(port, '127.0.0.1', () => {
  console.log(`Presidents local deployment: http://localhost:${port}`);
});
