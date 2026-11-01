# quantraksha.in Kanban

Scanned 2026-11-01 from `TODO` comments in the code and the backlog sections of `PHASE-0.md` and `PHASE-1.md`. Each open item was checked against the code where the code can show it. Paths under `dashboard/` mean `frontend/zerodha-nodejs/`, and paths under `api/` mean `backend/kite-rest-api/`. Move a card by moving its line to another column.

## Backlog

### kite-rest-api

- [ ] **Map `ReferenceCandleData` to `TradeCaptureEntity` (one-to-many).** Source: `TODO` at `api/src/main/java/com/quantraksha/kite/persist/entity/TradeCaptureEntity.java:105`. The class has no JPA annotations, `@Id`, repository or table yet, so it needs those first. The `TODO` says "if needed in future", so decide whether it's needed.
- [ ] **Map `StudyResearchBackEntity` to `StrategyEntity` (one-to-many).** Source: two `TODO`s at `api/src/main/java/com/quantraksha/kite/persist/entity/StrategyEntity.java:34` and `:37`. The class has no JPA annotations, `@Id`, repository or table yet, so it needs those first. The `TODO` at `:34` says "if needed in future", so decide whether it's needed.

### Dashboard

- [ ] **Remove the hardcoded `fallbackRequestToken`.** Source: `PHASE-0.md`, `PHASE-1.md`. Still at `dashboard/test.ts:14`.
- [ ] **Re-resolve futures aliases during the day or at midnight.** Source: `PHASE-0.md`, `PHASE-1.md`. A long-running server keeps an expired contract because aliases only resolve in `dashboard/server/feed.ts` on start or when added.
- [ ] **Add tests and a linter.** Source: `PHASE-0.md`, `PHASE-1.md`. There are no test files, ESLint config, or `test` or `lint` scripts. `npm run typecheck` is the only check.
- [ ] **Handle days the server didn't run.** Source: `PHASE-1.md`. These days are missing from the journal and have to be backfilled by hand. Option: import the Kite Console P&L report (Console has no API).
- [ ] **Add dated capital changes.** Source: `PHASE-1.md`. Capital is a single current value, so changing it rewrites past return %. Recording deposits and withdrawals would allow time-weighted returns.
- [ ] **Allow one symbol to be split across strategies.** Source: `PHASE-1.md`. Overrides are keyed by trading symbol. Splitting needs a Kite order `tag` and a tradebook.
- [ ] **Keep tracking CNC/delivery positions.** Source: `PHASE-1.md`. They become holdings the next day and drop out, and the dashboard server and UI have no holdings handling.
- [ ] **Keep journal history when a strategy is deleted.** Source: `PHASE-1.md`. `deleteStrategy` in `dashboard/server/portfolio.ts` removes it, and the UI only confirms first.

## To Verify

These need live market hours, real positions or a browser, so they can't be confirmed from the code.

- [ ] **Live NSE/NFO ticks during market hours.** Source: `PHASE-0.md`.
- [ ] **Positions table and live P&L against real positions.** Source: `PHASE-0.md`.
- [ ] **Live dashboard visual check in a browser.** Source: `PHASE-0.md`.
- [ ] **Auto journal entries with real positions, including closing figures after the day's last poll.** Source: `PHASE-1.md`.
- [ ] **Portfolio tab and grouped positions visual check in a browser.** Source: `PHASE-1.md`.

## Ideas

Listed as "ideas, not requested yet" in `PHASE-0.md`.

- [ ] **Option chain view.**
- [ ] **Order placement.** This would be the first feature to place orders on the real brokerage account. The dashboard and the API are read-only today.
- [ ] **Charts.**

## In Progress

## Done

- [x] **Strategy/account segregation of positions, with fund allocation and weekly/monthly returns.** Source: `PHASE-0.md`, delivered in `PHASE-1.md`.
