# Copilot instructions for Presidents

## Local run guidance

When a user asks to run the app locally, prefer the guest-mode static local deployment unless they explicitly request database-backed sign-in or account features.

- Use `npm run dev`.
- Do not require or initialize a Postgres database for the default local run.
- Do not run `npm run db:setup` or `docker compose up` unless the user explicitly asks for passkey/account features or local database-backed auth.
- If `DATABASE_URL` is absent or the database is unavailable, continue in guest mode and explain that passkey accounts remain unavailable until a configured database is provided.
- The local guest-mode app runs at `http://localhost:8888` and supports playing without a database.

## Project context

- This repo contains a browser game in `web/`, plus Java desktop/server code under `PresidentsClient`, `PresidentsServer`, and `PresidentsPlayer`.
- Browser app setup is handled by the Node build and local dev server scripts.
- Passkey account functionality depends on Neon/Postgres configuration and is optional for local gameplay.
