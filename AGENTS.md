# AGENTS.md

When asked to run the app locally, prefer the guest-mode version without a database.

Use:
- `npm run dev`

Do not:
- run `npm run db:setup`
- start Postgres via Docker
- require `DATABASE_URL` for a normal local play session

Guest mode is the default local run for gameplay and matches the static browser app behavior. Passkey/account features require a configured database and are only used when explicitly requested.
