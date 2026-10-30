# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

@AGENTS.md

## Two systems in one repo

- **TraderX** (everything except `trade-signal-service/`): the FINOS demo trading app, covered by `AGENTS.md`.
- **`trade-signal-service/quantraksha.in/`**: a separate Zerodha Kite project (a Spring Boot API and a Node/React dashboard). Nothing outside this folder references it: it isn't in `docker-compose.yml`, `settings.gradle`, the C4 model or the ingress config. Unlike the demo, it connects to a real brokerage account. See `quantraksha.in/CLAUDE.md` and `frontend/zerodha-nodejs/CLAUDE.md` there.

## TraderX commands

Run these from the service directory.

| Service | Run | Test |
| --- | --- | --- |
| `account-service`, `position-service`, `trade-service`, `trade-processor` (Java 21) | `./gradlew bootRun` | `./gradlew build` compiles; there are no test sources yet |
| `database` | `./gradlew build && ./run.sh` | none |
| `reference-data` | `npm ci && npm run start:dev` | `npm test`, `npx jest src/stocks/stocks.service.spec.ts`, `npm run test:e2e`; lint with `npm run lint` (applies `--fix`) |
| `trade-feed` | `npm ci && npm start` | none |
| `people-service` (.NET) | `cd PeopleService.WebApi && dotnet run` | none |
| `web-front-end/angular` | `npm ci && npm start` | `npm run test:ci`, or one spec with `npx ng test --watch=false --browsers=ChromeHeadlessNoSandbox --include='main/app/trade/**/*.spec.ts'`; lint with `npm run lint` |
| `web-front-end/react` | `npm install && npm start` | `npm test` |
| `website` | `npm install && npm start` | none |

- The whole system runs with `docker compose up` from the repo root. The ingress serves it on :8080.
- The root `settings.gradle` includes the Java services and `database`, so `./gradlew account-service:bootRun` also works from the root.
- `scripts/generate-openapi.sh` needs the services already running, or pass `--start-compose`.

## TraderX architecture

- **Trade flow:** `trade-service` validates the account and ticker over REST, then publishes to the `/trades` topic on `trade-feed` and returns. It has no repositories, although `build.gradle` includes JPA. `trade-processor` subscribes to that topic (`TradeFeedHandler`), writes the trade and position, and publishes to `/accounts/$id/trades` and `/accounts/$id/positions` for the GUI. A successful submission therefore only means the trade was accepted. `docs/flows.md` calls the first topic `trades/new`, but the code uses `/trades`.
- **Read and write split:** `position-service` only reads trades and positions, and `trade-processor` only writes them. Both have their own JPA models of the same tables, as does `account-service` for accounts.
- **Schema:** `database/initialSchema.sql` is the only schema source. It runs every time the database starts, against the H2 data directory (`./_data` by default). The H2 server also exposes a Postgres port. Nothing references `initialSchema.postgres.sql`, so `run.sh` doesn't use it.
- **Duplicated messaging code:** `finos.traderx.messaging` (`Publisher`, `Subscriber`, the Socket.IO implementations) is copied into both `trade-service` and `trade-processor`, not shared, and `Envelope` and `SocketIOJSONPublisher` already differ between them. Compare both copies before changing either.
- **`trade-feed`:** one file, `index.js`, a Socket.IO server that uses rooms as topics. It isn't durable and has no authentication. Clients receive `publish` events wrapped in an envelope (`type`, `from`, `topic`, `date`, `payload`).
- **Service addresses:** each service resolves its peers from `*_HOST` or `*_URL` environment variables, defaulting to `localhost` and the standard port (see `application.properties`). Compose sets the `*_HOST` values.
- **Ingress:** `ingress/nginx.traderx.conf.template` routes `/account-service/`, `/trade-service/` and so on. A new service exposed to the browser needs a route here, in `docker-compose.yml` and in `gitops/`.

## kite-rest-api

Located at `trade-signal-service/quantraksha.in/backend/kite-rest-api` (Java 17, Maven, port 4100). Run these from that directory.

```bash
./run_backend.sh [dev|prod|build]         # loads KITE_* from the environment, ./.env, or the dashboard's .env
mvn test                                  # unit tests (*Test): no Spring context, database or network
mvn verify                                # also integration tests (*IT): "it" profile, H2, Kite mocked
mvn test -Dtest=TradeCalculatorsTest      # one unit test
mvn verify -Dit.test=LevelsControllerIT   # one integration test, after the unit tests
```

- **Signal flow:** `TradeCaptureController` calls `TradeCalculatorService`, which gets `Levels` from `LevelCalculatorService` (highest high and lowest low of the last N completed daily candles, excluding today, in IST). It then passes them to the `ITradeCalculator` chosen for the instrument, which returns a `TradeCapture`. Nothing currently places orders, and order endpoints are read-only.
- **Adding an instrument:** add a subclass of `ITradeCalculator`, a constant in `StrategyName`, a branch in `TradeCalculatorService.getTradeCalculator` and a `getTradeCaptureFor...` method. Then expose it in `TradeCaptureController`, which currently routes only the crude oil and Nifty calculators (`/api/calculateTrade/mcx/{token}`, `/api/calculateTrade/nse/{token}`).
- **`ITradeCalculator` is an abstract class** despite its name. The name is why Failsafe is restricted to `**/*IT.java`: its default `IT*` pattern would pick up `ITradeCalculatorTest`.
- **Schema:** `ddl-auto` is `none`, so Hibernate doesn't create or check tables outside the `it` profile. `database/schema.sql` defines `TradeSignals`, but `TradeCaptureEntity` maps to `trade_capture`, so integration tests can pass against H2 while the schema file doesn't match.
- **Git:** this directory's `.gitignore` is an allowlist (`src/`, `pom.xml`, `database/`, `run_backend.sh`). Any other new top-level path is ignored unless added there.

## Corrections to the nested CLAUDE.md files

Those files were written for a different repo layout. In this repo:

- The git root isn't a `trading-automation` monorepo, and `backend/fyers-backend-connect` and `backend/javakiteconnect` don't exist.
- `kite-rest-api` does have tests.
- The root `.gitignore` covers `.env` only. It doesn't ignore `access_token.txt`, `watchlist.json`, `portfolio.json` or `web/dist/`, and the first three are currently tracked.
