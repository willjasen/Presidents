CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY,
  username TEXT NOT NULL,
  username_key TEXT NOT NULL UNIQUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS passkeys (
  credential_id TEXT PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  webauthn_user_id TEXT NOT NULL,
  public_key TEXT NOT NULL,
  counter BIGINT NOT NULL DEFAULT 0,
  device_type TEXT NOT NULL,
  backed_up BOOLEAN NOT NULL DEFAULT FALSE,
  transports JSONB NOT NULL DEFAULT '[]'::jsonb,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS passkeys_user_id_idx ON passkeys(user_id);

CREATE TABLE IF NOT EXISTS auth_challenges (
  id UUID PRIMARY KEY,
  purpose TEXT NOT NULL CHECK (purpose IN ('register', 'authenticate')),
  challenge TEXT NOT NULL,
  proposed_user_id UUID,
  username TEXT,
  expires_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS sessions (
  token_hash TEXT PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  expires_at TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS sessions_user_id_idx ON sessions(user_id);

CREATE TABLE IF NOT EXISTS game_stats (
  user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
  games_played INTEGER NOT NULL DEFAULT 0,
  wins INTEGER NOT NULL DEFAULT 0,
  best_place INTEGER,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS games (
  id UUID PRIMARY KEY,
  client_game_id UUID NOT NULL UNIQUE,
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  player_count INTEGER NOT NULL CHECK (player_count BETWEEN 2 AND 7),
  status TEXT NOT NULL DEFAULT 'completed' CHECK (status IN ('completed')),
  finish_place INTEGER NOT NULL,
  finish_order JSONB NOT NULL,
  rules JSONB NOT NULL,
  started_at TIMESTAMPTZ NOT NULL,
  completed_at TIMESTAMPTZ NOT NULL,
  recorded_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS games_user_id_idx ON games(user_id);
CREATE INDEX IF NOT EXISTS games_completed_at_idx ON games(completed_at DESC);

CREATE TABLE IF NOT EXISTS game_players (
  id BIGSERIAL PRIMARY KEY,
  game_id UUID NOT NULL REFERENCES games(id) ON DELETE CASCADE,
  seat INTEGER NOT NULL,
  display_name TEXT NOT NULL,
  player_type TEXT NOT NULL CHECK (player_type IN ('human', 'computer')),
  starting_hand JSONB NOT NULL,
  finish_place INTEGER NOT NULL,
  UNIQUE (game_id, seat)
);

CREATE INDEX IF NOT EXISTS game_players_game_id_idx ON game_players(game_id);

CREATE TABLE IF NOT EXISTS game_actions (
  id BIGSERIAL PRIMARY KEY,
  game_id UUID NOT NULL REFERENCES games(id) ON DELETE CASCADE,
  sequence INTEGER NOT NULL,
  trick_number INTEGER NOT NULL,
  player_seat INTEGER NOT NULL,
  action_type TEXT NOT NULL CHECK (action_type IN ('play', 'pass')),
  cards JSONB NOT NULL DEFAULT '[]'::jsonb,
  hand_size_after INTEGER NOT NULL,
  pile_count_before INTEGER NOT NULL,
  pile_rank_before INTEGER,
  occurred_at TIMESTAMPTZ NOT NULL,
  UNIQUE (game_id, sequence)
);

CREATE INDEX IF NOT EXISTS game_actions_game_id_sequence_idx ON game_actions(game_id, sequence);
CREATE INDEX IF NOT EXISTS game_actions_player_action_idx ON game_actions(player_seat, action_type);

CREATE OR REPLACE VIEW player_strategy_summary AS
SELECT
  u.id AS user_id,
  u.username,
  COUNT(DISTINCT g.id)::int AS games_recorded,
  COUNT(DISTINCT g.id) FILTER (WHERE g.finish_place = 1)::int AS wins,
  ROUND(AVG(g.finish_place), 2) AS average_finish,
  COUNT(ga.id) FILTER (WHERE ga.action_type = 'play')::int AS plays,
  COUNT(ga.id) FILTER (WHERE ga.action_type = 'pass')::int AS passes,
  ROUND(
    COUNT(ga.id) FILTER (WHERE ga.action_type = 'pass')::numeric
    / NULLIF(COUNT(ga.id), 0),
    3
  ) AS pass_rate,
  ROUND(AVG(jsonb_array_length(ga.cards)) FILTER (WHERE ga.action_type = 'play'), 2) AS average_cards_per_play,
  ROUND(AVG((ga.cards -> 0 ->> 'value')::numeric) FILTER (WHERE ga.action_type = 'play'), 2) AS average_rank_played
FROM users u
JOIN games g ON g.user_id = u.id
LEFT JOIN game_actions ga ON ga.game_id = g.id AND ga.player_seat = 0
GROUP BY u.id, u.username;
