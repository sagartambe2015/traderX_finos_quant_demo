// Message and data shapes shared by the server (server/) and the React UI (web/).
// Type-only: import with `import type` so nothing from here ends up in the web bundle.

export interface TickData {
  token: number;
  ltp: number;
  /** Previous day's close; change % is derived from this. */
  close?: number;
  open?: number;
  high?: number;
  low?: number;
  volume?: number;
  bid?: number;
  ask?: number;
  buyQty?: number;
  sellQty?: number;
  oi?: number;
  lastTradeTime?: string;
  /** Server receive time (ms epoch). */
  ts: number;
}

export interface WatchItem {
  /** As entered: a Kite instrument key ("NSE:INFY") or a continuous-futures alias ("MCX:GOLDM FUT"). */
  symbol: string;
  token: number;
  /** For futures aliases: the contract currently tracked, e.g. "MCX:GOLDM26NOVFUT". */
  contract?: string;
  /** For futures aliases: the contract's expiry, YYYY-MM-DD. */
  expiry?: string;
}

export interface PositionRow {
  tradingsymbol: string;
  exchange: string;
  token: number;
  product: string;
  quantity: number;
  overnight_quantity: number;
  multiplier: number;
  average_price: number;
  close_price: number;
  last_price: number;
  pnl: number;
  buy_quantity: number;
  buy_value: number;
  sell_quantity: number;
  sell_value: number;
  /** Kite's mark-to-market P&L for the day (overnight quantity is valued from the previous close). */
  m2m: number;
  /** Strategy this position counts towards; undefined = unassigned. */
  strategyId?: string;
  /** What the strategy rules alone would pick, shown as the "Auto" choice in the UI. */
  ruleStrategyId?: string;
  /** True when strategyId comes from a manual override rather than the rules. */
  manual: boolean;
}

export interface Positions {
  net: PositionRow[];
  day: PositionRow[];
  updatedAt: number;
  /** IST date (YYYY-MM-DD) these positions' day P&L belongs to; absent when the market hasn't traded today. */
  tradingDate?: string;
}

/** A capital bucket within the single Kite login. Returns are measured against `capital`. */
export interface Account {
  id: string;
  name: string;
  capital: number;
}

export interface Strategy {
  id: string;
  name: string;
  accountId: string;
  /** Glob patterns on "EXCHANGE:TRADINGSYMBOL" (or just "TRADINGSYMBOL"), e.g. "MCX:CRUDEOILM*", "NFO:NIFTY*PE". */
  rules: string[];
}

/** One strategy's P&L for one day. Manual entries win over auto ones for the same date and strategy. */
export interface JournalEntry {
  date: string; // YYYY-MM-DD (IST)
  strategyId: string;
  pnl: number;
  source: "auto" | "manual";
}

export interface Portfolio {
  accounts: Account[];
  /** Ordered: when several strategies' rules match a position, the first one wins. */
  strategies: Strategy[];
  /** "EXCHANGE:TRADINGSYMBOL" → strategyId, or "" to force unassigned. */
  overrides: Record<string, string>;
  journal: JournalEntry[];
}

export type TickerState = "connected" | "connecting" | "disconnected";

export interface Status {
  authenticated: boolean;
  user?: string;
  loginUrl: string;
  ticker: TickerState;
  error?: string;
}

export type ServerMessage =
  | { type: "snapshot"; status: Status; watchlist: WatchItem[]; positions: Positions | null; ticks: TickData[]; portfolio: Portfolio }
  | { type: "status"; status: Status }
  | { type: "watchlist"; watchlist: WatchItem[] }
  | { type: "positions"; positions: Positions }
  | { type: "ticks"; ticks: TickData[] }
  | { type: "portfolio"; portfolio: Portfolio };
