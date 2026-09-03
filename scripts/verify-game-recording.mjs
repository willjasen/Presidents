import { createHash, randomBytes, randomUUID } from 'node:crypto';
import postgres from 'postgres';
import auth from '../netlify/functions/auth.mjs';

const sql = postgres(process.env.DATABASE_URL, { max: 1 });
const userId = randomUUID();
const token = randomBytes(32).toString('base64url');
const tokenHash = createHash('sha256').update(token).digest('hex');
const gameId = randomUUID();

try {
  await sql`INSERT INTO users (id, username, username_key) VALUES (${userId}, 'Recording Test', ${`recording-test-${userId}`})`;
  await sql`INSERT INTO game_stats (user_id) VALUES (${userId})`;
  await sql`INSERT INTO sessions (token_hash, user_id, expires_at) VALUES (${tokenHash}, ${userId}, NOW() + INTERVAL '5 minutes')`;

  const startedAt = new Date(Date.now() - 60_000).toISOString();
  const completedAt = new Date().toISOString();
  const response = await auth(new Request('http://localhost:8888/.netlify/functions/auth', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
    body: JSON.stringify({
      action: 'save-game', gameId, startedAt, completedAt, playerCount: 2, finishPlace: 1,
      finishOrder: [0, 1], rules: { version: 1 },
      players: [
        { seat: 0, name: 'Recording Test', type: 'human', startingHand: [{ id: '0-0' }], finishPlace: 1 },
        { seat: 1, name: 'Jordan', type: 'computer', startingHand: [{ id: '1-0' }], finishPlace: 2 },
      ],
      actions: [
        { sequence: 1, trickNumber: 1, playerSeat: 0, action: 'play', cards: [{ id: '0-0', rank: '3', suit: '♣', value: 0 }], handSizeAfter: 0, pileCountBefore: 0, pileRankBefore: null, occurredAt: completedAt },
      ],
    }),
  }));
  if (!response.ok) throw new Error(`Recording endpoint returned ${response.status}: ${await response.text()}`);
  const counts = await sql`
    SELECT
      (SELECT COUNT(*)::int FROM games WHERE client_game_id = ${gameId}) AS games,
      (SELECT COUNT(*)::int FROM game_players gp JOIN games g ON g.id = gp.game_id WHERE g.client_game_id = ${gameId}) AS players,
      (SELECT COUNT(*)::int FROM game_actions ga JOIN games g ON g.id = ga.game_id WHERE g.client_game_id = ${gameId}) AS actions`;
  if (counts[0].games !== 1 || counts[0].players !== 2 || counts[0].actions !== 1) throw new Error('Recorded row counts did not match');
  console.log('Game recording verified.');
} finally {
  await sql`DELETE FROM users WHERE id = ${userId}`;
  await sql.end();
}
process.exit(0);
