# Kite Dashboard (Zerodha)

A local web dashboard for Zerodha Kite Connect. It shows **live market ticks** for a watchlist and your **positions with live P&L**.

It has two parts:

- **Relay server** (`server/`): Node + Express + WebSocket. It holds your Kite login, connects to Kite's live price feed, polls positions, and streams updates to the browser.
- **Web UI** (`web/`): React + Vite. The watchlist, the positions table and the login screen.

The browser never talks to Kite directly. Your API secret and access token stay on the server, which only listens on `127.0.0.1`.

---

## 1. Prerequisites

| Requirement | Notes |
| --- | --- |
| **Node.js 20.6 or newer** | Needed for `--env-file`. Check with `node --version`. |
| **npm** | Comes with Node. |
| **Zerodha Kite Connect app** | Create one at <https://developers.kite.trade/apps>. It gives you an **API key** and **API secret**. A paid Kite Connect subscription is needed for live market data. |

### Kite Connect app settings

In your app on the Kite developer console, set the **Redirect URL** to:

```
http://127.0.0.1:4000/api/callback
```

With this, logging in from the dashboard is one click. If you keep a different redirect URL, you can still log in by pasting the redirect URL into the UI (see [step 5](#5-log-in-to-kite)).

---

## 2. Install dependencies

From the `frontend/zerodha-nodejs/` folder:

```bash
npm install                 # server dependencies
npm --prefix web install    # web UI dependencies
```

---

## 3. Configure your API credentials

Copy the template and fill in the values from your Kite Connect app:

```bash
cp .env.example .env
```

`.env`:

```
KITE_API_KEY=your_api_key
KITE_API_SECRET=your_api_secret
```

`.env` is gitignored. Never commit it.

---

## 4. Start the application

### Development (recommended)

```bash
npm run dev
```

This starts both parts together:

| Part | URL | Notes |
| --- | --- | --- |
| Relay server | `http://127.0.0.1:4000` | Restarts automatically when server code changes |
| Web UI | **http://localhost:5173** | Hot reload; proxies `/api` and `/ws` to the server |

Open **http://localhost:5173** in your browser.

To run only one part: `npm run server` or `npm run web`.

### Production-style (single server)

```bash
npm run build     # builds the UI into web/dist
npm run serve     # server on :4000 also serves the built UI
```

Then open **http://127.0.0.1:4000**.

Stop either mode with **Ctrl+C**.

---

## 5. Log in to Kite

Kite access tokens are valid for **one trading day**. You will need to log in once each day.

**From the dashboard (easiest):**

1. Open the dashboard. If there is no valid session, it shows the **Log in to Kite** screen.
2. Click **Open the Kite login page** and sign in with your Zerodha ID and TOTP.
3. Then either:
   - If your Redirect URL is `http://127.0.0.1:4000/api/callback`, you are sent back to the dashboard and logged in automatically.
   - Otherwise, copy the full URL you were redirected to (or just the `request_token` value in it), paste it into the box and click **Log in**.

**From the command line (alternative):**

```bash
npm start                      # checks the saved token; prints a login link if it has expired
npm start -- <request_token>   # creates a session from a fresh request_token
```

Either way, the access token is saved to `access_token.txt` (gitignored). The CLI and the server share it, so after logging in once, restarting the server needs no new login that day.

> A `request_token` is single-use and expires within a few minutes, so use it right away.

---

## 6. Using the dashboard

**Header:** connection status (`Live`, `Ticker connecting`, `Not logged in`, `Server offline`), your user name and total net P&L.

**Watchlist**
- Add instruments as `EXCHANGE:SYMBOL`, e.g. `NSE:SBIN`, `NSE:NIFTY 50`, `BSE:SENSEX`, or a specific contract like `MCX:GOLDM26NOVFUT`.
- **Continuous futures:** add `EXCHANGE:NAME FUT` to track the nearest unexpired contract. Examples: `NFO:NIFTY FUT`, `NFO:BANKNIFTY FUT`, `MCX:CRUDEOILM FUT`, `MCX:GOLDM FUT`, `MCX:SILVERM FUT`. The row shows the contract currently tracked and its expiry, e.g. `CRUDEOILM26NOVFUT · exp 19 Nov`. After expiry it switches to the next contract when the server next starts or you next log in (Kite sessions are daily anyway).
- Shows LTP (flashes green/red on each change), change, change %, open/high/low, best bid/ask and volume.
- Remove an instrument with **×**.
- The list is saved to `watchlist.json`. The defaults are NIFTY 50, NIFTY futures, NIFTY BANK, CRUDEOILM/GOLDM/SILVERM futures, RELIANCE, INFY and HDFCBANK.
- MCX commodities trade until 23:30 IST, so they keep ticking after the equity market closes.

**Positions**
- **Net** / **Day** tabs, with product, quantity, average price, LTP, P&L and change %.
- Positions refresh from Kite every 5 seconds. P&L is recalculated on every tick between refreshes.
- Closed positions (quantity 0) are dimmed and listed after open ones.

- When strategies exist, positions are grouped by strategy with a subtotal each, and a **Strategy** column lets you reassign any position (see below).

Outside market hours no ticks arrive. The watchlist shows the last prices fetched from Kite on startup.

### Portfolio tab: strategies, accounts and returns

Use this to split one Kite login into capital buckets and track each one's return.

1. **Add accounts.** An account is a name plus its allocated capital, e.g. `Commodity – 5,00,000`. Returns are calculated against this capital.
2. **Add strategies** under an account, with optional **rules** that pick up positions automatically. Rules match `EXCHANGE:TRADINGSYMBOL` with `*` as a wildcard, separated by commas:
   - `MCX:CRUDEOILM*`: all Crude Oil Mini contracts
   - `NFO:NIFTY*PE, NFO:NIFTY*CE`: NIFTY options (note `NIFTY*` also matches `NIFTYNXT50`)
   - `NFO:BANKNIFTY*FUT`: Bank Nifty futures

   If several strategies match a position, the first one in the list wins.
3. **Override** any position from the **Strategy** dropdown on the Live tab: `Auto · <rule result>`, a specific strategy, or `Unassigned`. Overrides are remembered per trading symbol.

The **Returns** table shows each account and its strategies for **today** (live), **this week** (Monday onwards) and **this month**, as P&L and % of the account's capital. **History** shows the last 8 weeks or 6 months.

**Where the numbers come from.** Kite only reports today's positions, so the server keeps a **P&L journal**:
- While the server runs on a trading day, it records each strategy's day P&L (Kite's `m2m`) on every position refresh. The last refresh of the day is the closing figure. Leave the server running until you close your trades, or start it once later that day.
- Positions with no strategy are not counted towards any account.
- **Backfill** past days in the **P&L journal** section: one day at a time, or paste CSV rows of `date,strategy,pnl` (e.g. `2026-10-20,Crude momentum,4500`). You can take the daily figures from Zerodha Console. A manual entry replaces the recorded figure for that strategy and day.
- Changing an account's capital recalculates all past returns with the new capital. Equity delivery (CNC) positions are tracked only on the day they are bought; afterwards they become holdings, which this dashboard doesn't track.

Everything is stored in `portfolio.json` (gitignored). Back it up if the history matters to you.

---

## 7. All commands

| Command | What it does |
| --- | --- |
| `npm run dev` | Relay server + web UI in development mode |
| `npm run server` | Relay server only (watch mode) |
| `npm run web` | Web UI only (Vite dev server) |
| `npm run build` | Build the web UI into `web/dist` |
| `npm run serve` | Run the server, which also serves `web/dist` |
| `npm run typecheck` | Type-check server and web code |
| `npm start [-- <request_token>]` | CLI login / session check |

Optional environment variables (set them in `.env`):

| Variable | Default | Purpose |
| --- | --- | --- |
| `PORT` | `4000` | Relay server port. If you change it, also update the proxy in `web/vite.config.ts`. |
| `WEB_URL` | `http://localhost:5173` | Where `/api/callback` redirects after login. Set it to `http://127.0.0.1:4000` when using `npm run serve`. |

---

## 8. Troubleshooting

| Symptom | Fix |
| --- | --- |
| `Missing KITE_API_KEY or KITE_API_SECRET` | Create `.env` as described in [step 3](#3-configure-your-api-credentials). |
| UI says **Server offline** | The relay server isn't running. Start it with `npm run dev` or `npm run server`. |
| UI shows the login screen every morning | This is expected. Kite tokens expire daily, so log in again. |
| Login fails with "Token is invalid or has expired" | The `request_token` was already used or is too old. Get a new one from the login page. |
| `EADDRINUSE` on port 4000 or 5173 | Another instance is still running. Stop it, or find it with `lsof -i :4000`. |
| Prices don't move | The market is closed, or the instrument isn't trading. Check that the header shows **Live**. |
| `Unknown instrument` when adding | Use the exact Kite trading symbol with its exchange prefix, e.g. `NSE:HDFCBANK`. For futures aliases, use the underlying's name as Kite lists it: `MCX:CRUDEOILM FUT`, not `MCX:CRUDE FUT`. |

---

## 9. Deploy with Docker (Dokploy on a Hostinger VPS)

The `Dockerfile` builds the UI and runs the relay on port 4000, serving everything from one origin. When the server is reachable from the network, it requires a password: it refuses to start on `0.0.0.0` without `DASHBOARD_PASSWORD`, and then asks for HTTP Basic auth on every page, API call and WebSocket.

**Try it locally first:**

```bash
KITE_API_KEY=... KITE_API_SECRET=... DASHBOARD_PASSWORD=... docker compose up --build
# open http://localhost:4000 and sign in as admin / your password
```

**On Dokploy:**

1. Point a DNS `A` record (e.g. `kite.example.com`) at the VPS IP.
2. Create a project, then add an **Application** from your Git repo. Set **Build Path** to `quantraksha.in/frontend/zerodha-nodejs` and **Build Type** to `Dockerfile`. (A **Compose** service using `docker-compose.yml` also works. In that case, delete its `ports:` block.)
3. Under **Environment**, set:
   ```
   KITE_API_KEY=...
   KITE_API_SECRET=...
   DASHBOARD_USER=admin
   DASHBOARD_PASSWORD=<long random string>
   PUBLIC_URL=https://kite.example.com
   ```
4. Under **Advanced → Volumes**, add a volume mounted at `/app/data`. It keeps the access token, watchlist and portfolio journal across redeploys.
5. Under **Domains**, add `kite.example.com` with container port `4000` and HTTPS (Let's Encrypt) turned on. Use HTTPS, because Basic auth sends the password with every request.
6. Deploy. Then, in the Kite developer console, set your app's **Redirect URL** to `https://kite.example.com/api/callback`. That way, logging in from the UI lands back on the dashboard.

Kite access tokens expire every day, so log in again from the UI each morning, just as you would locally.

---

## Project structure

```
zerodha-nodejs/
├── server/
│   ├── index.ts      # Express REST API + /ws WebSocket relay
│   ├── feed.ts       # Kite ticker, watchlist, positions polling
│   ├── instruments.ts# Continuous-futures alias → near-month contract
│   ├── portfolio.ts  # Accounts, strategies, rules, P&L journal (portfolio.json)
│   └── kite.ts       # Kite client + session (access_token.txt)
├── shared/
│   └── protocol.ts   # Types shared by server and UI
├── web/              # React + Vite UI
│   └── src/
│       ├── App.tsx
│       ├── useFeed.ts          # WebSocket client + live state
│       ├── returns.ts          # Daily/weekly/monthly P&L aggregation
│       └── components/         # Watchlist, Positions, Login, Returns, Manage, Journal
├── test.ts           # CLI login script (npm start)
├── .env.example      # Credentials template
└── package.json
```

Files created at runtime, all gitignored: `.env`, `access_token.txt`, `watchlist.json`, `portfolio.json`, `web/dist/`.
