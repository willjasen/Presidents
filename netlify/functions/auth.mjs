import { createHash, randomBytes, randomUUID } from 'node:crypto';
import { neon } from '@neondatabase/serverless';
import postgres from 'postgres';
import {
  generateAuthenticationOptions,
  generateRegistrationOptions,
  verifyAuthenticationResponse,
  verifyRegistrationResponse,
} from '@simplewebauthn/server';

const json = (body, status = 200) => new Response(JSON.stringify(body), {
  status,
  headers: { 'Content-Type': 'application/json', 'Cache-Control': 'no-store' },
});

let cachedDatabase;
function db() {
  if (!process.env.DATABASE_URL) throw new Error('DATABASE_URL is not configured');
  if (cachedDatabase) return cachedDatabase;
  const isLocal = /@(127\.0\.0\.1|localhost)(:\d+)?\//.test(process.env.DATABASE_URL);
  cachedDatabase = isLocal
    ? postgres(process.env.DATABASE_URL, { max: 4, idle_timeout: 20 })
    : neon(process.env.DATABASE_URL);
  return cachedDatabase;
}

async function transaction(sql, makeStatements) {
  if (typeof sql.begin === 'function') {
    return sql.begin(async (tx) => {
      for (const statement of makeStatements(tx)) await statement;
    });
  }
  return sql.transaction(makeStatements(sql));
}

function relyingParty(request) {
  const requestUrl = new URL(request.url);
  const origin = (process.env.WEBAUTHN_ORIGIN || requestUrl.origin).replace(/\/$/, '');
  const rpID = process.env.WEBAUTHN_RP_ID || new URL(origin).hostname;
  return { origin, rpID, rpName: process.env.WEBAUTHN_RP_NAME || 'scummie' };
}

function hashToken(value) {
  return createHash('sha256').update(value).digest('hex');
}

function cleanUsername(value) {
  const username = String(value || '').trim().replace(/\s+/g, ' ');
  if (!/^[\p{L}\p{N} ._-]{2,24}$/u.test(username)) {
    throw new Error('Use 2–24 letters, numbers, spaces, periods, dashes, or underscores.');
  }
  return username;
}

function isUuid(value) {
  return /^[0-9a-f]{8}-[0-9a-f]{4}-[1-8][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(String(value || ''));
}

function validDate(value) {
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? null : date.toISOString();
}

function validateGameRecord(body) {
  const playerCount = Number(body.playerCount);
  const startedAt = validDate(body.startedAt);
  const completedAt = validDate(body.completedAt);
  const players = Array.isArray(body.players) ? body.players : [];
  const actions = Array.isArray(body.actions) ? body.actions : [];
  const finishOrder = Array.isArray(body.finishOrder) ? body.finishOrder.map(Number) : [];
  if (!isUuid(body.gameId) || !Number.isInteger(playerCount) || playerCount < 2 || playerCount > 7
      || players.length !== playerCount || finishOrder.length !== playerCount
      || new Set(finishOrder).size !== playerCount || finishOrder.some((seat) => seat < 0 || seat >= playerCount)
      || !startedAt || !completedAt || new Date(completedAt) < new Date(startedAt)
      || actions.length < 1 || actions.length > 300) {
    throw new Error('Invalid game record');
  }
  const normalizedPlayers = players.map((player, index) => {
    const seat = Number(player.seat);
    const finishPlace = Number(player.finishPlace);
    const startingHand = Array.isArray(player.startingHand) ? player.startingHand.slice(0, 52) : [];
    if (seat !== index || !['human', 'computer'].includes(player.type)
        || !Number.isInteger(finishPlace) || finishPlace < 1 || finishPlace > playerCount) {
      throw new Error('Invalid player record');
    }
    if (finishPlace !== finishOrder.indexOf(seat) + 1) throw new Error('Invalid player record');
    return { seat, finishPlace, type: player.type, name: String(player.name || '').slice(0, 40), startingHand };
  });
  const normalizedActions = actions.map((action, index) => {
    const sequence = Number(action.sequence);
    const trickNumber = Number(action.trickNumber);
    const playerSeat = Number(action.playerSeat);
    const handSizeAfter = Number(action.handSizeAfter);
    const pileCountBefore = Number(action.pileCountBefore);
    const occurredAt = validDate(action.occurredAt);
    const cards = Array.isArray(action.cards) ? action.cards.slice(0, 4) : [];
    const pileRankBefore = action.pileRankBefore == null ? null : Number(action.pileRankBefore);
    if (sequence !== index + 1 || !Number.isInteger(trickNumber) || trickNumber < 1
        || !Number.isInteger(playerSeat) || playerSeat < 0 || playerSeat >= playerCount
        || !['play', 'pass'].includes(action.action) || !Number.isInteger(handSizeAfter) || handSizeAfter < 0
        || !Number.isInteger(pileCountBefore) || pileCountBefore < 0 || !occurredAt
        || (action.action === 'play' && cards.length < 1) || (action.action === 'pass' && cards.length !== 0)) {
      throw new Error('Invalid action record');
    }
    return { sequence, trickNumber, playerSeat, handSizeAfter, pileCountBefore, pileRankBefore, occurredAt, cards, action: action.action };
  });
  const finishPlace = Number(body.finishPlace);
  if (!Number.isInteger(finishPlace) || finishPlace < 1 || finishPlace > playerCount) throw new Error('Invalid finish place');
  return { playerCount, startedAt, completedAt, players: normalizedPlayers, actions: normalizedActions, finishOrder, finishPlace };
}

async function issueSession(sql, userId) {
  const token = randomBytes(32).toString('base64url');
  await sql`INSERT INTO sessions (token_hash, user_id, expires_at)
    VALUES (${hashToken(token)}, ${userId}, NOW() + INTERVAL '30 days')`;
  return token;
}

async function authenticatedUser(sql, request) {
  const value = request.headers.get('authorization') || '';
  if (!value.startsWith('Bearer ')) return null;
  const rows = await sql`
    SELECT u.id, u.username,
      COALESCE(g.games_played, 0)::int AS games_played,
      COALESCE(g.wins, 0)::int AS wins,
      g.best_place
    FROM sessions s
    JOIN users u ON u.id = s.user_id
    LEFT JOIN game_stats g ON g.user_id = u.id
    WHERE s.token_hash = ${hashToken(value.slice(7))} AND s.expires_at > NOW()
    LIMIT 1`;
  return rows[0] || null;
}

function publicUser(row) {
  return {
    id: row.id,
    username: row.username,
    gamesPlayed: row.games_played || 0,
    wins: row.wins || 0,
    bestPlace: row.best_place || null,
  };
}

async function registerOptions(sql, body, rp) {
  const username = cleanUsername(body.username);
  const usernameKey = username.toLocaleLowerCase('en-US');
  const existing = await sql`SELECT 1 FROM users WHERE username_key = ${usernameKey} LIMIT 1`;
  if (existing.length) return json({ error: 'That player name is already taken.' }, 409);

  const userId = randomUUID();
  const options = await generateRegistrationOptions({
    rpName: rp.rpName,
    rpID: rp.rpID,
    userID: new TextEncoder().encode(userId),
    userName: username,
    userDisplayName: username,
    attestationType: 'none',
    authenticatorSelection: { residentKey: 'required', userVerification: 'required' },
  });
  const challengeId = randomUUID();
  await sql`INSERT INTO auth_challenges
    (id, purpose, challenge, proposed_user_id, username, expires_at)
    VALUES (${challengeId}, 'register', ${options.challenge}, ${userId}, ${username}, NOW() + INTERVAL '5 minutes')`;
  return json({ challengeId, options });
}

async function registerVerify(sql, body, rp) {
  const rows = await sql`SELECT * FROM auth_challenges
    WHERE id = ${body.challengeId} AND purpose = 'register' AND expires_at > NOW() LIMIT 1`;
  const challenge = rows[0];
  if (!challenge) return json({ error: 'That sign-up request expired. Please try again.' }, 400);

  const verification = await verifyRegistrationResponse({
    response: body.credential,
    expectedChallenge: challenge.challenge,
    expectedOrigin: rp.origin,
    expectedRPID: rp.rpID,
    requireUserVerification: true,
  });
  if (!verification.verified || !verification.registrationInfo) return json({ error: 'The passkey could not be verified.' }, 400);

  const { credential, credentialDeviceType, credentialBackedUp } = verification.registrationInfo;
  const usernameKey = challenge.username.toLocaleLowerCase('en-US');
  await transaction(sql, (tx) => [
    tx`INSERT INTO users (id, username, username_key) VALUES (${challenge.proposed_user_id}, ${challenge.username}, ${usernameKey})`,
    tx`INSERT INTO passkeys
      (credential_id, user_id, webauthn_user_id, public_key, counter, device_type, backed_up, transports)
      VALUES (${credential.id}, ${challenge.proposed_user_id}, ${Buffer.from(String(challenge.proposed_user_id)).toString('base64url')},
        ${Buffer.from(credential.publicKey).toString('base64url')}, ${credential.counter},
        ${credentialDeviceType}, ${credentialBackedUp}, ${JSON.stringify(credential.transports || [])}::jsonb)`,
    tx`INSERT INTO game_stats (user_id) VALUES (${challenge.proposed_user_id})`,
    tx`DELETE FROM auth_challenges WHERE id = ${challenge.id}`,
  ]);
  const token = await issueSession(sql, challenge.proposed_user_id);
  return json({ verified: true, token, user: { id: challenge.proposed_user_id, username: challenge.username, gamesPlayed: 0, wins: 0, bestPlace: null } });
}

async function loginOptions(sql, rp) {
  const options = await generateAuthenticationOptions({
    rpID: rp.rpID,
    allowCredentials: [],
    userVerification: 'required',
  });
  const challengeId = randomUUID();
  await sql`INSERT INTO auth_challenges (id, purpose, challenge, expires_at)
    VALUES (${challengeId}, 'authenticate', ${options.challenge}, NOW() + INTERVAL '5 minutes')`;
  return json({ challengeId, options });
}

async function loginVerify(sql, body, rp) {
  const challenges = await sql`SELECT * FROM auth_challenges
    WHERE id = ${body.challengeId} AND purpose = 'authenticate' AND expires_at > NOW() LIMIT 1`;
  if (!challenges[0]) return json({ error: 'That sign-in request expired. Please try again.' }, 400);
  const rows = await sql`
    SELECT p.*, u.username,
      COALESCE(g.games_played, 0)::int AS games_played,
      COALESCE(g.wins, 0)::int AS wins,
      g.best_place
    FROM passkeys p
    JOIN users u ON u.id = p.user_id
    LEFT JOIN game_stats g ON g.user_id = u.id
    WHERE p.credential_id = ${body.credential?.id} LIMIT 1`;
  const passkey = rows[0];
  if (!passkey) return json({ error: 'This passkey is not registered for scummie.' }, 404);

  const verification = await verifyAuthenticationResponse({
    response: body.credential,
    expectedChallenge: challenges[0].challenge,
    expectedOrigin: rp.origin,
    expectedRPID: rp.rpID,
    requireUserVerification: true,
    credential: {
      id: passkey.credential_id,
      publicKey: new Uint8Array(Buffer.from(passkey.public_key, 'base64url')),
      counter: Number(passkey.counter),
      transports: passkey.transports || [],
    },
  });
  if (!verification.verified) return json({ error: 'The passkey could not be verified.' }, 400);
  await transaction(sql, (tx) => [
    tx`UPDATE passkeys SET counter = ${verification.authenticationInfo.newCounter} WHERE credential_id = ${passkey.credential_id}`,
    tx`DELETE FROM auth_challenges WHERE id = ${challenges[0].id}`,
  ]);
  const token = await issueSession(sql, passkey.user_id);
  return json({ verified: true, token, user: publicUser({ id: passkey.user_id, ...passkey }) });
}

export default async (request) => {
  if (request.method !== 'POST') return json({ error: 'Method not allowed' }, 405);
  try {
    const sql = db();
    const body = await request.json();
    const rp = relyingParty(request);
    await sql`DELETE FROM auth_challenges WHERE expires_at < NOW()`;
    await sql`DELETE FROM sessions WHERE expires_at < NOW()`;

    if (body.action === 'register-options') return registerOptions(sql, body, rp);
    if (body.action === 'register-verify') return registerVerify(sql, body, rp);
    if (body.action === 'login-options') return loginOptions(sql, rp);
    if (body.action === 'login-verify') return loginVerify(sql, body, rp);

    const user = await authenticatedUser(sql, request);
    if (!user) return json({ error: 'Please sign in again.' }, 401);
    if (body.action === 'me') return json({ user: publicUser(user) });
    if (body.action === 'leaderboard') {
      const players = await sql`
        SELECT u.id, u.username,
          COALESCE(g.games_played, 0)::int AS games_played,
          COALESCE(g.wins, 0)::int AS wins
        FROM users u
        JOIN game_stats g ON g.user_id = u.id
        WHERE g.games_played > 0
        ORDER BY g.wins DESC,
          (g.wins::decimal / NULLIF(g.games_played, 0)) DESC,
          g.games_played DESC,
          LOWER(u.username) ASC
        LIMIT 100`;
      return json({ players: players.map((player) => ({
        id: player.id,
        username: player.username,
        wins: player.wins,
        losses: player.games_played - player.wins,
        winRate: Math.round((player.wins / player.games_played) * 100),
      })) });
    }
    if (body.action === 'save-game') {
      const record = validateGameRecord(body);
      const databaseGameId = randomUUID();
      await transaction(sql, (tx) => [
        tx`WITH inserted_game AS (
          INSERT INTO games
            (id, client_game_id, user_id, player_count, finish_place, finish_order, rules, started_at, completed_at)
          VALUES
            (${databaseGameId}, ${body.gameId}, ${user.id}, ${record.playerCount}, ${record.finishPlace},
             ${JSON.stringify(record.finishOrder)}::jsonb, ${JSON.stringify(body.rules || {})}::jsonb,
             ${record.startedAt}, ${record.completedAt})
          ON CONFLICT (client_game_id) DO NOTHING
          RETURNING user_id, finish_place
        )
        INSERT INTO game_stats (user_id, games_played, wins, best_place)
        SELECT user_id, 1, CASE WHEN finish_place = 1 THEN 1 ELSE 0 END, finish_place
        FROM inserted_game
        ON CONFLICT (user_id) DO UPDATE SET
          games_played = game_stats.games_played + 1,
          wins = game_stats.wins + EXCLUDED.wins,
          best_place = LEAST(COALESCE(game_stats.best_place, EXCLUDED.best_place), EXCLUDED.best_place),
          updated_at = NOW()`,
        ...record.players.map((player) => tx`INSERT INTO game_players
          (game_id, seat, display_name, player_type, starting_hand, finish_place)
          SELECT id, ${player.seat}, ${player.name}, ${player.type}, ${JSON.stringify(player.startingHand)}::jsonb, ${player.finishPlace}
          FROM games WHERE client_game_id = ${body.gameId} AND user_id = ${user.id}
          ON CONFLICT (game_id, seat) DO NOTHING`),
        ...record.actions.map((action) => tx`INSERT INTO game_actions
          (game_id, sequence, trick_number, player_seat, action_type, cards, hand_size_after,
           pile_count_before, pile_rank_before, occurred_at)
          SELECT id, ${action.sequence}, ${action.trickNumber}, ${action.playerSeat}, ${action.action},
            ${JSON.stringify(action.cards)}::jsonb, ${action.handSizeAfter}, ${action.pileCountBefore},
            ${action.pileRankBefore}, ${action.occurredAt}
          FROM games WHERE client_game_id = ${body.gameId} AND user_id = ${user.id}
          ON CONFLICT (game_id, sequence) DO NOTHING`),
      ]);
      return json({ saved: true, gameId: body.gameId });
    }
    return json({ error: 'Unknown action.' }, 400);
  } catch (error) {
    console.error(error);
    if (String(error?.message || '').startsWith('Invalid ')) return json({ error: error.message }, 400);
    if (error?.code === '23505') return json({ error: 'That player name or passkey is already registered.' }, 409);
    const safeMessage = error?.message === 'DATABASE_URL is not configured'
      ? 'Account service is not configured yet.'
      : 'The account service could not complete that request.';
    return json({ error: safeMessage }, 500);
  }
};
