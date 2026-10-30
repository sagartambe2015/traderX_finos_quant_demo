import { istDate } from "./ist";
import { kc } from "./kite";

/**
 * Continuous-futures aliases like "MCX:CRUDEOILM FUT" or "NFO:NIFTY FUT" resolve to the
 * nearest unexpired contract (e.g. "MCX:CRUDEOILM26NOVFUT"), so watchlists roll over on expiry.
 */
const FUT_ALIAS = /^([A-Z]+):([A-Z0-9&-]+) FUT$/;

export function isFutureAlias(symbol: string): boolean {
  return FUT_ALIAS.test(symbol);
}

interface FutureContract {
  /** Kite instrument key, e.g. "MCX:GOLDM26NOVFUT" */
  key: string;
  expiry: string; // YYYY-MM-DD
}

// Instrument dumps are large (NFO ≈ 37k rows) and change daily, so cache per exchange per IST date
const cache = new Map<string, { date: string; futures: Map<string, FutureContract[]> }>();

function toDateString(expiry: unknown): string {
  const s = String(expiry);
  if (/^\d{4}-\d{2}-\d{2}/.test(s)) return s.slice(0, 10);
  return istDate(new Date(s));
}

async function futuresFor(exchange: string): Promise<Map<string, FutureContract[]>> {
  const today = istDate();
  const hit = cache.get(exchange);
  if (hit && hit.date === today) return hit.futures;

  const rows: any[] = await kc.getInstruments([exchange] as any);
  const futures = new Map<string, FutureContract[]>();
  for (const r of rows) {
    if (r.instrument_type !== "FUT") continue;
    const list = futures.get(r.name) ?? [];
    list.push({ key: `${exchange}:${r.tradingsymbol}`, expiry: toDateString(r.expiry) });
    futures.set(r.name, list);
  }
  for (const list of futures.values()) list.sort((a, b) => a.expiry.localeCompare(b.expiry));
  cache.set(exchange, { date: today, futures });
  return futures;
}

/** Returns the near-month contract for an alias, or null if the underlying has no live futures. */
export async function resolveFutureAlias(alias: string): Promise<FutureContract | null> {
  const m = FUT_ALIAS.exec(alias);
  if (!m) return null;
  const [, exchange, name] = m;
  const today = istDate();
  const contracts = (await futuresFor(exchange)).get(name) ?? [];
  // Contracts still trade on their expiry day
  return contracts.find((c) => c.expiry >= today) ?? null;
}
