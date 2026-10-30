# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Zerodha Kite Connect tooling in TypeScript (`kiteconnect` npm package). It has two parts:

- `test.ts`: a standalone CLI login script that creates or validates a session and prints the profile.
- A live dashboard: a Node relay (`server/`) plus a React UI (`web/`) that shows streaming ticks for a watchlist and positions with live P&L.

This is one sub-project of the larger `trading-automation` monorepo (sibling dirs hold Java/Python broker integrations such as Fyers and Dhan). The git root and `.gitignore` live in the parent directory.

## Commands

```bash
npm install && npm --prefix web install
cp .env.example .env           # then fill in KITE_API_KEY / KITE_API_SECRET

./run.sh [dev|prod|login]      # one-shot: checks Node/.env, installs deps if needed, then runs the matching npm script
npm run dev                    # relay on 127.0.0.1:4000 (tsx watch) + Vite UI on :5173, together
npm run server / npm run web   # either half alone
npm run typecheck              # tsc for server/shared/test.ts, then for web/
npm run build && npm run serve # production: relay also serves web/dist on :4000

npm start                      # CLI login script: reuses a saved access token if valid
npm start -- <request_token>   # CLI: create a new session from a fresh Kite request_token
```

There is no linter or test suite.

## Dashboard architecture

The browser never talks to Kite directly, because the API secret and access token stay on the server.

- `server/kite.ts`: the single `KiteConnect` instance and session lifecycle: restore from `access_token.txt`, `createSession(request_token)`, and expiry detection (`isTokenError`).
- `server/feed.ts`: the `MarketFeed` singleton. It owns one `KiteTicker` WebSocket (full mode), the watchlist (persisted to `watchlist.json` as `EXCHANGE:SYMBOL` strings, resolved to instrument tokens via `getQuote`, which also seeds prices for when the market is closed), and positions polled every 5s. It subscribes to watchlist tokens plus net-position tokens and coalesces ticks per instrument into 250ms batches. When any REST call returns a token error, it clears the session and stops everything.
- `server/instruments.ts`: continuous-futures aliases (`MCX:GOLDM FUT`, `NFO:NIFTY FUT`). It downloads the exchange's instrument dump via `getInstruments`, cached per exchange per IST day, and picks the nearest contract whose expiry is today or later. The watchlist stores the alias, and `WatchItem.contract`/`expiry` carry the resolved contract, so a rollover happens on the next `feed.start()` (server restart or login).
- `server/portfolio.ts`: the `PortfolioStore` singleton, persisted to `portfolio.json`. It holds accounts (named capital buckets within the one Kite login), strategies (each mapped to an account, with glob rules on `EXCHANGE:TRADINGSYMBOL`; the first match wins), manual overrides per trading symbol (`""` forces unassigned), and the daily P&L journal. `feed` tags every `PositionRow` with `strategyId` on each poll and calls `recordDay`, which replaces that day's `auto` journal entries with per-strategy sums of Kite's `m2m`. This happens only when `Positions.tradingDate` is set, i.e. some followed instrument has a last trade time today; Kite keeps showing stale positions on holidays. `manual` entries (UI form/CSV backfill) beat `auto` ones for the same date and strategy. Mutations emit `change`, which re-tags positions and broadcasts a `portfolio` message.
- `server/index.ts`: Express REST (`/api/status`, `/api/session`, `/api/callback`, `/api/watchlist`, plus portfolio CRUD: `/api/accounts`, `/api/strategies`, `/api/assignments`, `/api/journal`) plus a `ws` server at `/ws`. That socket sends a `snapshot` on connect, then relays `MarketFeed` events. By default it binds to 127.0.0.1 and rejects non-localhost `Origin` headers on both HTTP and WebSocket, because otherwise any web page could read positions. For deployment, `HOST=0.0.0.0` is allowed only with `DASHBOARD_PASSWORD` set, which enables HTTP Basic auth on everything except `/healthz` (browsers reuse the credentials for `fetch` and `/ws`), and `PUBLIC_URL`'s origin is also accepted.
- `shared/protocol.ts`: the message/data types used by both sides. The web app imports it with `import type` only.
- `web/`: Vite + React 18. In dev, Vite proxies `/api` and `/ws` to :4000. `useFeed.ts` holds all live state in a reducer and reconnects with backoff. Position P&L is recomputed client-side on every tick with Kite's formula `(sell_value - buy_value) + quantity * ltp * multiplier`, so it moves between the 5s position polls. The Portfolio tab (`returns.ts`) builds daily P&L per strategy from the journal, replaces the current trading day's auto figures with live `m2m` (`m2m + qty * (ltp - last_price) * multiplier`), then sums by ISO week (Monday start) and calendar month. Return % = P&L ÷ the account's capital.

Login from the UI: open the Kite login URL, then paste the redirect URL or `request_token`. Alternatively, set the Kite app's Redirect URL to `http://127.0.0.1:4000/api/callback`.

## Auth flow (shared by test.ts and the server)

Kite Connect uses a two-token login. Request tokens are single-use and expire within minutes. Access tokens last for the trading day and are saved to `access_token.txt`, so later runs (CLI or server) skip the login. On startup the saved token is validated with `getProfile()` and deleted if it is invalid. A `request_token` is exchanged with the API secret via `generateSession()`.

## Notes

- Credentials come from `.env` (loaded by Node's `--env-file`; requires Node ≥ 20.6). Both entry points exit early if they are missing.
- State files (`access_token.txt`, `watchlist.json`, `portfolio.json`) go in `DATA_DIR` (`server/paths.ts`), which defaults to the project root. `test.ts` always uses the project root.
- Docker: `Dockerfile` (multi-stage: builds `web/dist`, runs the server with `tsx`, so `tsx` is a runtime dependency) plus `docker-compose.yml`, intended for Dokploy. The container sets `HOST=0.0.0.0` and `DATA_DIR=/app/data` (a volume), and reads env vars directly rather than from `.env`.
- `.env`, `access_token.txt`, `watchlist.json`, `portfolio.json` and `web/dist/` are gitignored by the parent `.gitignore`. Only `.env.example` is meant to be committed.
