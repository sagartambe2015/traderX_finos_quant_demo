import { Fragment, useState } from "react";
import type { Portfolio, PositionRow, Positions, TickData } from "../../../shared/protocol";
import { api, type Direction } from "../useFeed";
import { fmtInt, fmtPct, fmtPrice, fmtSigned, signClass } from "../format";
import { Ltp } from "./Ltp";

/** Kite's P&L formula, re-evaluated against the live tick price between position polls. */
export function livePnl(p: PositionRow, ticks: Record<number, TickData>): number {
  const ltp = ticks[p.token]?.ltp ?? p.last_price;
  return p.sell_value - p.buy_value + p.quantity * ltp * p.multiplier;
}

export function totalPnl(rows: PositionRow[], ticks: Record<number, TickData>): number {
  return rows.reduce((sum, p) => sum + livePnl(p, ticks), 0);
}

interface Props {
  positions: Positions | null;
  portfolio: Portfolio;
  ticks: Record<number, TickData>;
  dir: Record<number, Direction>;
}

const AUTO = "__auto";
const NONE = "__none";

export function PositionsTable({ positions, portfolio, ticks, dir }: Props) {
  const [view, setView] = useState<"net" | "day">("net");
  const [error, setError] = useState<string | null>(null);
  const rows = positions?.[view] ?? [];
  const total = totalPnl(rows, ticks);
  const { strategies, accounts } = portfolio;
  const strategyName = (id: string | undefined) => strategies.find((s) => s.id === id)?.name;

  // One group per strategy (in strategy order), unassigned last; open positions before closed ones
  const groups = [...strategies.map((s) => s.id), undefined]
    .map((id) => ({
      id,
      rows: rows.filter((p) => p.strategyId === id).sort((a, b) => Number(b.quantity !== 0) - Number(a.quantity !== 0)),
    }))
    .filter((g) => g.rows.length > 0);
  const grouped = strategies.length > 0;
  const columns = grouped ? 8 : 7;

  async function assign(p: PositionRow, value: string) {
    setError(null);
    try {
      await api("PUT", "/api/assignments", {
        symbol: `${p.exchange}:${p.tradingsymbol}`,
        strategyId: value === AUTO ? null : value === NONE ? "" : value,
      });
    } catch (err: any) {
      setError(err.message);
    }
  }

  return (
    <section className="card">
      <div className="card-head">
        <h2>
          Positions <span className="count">{rows.length}</span>
        </h2>
        <div className="tabs" role="tablist">
          {(["net", "day"] as const).map((v) => (
            <button key={v} role="tab" aria-selected={view === v} className={view === v ? "active" : ""} onClick={() => setView(v)}>
              {v === "net" ? "Net" : "Day"}
            </button>
          ))}
        </div>
      </div>

      {error && <div className="form-error">{error}</div>}
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th className="hide-sm">Product</th>
              <th>Instrument</th>
              <th className="num">Qty</th>
              <th className="num">Avg</th>
              <th className="num">LTP</th>
              <th className="num">P&amp;L</th>
              <th className="num hide-sm">Chg %</th>
              {grouped && <th>Strategy</th>}
            </tr>
          </thead>
          <tbody>
            {groups.map((g) => {
              const strategy = strategies.find((s) => s.id === g.id);
              const account = accounts.find((a) => a.id === strategy?.accountId);
              const subtotal = totalPnl(g.rows, ticks);
              return (
                <Fragment key={g.id ?? NONE}>
                  {grouped && (
                    <tr className="group-row">
                      <td className="hide-sm" />
                      <td colSpan={4}>
                        {strategy ? strategy.name : "Unassigned"}
                        {account && <span className="exch">{account.name}</span>}
                      </td>
                      <td className={`num ${signClass(subtotal)}`}>{fmtSigned(subtotal)}</td>
                      <td className="hide-sm" />
                      <td />
                    </tr>
                  )}
                  {g.rows.map((p) => {
                    const t = ticks[p.token];
                    const ltp = t?.ltp ?? p.last_price;
                    const pnl = livePnl(p, ticks);
                    const close = t?.close ?? p.close_price;
                    const pct = close ? ((ltp - close) / close) * 100 : undefined;
                    const value = !p.manual ? AUTO : (p.strategyId ?? NONE);
                    return (
                      <tr key={`${p.exchange}:${p.tradingsymbol}:${p.product}`} className={p.quantity === 0 ? "closed" : ""}>
                        <td className="hide-sm">
                          <span className="tag">{p.product}</span>
                        </td>
                        <td>
                          <span className="sym">{p.tradingsymbol}</span>
                          <span className="exch">{p.exchange}</span>
                        </td>
                        <td className={`num ${signClass(p.quantity)}`}>{fmtInt(p.quantity)}</td>
                        <td className="num">{fmtPrice(p.average_price)}</td>
                        <td className="num">
                          <Ltp value={ltp} dir={dir[p.token]} ts={t?.ts} />
                        </td>
                        <td className={`num strong ${signClass(pnl)}`}>{fmtSigned(pnl)}</td>
                        <td className={`num hide-sm ${signClass(pct)}`}>{fmtPct(pct)}</td>
                        {grouped && (
                          <td>
                            <select
                              className="strategy-select"
                              value={value}
                              onChange={(e) => assign(p, e.target.value)}
                              aria-label={`Strategy for ${p.tradingsymbol}`}
                            >
                              <option value={AUTO}>Auto · {strategyName(p.ruleStrategyId) ?? "Unassigned"}</option>
                              {strategies.map((s) => (
                                <option key={s.id} value={s.id}>
                                  {s.name}
                                </option>
                              ))}
                              <option value={NONE}>Unassigned</option>
                            </select>
                          </td>
                        )}
                      </tr>
                    );
                  })}
                </Fragment>
              );
            })}
            {positions && rows.length === 0 && (
              <tr>
                <td colSpan={columns} className="empty-row">
                  No {view} positions.
                </td>
              </tr>
            )}
            {!positions && (
              <tr>
                <td colSpan={columns} className="empty-row">
                  Loading positions…
                </td>
              </tr>
            )}
          </tbody>
          {rows.length > 0 && (
            <tfoot>
              <tr>
                <td className="hide-sm" />
                <td colSpan={4}>Total</td>
                <td className={`num strong ${signClass(total)}`}>{fmtSigned(total)}</td>
                <td className="hide-sm" />
                {grouped && <td />}
              </tr>
            </tfoot>
          )}
        </table>
      </div>
      {positions && (
        <div className="footnote">
          Positions refreshed {new Date(positions.updatedAt).toLocaleTimeString()} · P&amp;L updates live
          {!grouped && " · Create strategies in the Portfolio tab to group positions"}
        </div>
      )}
    </section>
  );
}
