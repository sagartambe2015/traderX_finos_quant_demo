import { useEffect, useState } from "react";
import { api } from "../useFeed";
import { fmtPrice } from "../format";

/** kite-rest-api's TradeSignalResponse: one row of the trade_capture table. */
interface TradeSignal {
  captureId: string;
  strategyId: string;
  security: string;
  instrumentId: string;
  side: string;
  lotSize: number;
  targetQuantity: number;
  entryDate: string;
  entryTime: string;
  exitDate: string | null;
  exitTime: string | null;
  calcEntry: number;
  calcTg: number;
  calcSl: number;
  calcTsl: number | null;
  actEntry: number | null;
  actTg: number | null;
  actSl: number | null;
  actTsl: number | null;
  hh: number | null;
  ll: number | null;
  riskRewardRatio: number | null;
  riskInPercentage: number | null;
  rewardInPercentage: number | null;
  goNoDecision: string;
  captureTimestamp: string | null;
}

type Filter = "all" | "open" | "exited";

const PRICES: [string, keyof TradeSignal][] = [
  ["Calc entry", "calcEntry"],
  ["Calc TG", "calcTg"],
  ["Calc SL", "calcSl"],
  ["Calc TSL", "calcTsl"],
  ["Act entry", "actEntry"],
  ["Act TG", "actTg"],
  ["Act SL", "actSl"],
  ["Act TSL", "actTsl"],
  ["HH", "hh"],
  ["LL", "ll"],
  ["R:R", "riskRewardRatio"],
  ["Risk %", "riskInPercentage"],
  ["Reward %", "rewardInPercentage"],
];

/** "07:10:12.305" → "07:10:12" */
const dateTime = (date: string | null, time: string | null) => (date ? `${date} ${time?.slice(0, 8) ?? ""}` : "—");

/** Signals saved by kite-rest-api. A row with no exit date counts as open, even if no order was placed for it. */
export function TradeCaptures() {
  const [rows, setRows] = useState<TradeSignal[] | null>(null);
  const [filter, setFilter] = useState<Filter>("all");
  const [error, setError] = useState<string | null>(null);

  async function load() {
    setError(null);
    try {
      setRows(await api<TradeSignal[]>("GET", "/api/getAllTradesAndPosition"));
    } catch (err: any) {
      setError(`Can't load trades: ${err.message}`);
    }
  }

  useEffect(() => {
    load();
  }, []);

  const shown = (rows ?? [])
    .filter((r) => filter === "all" || (filter === "open") === (r.exitDate == null))
    .sort((a, b) => (b.captureTimestamp ?? "").localeCompare(a.captureTimestamp ?? ""));

  return (
    <section className="card">
      <div className="card-head">
        <h2>
          Trades &amp; positions <span className="count">{shown.length}</span>
        </h2>
        <div className="tabs" role="tablist">
          {(["all", "open", "exited"] as const).map((f) => (
            <button key={f} role="tab" aria-selected={filter === f} className={filter === f ? "active" : ""} onClick={() => setFilter(f)}>
              {f === "all" ? "All" : f === "open" ? "Open" : "Exited"}
            </button>
          ))}
          <button onClick={load}>Refresh</button>
        </div>
      </div>

      {error && <div className="form-error">{error}</div>}
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Security</th>
              <th>Strategy</th>
              <th>Side</th>
              <th>Decision</th>
              <th className="num">Lot</th>
              <th className="num">Target qty</th>
              <th>Entry</th>
              <th>Exit</th>
              {PRICES.map(([label]) => (
                <th key={label} className="num">
                  {label}
                </th>
              ))}
              <th>Captured</th>
              <th>Capture ID</th>
            </tr>
          </thead>
          <tbody>
            {shown.map((r) => (
              <tr key={r.captureId}>
                <td>
                  <span className="sym">{r.security}</span>
                  <span className="exch">{r.instrumentId}</span>
                </td>
                <td>{r.strategyId}</td>
                <td>
                  <span className="tag">{r.side}</span>
                </td>
                <td>
                  <span className="tag">{r.goNoDecision}</span>
                </td>
                <td className="num">{r.lotSize}</td>
                <td className="num">{r.targetQuantity}</td>
                <td>{dateTime(r.entryDate, r.entryTime)}</td>
                <td>{dateTime(r.exitDate, r.exitTime)}</td>
                {PRICES.map(([label, key]) => (
                  <td key={label} className="num">
                    {fmtPrice((r[key] as number | null) ?? undefined)}
                  </td>
                ))}
                <td>{r.captureTimestamp?.slice(0, 19).replace("T", " ") ?? "—"}</td>
                <td className="muted">{r.captureId}</td>
              </tr>
            ))}
            {rows && shown.length === 0 && (
              <tr>
                <td colSpan={10 + PRICES.length} className="empty-row">
                  No {filter === "all" ? "" : filter} trades.
                </td>
              </tr>
            )}
            {!rows && !error && (
              <tr>
                <td colSpan={10 + PRICES.length} className="empty-row">
                  Loading trades…
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}
