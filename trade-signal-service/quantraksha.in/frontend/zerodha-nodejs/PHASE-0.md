# Phase 0 — Live dashboard baseline (completed 2026-10-28)

Handoff notes for the next iteration. How to run everything is in [README.md](README.md); the architecture is in [CLAUDE.md](CLAUDE.md).

## What was built

| Area | Result |
| --- | --- |
| Credentials | API key/secret moved from code to `.env` (`.env.example` committed). `.env`, `access_token.txt`, `watchlist.json` and `web/dist/` are gitignored in the parent repo. |
| CLI login | `test.ts` (`npm start [-- <request_token>]`) shares `access_token.txt` with the server. |
| Relay server | `server/`: Express + `ws` on 127.0.0.1:4000. Rejects non-localhost origins (HTTP 403, WS 401). It restores the session on boot and runs one KiteTicker (full mode), coalescing ticks into 250ms batches. Positions are polled every 5s. An expired token clears the session and stops the feed. |
| Web UI | `web/`: Vite + React 18. It has the login screen (paste the redirect URL or request_token, or use `/api/callback`), the watchlist (add/remove, LTP flash, OHLC, bid/ask, volume), and positions (Net/Day tabs, live P&L, total in the header). |
| Futures aliases | `EXCHANGE:NAME FUT` (e.g. `MCX:GOLDM FUT`, `NFO:NIFTY FUT`) resolves to the nearest unexpired contract (`server/instruments.ts`). The row shows the contract and its expiry. |
| Docs | README.md (full run guide), CLAUDE.md. |

## Verified

- The session restores from the saved token; the ticker connects; the origin checks work; unknown symbols give a clear error.
- Futures resolution as of 2026-10-28:

  | Alias | Contract | Expiry |
  | --- | --- | --- |
  | NFO:NIFTY FUT | NIFTY26NOVFUT | 23 Nov |
  | MCX:CRUDEOILM FUT | CRUDEOILM26NOVFUT | 19 Nov |
  | MCX:GOLDM FUT | GOLDM26NOVFUT | 5 Nov |
  | MCX:SILVERM FUT | SILVERM26NOVFUT | 30 Nov |

- MCX ticks stream live in the evening session.
- `npm run typecheck` passes.

## Not yet verified

- Live NSE/NFO ticks during market hours.
- Positions table and live P&L against real positions (the account had none during testing).
- A visual check in a browser.

## Known gaps / backlog

1. `test.ts` still contains a hardcoded, expired `fallbackRequestToken`. Remove it.
2. Futures aliases roll only on `feed.start()`. Add an intraday or midnight re-resolve so a long-running server rolls after expiry.
3. ~~Strategy/account segregation of positions, with fund allocation and weekly/monthly returns~~: done in [Phase 1](PHASE-1.md).
4. Option chain view, order placement, charts (ideas, not requested yet).
5. No tests or linter.
