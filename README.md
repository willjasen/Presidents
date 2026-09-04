# Presidents

## Web app

The browser version lives in `web/` and is configured for Netlify. It includes a
complete single-player match against one to six computer opponents and optional
passkey-only player accounts backed by Neon Postgres.

To prepare accounts, create a Neon database, copy `.env.example` to `.env`, set
`DATABASE_URL`, and run `npm run db:setup`. Then deploy the repository; Netlify
uses `netlify.toml` to build the app and its account function.

### Netlify production setup

1. Create the Netlify project from this repository. Netlify will run
   `npm run build`, publish `dist/`, and deploy `netlify/functions/`.
2. In the Netlify project's environment variables, add `DATABASE_URL` with the
   Neon pooled connection string. Mark it as a secret and make it available to
   Functions. Do not commit the real value.
3. Run `npm run db:setup` once with that same `DATABASE_URL` to create the
   account, passkey, game-history, and leaderboard tables.
4. Use `scummie.netlify.app` as the production site domain in Netlify. Wait
   for HTTPS to be active before testing account creation or sign-in.

The production WebAuthn settings are already scoped in `netlify.toml` to
`scummie.netlify.app` and `https://scummie.netlify.app`. Deploy
previews intentionally derive their own relying-party settings from the request
hostname. If the production hostname changes later, update both values together;
existing passkeys are bound to the hostname on which they were created.

For local work, install the JavaScript dependencies and run `npm run dev`. A
plain static server is enough to play as a guest, but passkey accounts require
the local deployment server and a configured Postgres database.

### Local Postgres with OrbStack

For local passkey testing, `compose.yaml` runs a development Postgres database
through OrbStack on `127.0.0.1:54329`. Start it with `docker compose up -d`, then
run `npm run db:setup` once to apply the account and leaderboard schema. The
ignored `.env` file contains the matching local database and WebAuthn settings.
Run `npm run dev` and open `http://localhost:8888` for a passkey-enabled local
deployment.

Completed signed-in matches are recorded in `games`, `game_players`, and
`game_actions`. The `player_strategy_summary` database view provides a quick
starting point for periodic analysis of finish position, pass rate, average
cards per play, and average rank played. Starting hands and the full ordered
action history remain available for deeper analysis.

Presidents is a restored Java desktop implementation of the climbing card game
also commonly known as Asshole. The original 2009 project contains a Swing
client, a socket server, account registration, rooms, and chat. This restoration
provides a Java 21 build, an embedded account database, and a tested rules engine.

## Current status

The project compiles and its domain tests pass. These parts are working or have
been restored:

- a standard 52-card deck with 3 low and 2 high;
- deterministic dealing for two to seven players;
- the holder of the 3 of clubs opens the game;
- same-rank singles, pairs, triples, and four-of-a-kind plays;
- responses with the same or a greater card count and an equal or higher rank;
- passing, trick clearing, turn advancement, and finish order;
- server-authoritative multiplayer turns, plays, passes, and win results;
- room screens with selectable cards, Start Game, Play, and Pass controls;
- local account registration and login without a separate MySQL installation;
- lobby, room, and chat scaffolding from the original network application;
- a standalone Swing window in place of the obsolete browser applet.

Desktop matches require two to seven clients. Create or enter the same room from
each client, then choose **Start Game**. The holder of the 3 of clubs takes the
first turn. Click cards in your hand to select them, then use **Play Selected
Cards**, or use **Pass** when responding to another play.

## Requirements

- JDK 21
- Maven 3.9+

## macOS setup

Install the required tools with [Homebrew](https://brew.sh):

```sh
brew install openjdk@21 maven
```

The included `.command` launchers find the Homebrew JDK automatically, on both
Apple Silicon and Intel Macs. In Finder, double-click `run-server.command`, then
double-click `run-client.command`. Open another client launcher for each local
player.

If macOS refuses to open a launcher the first time, Control-click it, choose
**Open**, and confirm. The files may also be launched from Terminal:

```sh
./run-server.command
./run-client.command
```

## Build and test

```sh
mvn test
```

## Run the desktop applications

Start the server first:

```sh
mvn -DskipTests compile exec:java -Dexec.mainClass=PresidentsServer.Presidents
```

Then start each client in a separate terminal:

```sh
mvn -DskipTests compile exec:java -Dexec.mainClass=PresidentsClient.Client
```

The account database is empty on first launch. In the client, choose **Create
Account**, complete registration, and then log in with the account you created.
The server and client do not ship with default credentials.

Accounts are stored under `data/`, which is intentionally ignored by Git.
The current protocol is intended for local/LAN development and should not be
exposed directly to the public internet.

## Architecture

- `PresidentsPlayer` contains cards, hands, players, and the restored game rules.
- `PresidentsData` contains serializable messages exchanged by client and server.
- `PresidentsServer` contains accounts, rooms, chat, and socket handling.
- `PresidentsClient` contains the Swing desktop interface and card artwork.
- `src/test/java` contains repeatable domain tests.

## Remaining roadmap

1. Add a clean leave-room flow and decide how an in-progress game handles a
   disconnected player.
2. Consolidate the two legacy sockets into one framed connection, add clean
   disconnect/reconnect behavior, and test a seven-client match end to end.

The implemented house rules follow the original `Player` comments: equal ranks
are allowed, four-of-a-kind does not automatically clear the trick, and there
are no revolutions or jokers. Players are ranked in the order they empty their
hands; when only one player remains, that player receives last place and the
game ends.
