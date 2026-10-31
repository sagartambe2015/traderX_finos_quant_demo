import express from "express";
import * as http from "http";
import * as fs from "fs";
import * as path from "path";
import * as crypto from "crypto";
import { WebSocketServer, WebSocket } from "ws";
import { createSession, restoreSession } from "./kite";
import { feed } from "./feed";
import { portfolio } from "./portfolio";
import type { ServerMessage } from "../shared/protocol";

const PORT = Number(process.env.PORT ?? 4000);
// Loopback by default: this server holds a live trading session. Docker sets HOST=0.0.0.0,
// which is only allowed together with DASHBOARD_PASSWORD (checked below).
const HOST = process.env.HOST ?? "127.0.0.1";
// Public address when deployed behind a reverse proxy, e.g. https://kite.example.com
const PUBLIC_URL = process.env.PUBLIC_URL?.replace(/\/+$/, "");
const WEB_URL = process.env.WEB_URL ?? PUBLIC_URL ?? "http://localhost:5173";
const WEB_DIST = path.join(__dirname, "..", "web", "dist");
const KITE_REST_URL = process.env.KITE_REST_URL ?? "http://127.0.0.1:4100";

const DASHBOARD_USER = process.env.DASHBOARD_USER || "admin";
const DASHBOARD_PASSWORD = process.env.DASHBOARD_PASSWORD ?? "";
const isLoopback = HOST === "127.0.0.1" || HOST === "localhost" || HOST === "::1";
if (!isLoopback && !DASHBOARD_PASSWORD) {
  console.error(`Refusing to listen on ${HOST} without DASHBOARD_PASSWORD: positions would be readable by anyone.`);
  process.exit(1);
}

const publicOrigin = PUBLIC_URL ? new URL(PUBLIC_URL).origin : null;

/**
 * Browsers attach an Origin header to cross-site requests, so any open web page could otherwise
 * call this server or open the WebSocket and read positions. Allow only local origins, plus
 * PUBLIC_URL when deployed.
 */
function isAllowedOrigin(origin: string | undefined): boolean {
  if (!origin) return true; // same-origin GETs and non-browser clients
  try {
    const url = new URL(origin);
    return url.hostname === "localhost" || url.hostname === "127.0.0.1" || url.origin === publicOrigin;
  } catch {
    return false;
  }
}

/** HTTP Basic auth, enabled when DASHBOARD_PASSWORD is set. Browsers reuse it for fetch and /ws. */
function isAuthorized(header: string | undefined): boolean {
  if (!DASHBOARD_PASSWORD) return true;
  if (!header?.startsWith("Basic ")) return false;
  const expected = Buffer.from(`${DASHBOARD_USER}:${DASHBOARD_PASSWORD}`);
  const given = Buffer.from(Buffer.from(header.slice(6), "base64").toString("utf8"));
  return given.length === expected.length && crypto.timingSafeEqual(given, expected);
}

const app = express();
app.set("trust proxy", true); // behind Traefik/Nginx in Docker deployments

// Unauthenticated liveness probe for container health checks; reveals nothing.
app.get("/healthz", (_req, res) => {
  res.json({ ok: true });
});

app.use((req, res, next) => {
  if (!isAllowedOrigin(req.headers.origin)) return res.status(403).json({ error: "Origin not allowed" });
  if (!isAuthorized(req.headers.authorization)) {
    res.set("WWW-Authenticate", 'Basic realm="Kite dashboard", charset="UTF-8"');
    return res.status(401).send("Authentication required");
  }
  next();
});
app.use(express.json());

app.get("/api/status", (_req, res) => {
  res.json(feed.getStatus());
});

/** Accepts the request_token from the Kite login redirect, pasted into the UI. */
app.post("/api/session", async (req, res) => {
  const requestToken = String(req.body?.request_token ?? "").trim();
  if (!requestToken) return res.status(400).json({ error: "request_token is required" });
  try {
    await createSession(requestToken);
    await feed.start();
    res.json(feed.getStatus());
  } catch (err: any) {
    res.status(400).json({ error: err?.message ?? "Failed to create session" });
  }
});

/** Set this as the Redirect URL of your Kite Connect app to log in without copy-pasting. */
app.get("/api/callback", async (req, res) => {
  const requestToken = String(req.query.request_token ?? "");
  if (req.query.status !== "success" || !requestToken) return res.redirect(WEB_URL);
  try {
    await createSession(requestToken);
    await feed.start();
  } catch (err: any) {
    console.error("Login callback failed:", err?.message ?? err);
  }
  res.redirect(WEB_URL);
});

/** Saved signals live in kite-rest-api's database; the relay only passes them through. */
app.get("/api/getAllTradesAndPosition", async (_req, res) => {
  try {
    const upstream = await fetch(`${KITE_REST_URL}/api/getAllTradesAndPosition`);
    res.status(upstream.status).type("json").send(await upstream.text());
  } catch {
    res.status(502).json({ error: `kite-rest-api is not reachable at ${KITE_REST_URL}` });
  }
});

app.post("/api/watchlist", async (req, res) => {
  try {
    const item = await feed.addSymbol(String(req.body?.symbol ?? ""));
    res.json(item);
  } catch (err: any) {
    res.status(400).json({ error: err?.message ?? "Failed to add symbol" });
  }
});

app.delete("/api/watchlist/:symbol", (req, res) => {
  feed.removeSymbol(req.params.symbol);
  res.status(204).end();
});

// ---- accounts, strategies, assignments, P&L journal ----
// Every mutation broadcasts the new portfolio over /ws, so these only need to report errors.

/** Run a synchronous store mutation, turning validation errors into 400s. */
function mutate(res: express.Response, fn: () => unknown) {
  try {
    const result = fn();
    result === undefined ? res.status(204).end() : res.json(result);
  } catch (err: any) {
    res.status(400).json({ error: err?.message ?? "Request failed" });
  }
}

app.post("/api/accounts", (req, res) => mutate(res, () => portfolio.upsertAccount(req.body ?? {})));
app.delete("/api/accounts/:id", (req, res) => mutate(res, () => portfolio.deleteAccount(req.params.id)));
app.post("/api/strategies", (req, res) => mutate(res, () => portfolio.upsertStrategy(req.body ?? {})));
app.delete("/api/strategies/:id", (req, res) => mutate(res, () => portfolio.deleteStrategy(req.params.id)));

/** { symbol: "NFO:NIFTY26NOVFUT", strategyId: "<id>" | "" (unassigned) | null (back to rules) } */
app.put("/api/assignments", (req, res) =>
  mutate(res, () => portfolio.setOverride(String(req.body?.symbol ?? ""), req.body?.strategyId ?? null))
);

/** { entries: [{ date: "YYYY-MM-DD", strategy: "<id or name>", pnl: number }] } */
app.post("/api/journal", (req, res) =>
  mutate(res, () => ({ imported: portfolio.addManualEntries(Array.isArray(req.body?.entries) ? req.body.entries : []) }))
);
app.delete("/api/journal/:date/:strategyId", (req, res) =>
  mutate(res, () => portfolio.deleteManualEntry(req.params.date, req.params.strategyId))
);

// Serve the production build of the UI if it exists (`npm run build`)
if (fs.existsSync(WEB_DIST)) {
  app.use(express.static(WEB_DIST));
  app.get(/^(?!\/api|\/ws).*/, (_req, res) => res.sendFile(path.join(WEB_DIST, "index.html")));
}

const server = http.createServer(app);
const wss = new WebSocketServer({
  server,
  path: "/ws",
  verifyClient: ({ origin, req }: { origin?: string; req: http.IncomingMessage }) =>
    isAllowedOrigin(origin) && isAuthorized(req.headers.authorization),
});

function send(ws: WebSocket, msg: ServerMessage) {
  if (ws.readyState === WebSocket.OPEN) ws.send(JSON.stringify(msg));
}

function broadcast(msg: ServerMessage) {
  const data = JSON.stringify(msg);
  for (const ws of wss.clients) {
    if (ws.readyState === WebSocket.OPEN) ws.send(data);
  }
}

wss.on("connection", (ws) => {
  send(ws, { type: "snapshot", ...feed.getSnapshot() });
});

feed.on("status", (status) => broadcast({ type: "status", status }));
feed.on("watchlist", (watchlist) => broadcast({ type: "watchlist", watchlist }));
feed.on("positions", (positions) => broadcast({ type: "positions", positions }));
feed.on("ticks", (ticks) => broadcast({ type: "ticks", ticks }));
portfolio.on("change", (p) => broadcast({ type: "portfolio", portfolio: p }));

server.listen(PORT, HOST, async () => {
  console.log(`Server listening on http://${HOST}:${PORT}`);
  const session = await restoreSession();
  if (session) {
    console.log(`Restored Kite session for ${session.userName}`);
    await feed.start();
  } else {
    console.log("No valid Kite session. Log in from the UI.");
  }
});
