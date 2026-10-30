import type { Portfolio, PositionRow, Positions, TickData } from "../../shared/protocol";

/** Kite's day (m2m) P&L, re-evaluated against the live tick price between position polls. */
export function liveM2m(p: PositionRow, ticks: Record<number, TickData>): number {
  const ltp = ticks[p.token]?.ltp ?? p.last_price;
  return p.m2m + p.quantity * (ltp - p.last_price) * p.multiplier;
}

/** date (YYYY-MM-DD) → strategyId → P&L */
export type DailyPnl = Map<string, Map<string, number>>;

/**
 * Effective daily P&L per strategy: journal entries (manual beats auto for the same day), with the
 * current trading day's auto figures replaced by live values from the positions.
 */
export function dailyPnl(portfolio: Portfolio, positions: Positions | null, ticks: Record<number, TickData>): DailyPnl {
  const key = (date: string, strategyId: string) => `${date}|${strategyId}`;
  const auto = new Map<string, number>();
  const manual = new Map<string, number>();
  for (const e of portfolio.journal) (e.source === "manual" ? manual : auto).set(key(e.date, e.strategyId), e.pnl);

  const today = positions?.tradingDate;
  if (today) {
    for (const k of [...auto.keys()]) if (k.startsWith(today + "|")) auto.delete(k);
    for (const p of positions.net) {
      if (!p.strategyId) continue;
      const k = key(today, p.strategyId);
      auto.set(k, (auto.get(k) ?? 0) + liveM2m(p, ticks));
    }
  }

  const daily: DailyPnl = new Map();
  // Manual entries come second so they overwrite auto ones
  for (const [k, pnl] of [...auto, ...manual]) {
    const [date, strategyId] = k.split("|");
    if (!daily.has(date)) daily.set(date, new Map());
    daily.get(date)!.set(strategyId, pnl);
  }
  return daily;
}

/** Sum daily P&L into periods: period key → strategyId → P&L. */
export function byPeriod(daily: DailyPnl, periodOf: (date: string) => string): DailyPnl {
  const out: DailyPnl = new Map();
  for (const [date, perStrategy] of daily) {
    const period = periodOf(date);
    if (!out.has(period)) out.set(period, new Map());
    const bucket = out.get(period)!;
    for (const [sid, pnl] of perStrategy) bucket.set(sid, (bucket.get(sid) ?? 0) + pnl);
  }
  return out;
}

export function istToday(): string {
  return new Date().toLocaleDateString("en-CA", { timeZone: "Asia/Kolkata" });
}

const utc = (date: string) => new Date(date + "T00:00:00Z");

/** Monday of the date's week, YYYY-MM-DD. */
export function weekOf(date: string): string {
  const d = utc(date);
  d.setUTCDate(d.getUTCDate() - ((d.getUTCDay() + 6) % 7));
  return d.toISOString().slice(0, 10);
}

export function monthOf(date: string): string {
  return date.slice(0, 7);
}

/** The `n` most recent week keys (Mondays), newest first. */
export function recentWeeks(today: string, n: number): string[] {
  const start = utc(weekOf(today));
  return Array.from({ length: n }, (_, i) => {
    const d = new Date(start);
    d.setUTCDate(d.getUTCDate() - 7 * i);
    return d.toISOString().slice(0, 10);
  });
}

/** The `n` most recent month keys (YYYY-MM), newest first. */
export function recentMonths(today: string, n: number): string[] {
  const [y, m] = today.split("-").map(Number);
  return Array.from({ length: n }, (_, i) => new Date(Date.UTC(y, m - 1 - i, 1)).toISOString().slice(0, 7));
}

export const weekLabel = (monday: string) =>
  "w/c " + utc(monday).toLocaleDateString("en-IN", { day: "numeric", month: "short", timeZone: "UTC" });

export const monthLabel = (month: string) =>
  utc(month + "-01").toLocaleDateString("en-IN", { month: "short", year: "numeric", timeZone: "UTC" });

/** Sum a period bucket over a set of strategies. */
export function sumFor(bucket: Map<string, number> | undefined, strategyIds: string[]): number {
  return strategyIds.reduce((s, id) => s + (bucket?.get(id) ?? 0), 0);
}
