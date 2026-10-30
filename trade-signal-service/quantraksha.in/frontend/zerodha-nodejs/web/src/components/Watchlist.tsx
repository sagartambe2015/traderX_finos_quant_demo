import { useState, type FormEvent } from "react";
import type { TickData, WatchItem } from "../../../shared/protocol";
import { api, type Direction } from "../useFeed";
import { fmtExpiry, fmtInt, fmtPct, fmtPrice, fmtSigned, signClass } from "../format";
import { Ltp } from "./Ltp";

interface Props {
  watchlist: WatchItem[];
  ticks: Record<number, TickData>;
  dir: Record<number, Direction>;
}

export function Watchlist({ watchlist, ticks, dir }: Props) {
  const [symbol, setSymbol] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function add(e: FormEvent) {
    e.preventDefault();
    if (!symbol.trim()) return;
    setBusy(true);
    setError(null);
    try {
      await api("POST", "/api/watchlist", { symbol });
      setSymbol("");
    } catch (err: any) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function remove(s: string) {
    try {
      await api("DELETE", `/api/watchlist/${encodeURIComponent(s)}`);
    } catch (err: any) {
      setError(err.message);
    }
  }

  return (
    <section className="card">
      <div className="card-head">
        <h2>Watchlist</h2>
        <form onSubmit={add} className="add-form">
          <input
            value={symbol}
            onChange={(e) => setSymbol(e.target.value)}
            placeholder="NSE:SBIN or MCX:GOLDM FUT"
            aria-label="Instrument to add"
            spellCheck={false}
          />
          <button type="submit" disabled={busy}>
            Add
          </button>
        </form>
      </div>
      {error && <div className="form-error">{error}</div>}

      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Instrument</th>
              <th className="num">LTP</th>
              <th className="num">Chg</th>
              <th className="num">Chg %</th>
              <th className="num hide-sm">Open</th>
              <th className="num hide-sm">High</th>
              <th className="num hide-sm">Low</th>
              <th className="num hide-sm">Bid / Ask</th>
              <th className="num hide-sm">Volume</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {watchlist.map((w) => {
              const t = ticks[w.token];
              const chg = t?.close ? t.ltp - t.close : undefined;
              const pct = t?.close ? (chg! / t.close) * 100 : undefined;
              return (
                <tr key={w.symbol}>
                  <td>
                    <span className="sym">{w.symbol.split(":")[1]}</span>
                    <span className="exch">{w.symbol.split(":")[0]}</span>
                    {w.contract && (
                      <span className="contract" title={`Tracking ${w.contract}; rolls to the next contract after expiry`}>
                        {w.contract.split(":")[1]} · exp {fmtExpiry(w.expiry)}
                      </span>
                    )}
                  </td>
                  <td className="num">
                    <Ltp value={t?.ltp} dir={dir[w.token]} ts={t?.ts} />
                  </td>
                  <td className={`num ${signClass(chg)}`}>{fmtSigned(chg)}</td>
                  <td className={`num ${signClass(pct)}`}>{fmtPct(pct)}</td>
                  <td className="num hide-sm">{fmtPrice(t?.open)}</td>
                  <td className="num hide-sm">{fmtPrice(t?.high)}</td>
                  <td className="num hide-sm">{fmtPrice(t?.low)}</td>
                  <td className="num hide-sm muted">
                    {t?.bid || t?.ask ? `${fmtPrice(t.bid)} / ${fmtPrice(t.ask)}` : "—"}
                  </td>
                  <td className="num hide-sm">{fmtInt(t?.volume)}</td>
                  <td className="num">
                    <button className="icon" onClick={() => remove(w.symbol)} aria-label={`Remove ${w.symbol}`}>
                      ×
                    </button>
                  </td>
                </tr>
              );
            })}
            {watchlist.length === 0 && (
              <tr>
                <td colSpan={10} className="empty-row">
                  Add an instrument such as <code>NSE:INFY</code>, <code>NSE:NIFTY 50</code> or <code>MCX:CRUDEOILM FUT</code>.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}
