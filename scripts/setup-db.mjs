import { readFile } from 'node:fs/promises';
import { neon } from '@neondatabase/serverless';
import postgres from 'postgres';

if (!process.env.DATABASE_URL) {
  throw new Error('Set DATABASE_URL to your Neon connection string first.');
}

const databaseUrl = process.env.DATABASE_URL;
const isLocal = /@(127\.0\.0\.1|localhost)(:\d+)?\//.test(databaseUrl);
const sql = isLocal ? postgres(databaseUrl, { max: 1 }) : neon(databaseUrl);
const schema = await readFile(new URL('../netlify/schema.sql', import.meta.url), 'utf8');
if (isLocal) await sql.unsafe(schema);
else await sql.query(schema);
if (isLocal) await sql.end();
console.log('Presidents database is ready.');
