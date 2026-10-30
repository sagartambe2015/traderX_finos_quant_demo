# Phase 1 — Strategies, accounts and returns (2026-10-28)

Builds on [Phase 0](PHASE-0.md). User guide: README §6 "Portfolio tab". Architecture: CLAUDE.md (`server/portfolio.ts`).

## Decisions (confirmed with the user)

| Question | Decision |
| --- | --- |
| What is an "account"? | A virtual capital bucket within the single Kite login (name + allocated capital). Not separate Zerodha client IDs. |
| How do positions map to strategies? | Glob rules per strategy (`MCX:CRUDEOILM*`; the first matching strategy wins) plus a manual override per trading symbol from the positions table. |
| How are weekly/monthly returns computed? | A daily P&L journal recorded by the server, plus manual entry and CSV backfill. Return % = P&L ÷ the account's capital (simple, not compounded). |

## What was built

- `shared/protocol.ts`: `Account`, `Strategy`, `JournalEntry`, `Portfolio`. `PositionRow` gains `m2m`, `strategyId`, `ruleStrategyId` and `manual`. `Positions` gains `tradingDate`. New `portfolio` WS message; the snapshot includes the portfolio.
- `server/portfolio.ts`: the store (`portfolio.json`, gitignored), validation, rule matching, and journal upserts.
- `server/feed.ts`: tags positions and records the day's per-strategy `m2m` on each poll. A day counts as a trading day only if a followed instrument traded today (IST).
- `server/index.ts`: `POST/DELETE /api/accounts`, `POST/DELETE /api/strategies`, `PUT /api/assignments`, `POST /api/journal`, `DELETE /api/journal/:date/:strategyId`.
- `server/ist.ts`: shared IST date helper (`instruments.ts` uses it too).
- Web:
  - **Live / Portfolio** tabs, remembered in localStorage.
  - Positions are grouped by strategy with subtotals and a Strategy dropdown.
  - Portfolio tab: Returns (today / week / month per account and strategy), History (8 weeks / 6 months), Accounts & strategies management, P&L journal (manual entry, CSV import, recent entries).
  - `web/src/returns.ts` holds the aggregation.

## Verified

- `npm run typecheck` passes.
- API against the running server:
  - Account and strategy create, with duplicate-name and capital validation.
  - An account that still has strategies can't be deleted.
  - Journal import by strategy name (case-insensitive). A CSV with one bad row imports nothing.
  - Override set/clear, with symbol validation.
- Rule matching (`MCX:CRUDEOILM*` matches NOV and DEC contracts, not GOLDM); an `""` override forces unassigned.
- Returns math: live m2m = `m2m + qty*(ltp-last_price)*multiplier`; Monday week buckets; month buckets; unassigned positions excluded.
- `tradingDate` = 2026-10-28 while MCX was trading.
- Test data was removed afterwards; `portfolio.json` starts empty.

## Not yet verified

- Auto journal entries with real positions (the account had none), including closing figures after the last poll of the day.
- Visual check of the Portfolio tab and grouped positions in a browser.

## Known limitations / backlog

1. A day is only journaled if the server runs that day. Days it misses must be backfilled manually. A later option: Kite Console P&L report import (Console has no API).
2. Capital is a single current value. Changing it rewrites past return %. Could add dated capital changes (deposits/withdrawals) for time-weighted returns.
3. Overrides are keyed by trading symbol, so one symbol can't be split across two strategies (e.g. the same NIFTY future in two strategies). Splitting needs order-level tagging (Kite order `tag`) and a tradebook.
4. CNC/delivery positions turn into holdings the next day and drop out of tracking.
5. Deleting a strategy deletes its journal history (the UI confirms first).
6. Carried over from Phase 0: remove `fallbackRequestToken` from `test.ts`; intraday rollover of futures aliases; no tests.
