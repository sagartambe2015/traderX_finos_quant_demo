# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Layout

`quantraksha.in/` is one folder of the larger `trading-automation` monorepo. The git root and the shared `.gitignore` are in the parent directory, so run `git` from there or use paths relative to it. It holds four independent projects. There is no shared build:

| Path | What it is | Stack |
| --- | --- | --- |
| `frontend/zerodha-nodejs/` | Kite live dashboard: a Node relay plus a React UI showing watchlist ticks, positions and P&L, with strategy and account returns | TypeScript, Express, `ws`, `kiteconnect` npm, Vite + React 18 |
| `backend/fyers-backend-connect/` | Fyers API experiments: login, option chain, history, WebSocket, IST schedulers and a NIFTY option-selling strategy sketch | Java 21, Maven, `com.tts.in:fyersjavasdk` |
| `backend/kite-rest-api/` | Spring Boot REST API over Kite for the React UI (session, profile, margins, positions, holdings, orders, quotes) on :4100. See its `README.md` | Java 17, Spring Boot 3.5, Maven, `kiteconnect` 4.0.1 from Central |
| `backend/javakiteconnect/` | Vendored copy of Zerodha's official Kite Connect Java client (v4.0.1), with a prebuilt `dist/kiteconnect.jar` and examples in `sample/src/` | Java 8, Maven |

**The dashboard is the active project.** It has its own detailed `frontend/zerodha-nodejs/CLAUDE.md`, which covers commands, architecture, the auth flow and Docker/Dokploy deployment. Read it before working there. `README.md` and `PHASE-*.md` in that folder give the user-facing documentation and the phase plans.

## Dashboard commands (summary)

Run these from `frontend/zerodha-nodejs/`:

```bash
./run_frontend.sh [dev|prod|login]   # checks Node >= 20.6 and .env, installs deps, then runs
npm run dev                 # relay on 127.0.0.1:4000 + Vite on :5173
npm run typecheck           # the only automated check; there are no tests or linter
```

## Fyers backend (`backend/fyers-backend-connect/`)

- Build with `mvn compile`. JavaFX UI: `mvn javafx:run` (main class `com.example.TradingJavaFXApp`, which is currently commented out). Other classes, such as `FyersTraderApi`, `TradingSwingApp` and `ISTIntervalScheduler`, each have their own `main` and are run from the IDE.
- The build needs **JDK 21**. With an older JDK it fails with `invalid target release: 21`.
- `fyersjavasdk` is not on Maven Central. The pom points to a `${basedir}/repo` file repository that doesn't exist, so the SDK has to be installed in `~/.m2` under `com/tts/in`.
- `FyersTraderApi.init()` has the client ID, secret and app hash hardcoded. It reads and writes the token file at an absolute path under `trading-automation/backend-api/`, and uses an interactive login: it prints the auth URL, reads the auth code from stdin, and saves `fyers_access_token.txt` / `fyers_refresh_token.txt`. Some methods are half-finished, so don't assume the module compiles cleanly.
- The `junit` 3.8.1 test (`AppTest`) is a placeholder.

## Kite Java client (`backend/javakiteconnect/`)

This is upstream library code. Treat it as read-only unless a change is deliberate. The pom has no `sourceDirectory` override, so `mvn` does not compile `kiteconnect/src/`. Use `dist/kiteconnect.jar` and the jars in `kiteconnect/libs/` instead (see `kiteconnect.iml`). `sample/src/` shows usage: `Examples.java`, the ticker callbacks and `KiteInstrumentClient`.

## Secrets and runtime state

Token files (`access_token.txt`, `fyers_access_token.txt`), `.env`, `watchlist.json` and `portfolio.json` live inside the project folders but are runtime state. Never commit them. Check the parent `.gitignore` before adding new state files.

## Kite REST API (`backend/kite-rest-api/`)

- `mvn spring-boot:run` with `KITE_API_KEY` / `KITE_API_SECRET` set. `mvn package` builds `target/kite-rest-api-0.1.0.jar`. There are no tests yet.
- Every Kite call goes through `KiteSessionService.call(...)`. It converts the SDK's `KiteException`, which extends `Throwable` and so can't propagate through Spring MVC, into `KiteApiException`, and clears the session on `TokenException`.
- Gson is the HTTP JSON mapper (`spring.mvc.converters.preferred-json-mapper`), so SDK models serialize with Kite's snake_case names. Don't switch it to Jackson without adding naming config.
- Pointing `KITE_TOKEN_FILE` at `frontend/zerodha-nodejs/access_token.txt` shares one daily login with the Node relay.
