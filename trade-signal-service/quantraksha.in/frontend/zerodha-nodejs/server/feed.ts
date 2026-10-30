import { EventEmitter } from "events";
import * as fs from "fs";
import * as path from "path";
import { KiteTicker } from "kiteconnect";
import type { Position, Tick, FullTick } from "kiteconnect";
import { isFutureAlias, resolveFutureAlias } from "./instruments";
import { istDate } from "./ist";
import { dataFile } from "./paths";
import { portfolio } from "./portfolio";
import { kc, API_KEY, getSession, getLoginURL, clearSession, isTokenError } from "./kite";
import type { Positions, PositionRow, Status, TickData, TickerState, WatchItem } from "../shared/protocol";

const WATCHLIST_FILE = dataFile("watchlist.json");
const DEFAULT_WATCHLIST = [
  "NSE:NIFTY 50",
  "NFO:NIFTY FUT",
  "NSE:NIFTY BANK",
  "MCX:CRUDEOILM FUT",
  "MCX:GOLDM FUT",
  "MCX:SILVERM FUT",
  "NSE:RELIANCE",
  "NSE:INFY",
  "NSE:HDFCBANK",
];
const POSITIONS_POLL_MS = 5_000;
// Ticks are coalesced per instrument and flushed to browsers at this rate
const TICK_FLUSH_MS = 250;

type Ticker = InstanceType<typeof KiteTicker>;

/**
 * Owns the single Kite WebSocket connection plus watchlist/positions state.
 * Emits "status", "watchlist", "positions" and "ticks" for the browser relay in index.ts.
 */
class MarketFeed extends EventEmitter {
  private ticker: Ticker | null = null;
  private tickerState: TickerState = "disconnected";
  private lastError: string | undefined;

  private watchlist: WatchItem[] = [];
  private positions: Positions | null = null;
  private positionsTimer: NodeJS.Timeout | null = null;

  private latest = new Map<number, TickData>();
  private pending = new Map<number, TickData>();
  private subscribed = new Set<number>();

  constructor() {
    super();
    setInterval(() => this.flushTicks(), TICK_FLUSH_MS);
    // Rules or manual assignments changed: re-tag the current positions right away
    portfolio.on("change", () => {
      if (this.positions) this.publishPositions(this.positions.net, this.positions.day);
    });
  }

  getStatus(): Status {
    const session = getSession();
    return {
      authenticated: !!session,
      user: session ? `${session.userName} (${session.userId})` : undefined,
      loginUrl: getLoginURL(),
      ticker: this.tickerState,
      error: this.lastError,
    };
  }

  getSnapshot() {
    return {
      status: this.getStatus(),
      watchlist: this.watchlist,
      positions: this.positions,
      ticks: [...this.latest.values()],
      portfolio: portfolio.get(),
    };
  }

  /** Call once a valid session exists. */
  async start() {
    const session = getSession();
    if (!session) return;
    this.lastError = undefined;

    await this.loadWatchlist();
    await this.refreshPositions();
    if (!this.positionsTimer) {
      this.positionsTimer = setInterval(() => this.refreshPositions(), POSITIONS_POLL_MS);
    }
    this.connectTicker(session.accessToken);
  }

  stop(reason?: string) {
    if (this.positionsTimer) clearInterval(this.positionsTimer);
    this.positionsTimer = null;
    this.ticker?.disconnect();
    this.ticker = null;
    this.subscribed.clear();
    this.setTickerState("disconnected");
    this.lastError = reason;
    this.emit("status", this.getStatus());
  }

  // ---- watchlist ----

  async addSymbol(rawSymbol: string): Promise<WatchItem> {
    const symbol = rawSymbol.trim().toUpperCase().replace(/\s+/g, " ");
    if (!/^[A-Z]+:.+$/.test(symbol)) {
      throw new Error('Use EXCHANGE:SYMBOL, e.g. "NSE:INFY", or a continuous future like "MCX:GOLDM FUT"');
    }
    const existing = this.watchlist.find((w) => w.symbol === symbol);
    if (existing) return existing;

    const [item] = await this.resolve([symbol]);
    if (!item) throw new Error(`Unknown instrument: ${symbol}`);
    this.watchlist.push(item);
    this.saveWatchlist();
    this.emit("watchlist", this.watchlist);
    this.syncSubscriptions();
    return item;
  }

  removeSymbol(symbol: string) {
    this.watchlist = this.watchlist.filter((w) => w.symbol !== symbol);
    this.saveWatchlist();
    this.emit("watchlist", this.watchlist);
    this.syncSubscriptions();
  }

  private async loadWatchlist() {
    let symbols = DEFAULT_WATCHLIST;
    if (fs.existsSync(WATCHLIST_FILE)) {
      try {
        symbols = JSON.parse(fs.readFileSync(WATCHLIST_FILE, "utf8"));
      } catch {
        console.warn("watchlist.json is not valid JSON; using defaults");
      }
    }
    this.watchlist = await this.resolve(symbols);
    this.emit("watchlist", this.watchlist);
  }

  private saveWatchlist() {
    fs.writeFileSync(WATCHLIST_FILE, JSON.stringify(this.watchlist.map((w) => w.symbol), null, 2));
  }

  /**
   * Look up instrument tokens via a REST quote. This also seeds the latest prices,
   * so the UI shows values outside market hours when no ticks arrive.
   */
  private async resolve(symbols: string[]): Promise<WatchItem[]> {
    if (symbols.length === 0) return [];

    // Map each watchlist entry to the actual Kite instrument key (futures aliases → near-month contract)
    const contracts = new Map<string, { key: string; expiry?: string }>();
    for (const symbol of symbols) {
      if (!isFutureAlias(symbol)) {
        contracts.set(symbol, { key: symbol });
        continue;
      }
      const contract = await this.guard(() => resolveFutureAlias(symbol));
      if (contract) contracts.set(symbol, contract);
      else console.warn(`No live futures contract for ${symbol}`);
    }
    if (contracts.size === 0) return [];

    const keys = [...new Set([...contracts.values()].map((c) => c.key))];
    const quotes = await this.guard(() => kc.getQuote(keys));
    if (!quotes) return [];
    const items: WatchItem[] = [];
    for (const symbol of symbols) {
      const contract = contracts.get(symbol);
      const q: any = contract && quotes[contract.key];
      if (!q) continue;
      items.push(
        contract!.key === symbol
          ? { symbol, token: q.instrument_token }
          : { symbol, token: q.instrument_token, contract: contract!.key, expiry: contract!.expiry }
      );
      if (!this.latest.has(q.instrument_token)) {
        this.latest.set(q.instrument_token, {
          token: q.instrument_token,
          ltp: q.last_price,
          close: q.ohlc?.close,
          open: q.ohlc?.open,
          high: q.ohlc?.high,
          low: q.ohlc?.low,
          volume: q.volume || undefined,
          bid: q.depth?.buy?.[0]?.price || undefined,
          ask: q.depth?.sell?.[0]?.price || undefined,
          buyQty: q.buy_quantity || undefined,
          sellQty: q.sell_quantity || undefined,
          oi: q.oi || undefined,
          lastTradeTime: q.last_trade_time ?? undefined,
          ts: Date.now(),
        });
      }
    }
    return items;
  }

  // ---- positions ----

  private async refreshPositions() {
    const res = await this.guard(() => kc.getPositions());
    if (!res) return;
    const toRow = (p: Position): Omit<PositionRow, "strategyId" | "ruleStrategyId" | "manual"> => ({
      tradingsymbol: p.tradingsymbol,
      exchange: p.exchange,
      token: p.instrument_token,
      product: p.product,
      quantity: p.quantity,
      overnight_quantity: p.overnight_quantity,
      multiplier: p.multiplier,
      average_price: p.average_price,
      close_price: p.close_price,
      last_price: p.last_price,
      pnl: p.pnl,
      buy_quantity: p.buy_quantity,
      buy_value: p.buy_value,
      sell_quantity: p.sell_quantity,
      sell_value: p.sell_value,
      m2m: p.m2m,
    });
    this.publishPositions(res.net.map(toRow), res.day.map(toRow));
    if (this.lastError) {
      this.lastError = undefined;
      this.emit("status", this.getStatus());
    }
    this.syncSubscriptions();
  }

  /** Tag each position with its strategy, broadcast, and journal today's per-strategy P&L. */
  private publishPositions(net: Omit<PositionRow, "strategyId" | "ruleStrategyId" | "manual">[], day: typeof net) {
    const tag = (p: (typeof net)[number]): PositionRow => ({ ...p, ...portfolio.assign(p.exchange, p.tradingsymbol) });
    const tradingDate = this.tradingDate();
    this.positions = { net: net.map(tag), day: day.map(tag), updatedAt: Date.now(), tradingDate };
    this.emit("positions", this.positions);

    if (!tradingDate) return;
    const totals = new Map<string, number>();
    for (const p of this.positions.net) {
      if (p.strategyId) totals.set(p.strategyId, (totals.get(p.strategyId) ?? 0) + p.m2m);
    }
    portfolio.recordDay(tradingDate, totals);
  }

  /**
   * Kite keeps showing the last session's positions on weekends and holidays, so only treat them as
   * today's when some instrument we follow has actually traded today.
   */
  private tradingDate(): string | undefined {
    const today = istDate();
    for (const t of this.latest.values()) {
      if (t.lastTradeTime && istDate(new Date(t.lastTradeTime)) === today) return today;
    }
    return undefined;
  }

  // ---- ticker ----

  private connectTicker(accessToken: string) {
    if (this.ticker) return;
    const ticker = new KiteTicker({ api_key: API_KEY, access_token: accessToken, reconnect: true, max_retry: 300, max_delay: 30 });
    this.ticker = ticker;
    this.setTickerState("connecting");

    ticker.on("connect", () => {
      this.setTickerState("connected");
      this.subscribed.clear();
      this.syncSubscriptions();
    });
    ticker.on("ticks", (ticks: Tick[]) => this.onTicks(ticks));
    ticker.on("reconnect", () => this.setTickerState("connecting"));
    ticker.on("disconnect", () => this.setTickerState("connecting"));
    ticker.on("noreconnect", () => this.setTickerState("disconnected"));
    ticker.on("error", (err: Error) => {
      console.error("Ticker error:", err?.message ?? err);
    });
    ticker.connect();
  }

  private setTickerState(state: TickerState) {
    if (this.tickerState === state) return;
    this.tickerState = state;
    this.emit("status", this.getStatus());
  }

  /** Subscribe to watchlist + position instruments; drop anything no longer needed. */
  private syncSubscriptions() {
    const wanted = new Set<number>(this.watchlist.map((w) => w.token));
    for (const p of this.positions?.net ?? []) wanted.add(p.token);

    if (!this.ticker || this.tickerState !== "connected") return;
    const add = [...wanted].filter((t) => !this.subscribed.has(t));
    const remove = [...this.subscribed].filter((t) => !wanted.has(t));
    if (add.length) {
      this.ticker.subscribe(add);
      this.ticker.setMode(this.ticker.modeFull, add);
    }
    if (remove.length) this.ticker.unsubscribe(remove);
    this.subscribed = wanted;
  }

  private onTicks(ticks: Tick[]) {
    const now = Date.now();
    for (const t of ticks) {
      const f = t as Partial<FullTick> & Tick;
      const data: TickData = {
        token: t.instrument_token,
        ltp: t.last_price,
        close: f.ohlc?.close,
        open: f.ohlc?.open,
        high: f.ohlc?.high,
        low: f.ohlc?.low,
        volume: f.volume_traded || undefined,
        bid: f.depth?.buy?.[0]?.price || undefined,
        ask: f.depth?.sell?.[0]?.price || undefined,
        buyQty: f.total_buy_quantity || undefined,
        sellQty: f.total_sell_quantity || undefined,
        oi: f.oi || undefined,
        lastTradeTime: f.last_trade_time ? new Date(f.last_trade_time).toISOString() : undefined,
        ts: now,
      };
      this.latest.set(data.token, data);
      this.pending.set(data.token, data);
    }
  }

  private flushTicks() {
    if (this.pending.size === 0) return;
    const batch = [...this.pending.values()];
    this.pending.clear();
    this.emit("ticks", batch);
  }

  /** Run a Kite REST call; on an expired session, tear everything down instead of throwing. */
  private async guard<T>(fn: () => Promise<T>): Promise<T | null> {
    try {
      return await fn();
    } catch (err: any) {
      if (isTokenError(err)) {
        clearSession();
        this.stop("Kite session expired. Log in again.");
        return null;
      }
      console.error("Kite API error:", err?.message ?? err);
      this.lastError = err?.message ?? String(err);
      this.emit("status", this.getStatus());
      return null;
    }
  }
}

export const feed = new MarketFeed();
