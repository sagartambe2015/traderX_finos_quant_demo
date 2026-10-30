import { Fragment, useState } from "react";
import type { Portfolio, Positions, TickData } from "../../../shared/protocol";
import { fmtInt, fmtPct, fmtSigned, signClass } from "../format";
import {
  byPeriod,
  dailyPnl,
  istToday,
  monthLabel,
  monthOf,
  recentMonths,
  recentWeeks,
  sumFor,
  weekLabel,
  weekOf,
} from "../returns";

interface Props {
  portfolio: Portfolio;
  positions: Positions | null;
  ticks: Record<number, TickData>;
}

/** P&L with its return on `capital` underneath. */
function PnlCell({ pnl, capital, strong }: { pnl: number; capital: number; strong?: boolean }) {
  return (
    <td className={`num ${signClass(pnl)} ${strong ? "strong" : ""}`}>
      {fmtSigned(pnl)}
      <span className="pct">{fmtPct((pnl / capital) * 100)}</span>
    </td>
  );
}

export function Returns({ portfolio, positions, ticks }: Props) {
  const [history, setHistory] = useState<"week" | "month">("week");
  const { accounts, strategies } = portfolio;

  if (accounts.length === 0) {
    return (
      <section className="card">
        <div className="card-head">
          <h2>Returns</h2>
        </div>
        <div className="empty-row">Add an account with its allocated capital below, then strategies under it.</div>
      </section>
    );
  }

  const today = istToday();
  const daily = dailyPnl(portfolio, positions, ticks);
  const weeks = byPeriod(daily, weekOf);
  const months = byPeriod(daily, monthOf);
  const idsOf = (accountId: string) => strategies.filter((s) => s.accountId === accountId).map((s) => s.id);
  const allIds = strategies.map((s) => s.id);
  const totalCapital = accounts.reduce((s, a) => s + a.capital, 0);

  const periods = history === "week" ? recentWeeks(today, 8) : recentMonths(today, 6);
  const buckets = history === "week" ? weeks : months;
  const label = history === "week" ? weekLabel : monthLabel;

  const columns = (ids: string[], capital: number, strong?: boolean) => (
    <>
      <PnlCell pnl={sumFor(daily.get(today), ids)} capital={capital} strong={strong} />
      <PnlCell pnl={sumFor(weeks.get(weekOf(today)), ids)} capital={capital} strong={strong} />
      <PnlCell pnl={sumFor(months.get(monthOf(today)), ids)} capital={capital} strong={strong} />
    </>
  );

  return (
    <>
      <section className="card">
        <div className="card-head">
          <h2>Returns</h2>
          <span className="muted">Return % is P&amp;L ÷ the account's capital</span>
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Account / strategy</th>
                <th className="num">Capital</th>
                <th className="num">Today</th>
                <th className="num">This week</th>
                <th className="num">This month</th>
              </tr>
            </thead>
            <tbody>
              {accounts.map((a) => (
                <Fragment key={a.id}>
                  <tr className="group-row">
                    <td>{a.name}</td>
                    <td className="num">{fmtInt(a.capital)}</td>
                    {columns(idsOf(a.id), a.capital, true)}
                  </tr>
                  {strategies
                    .filter((s) => s.accountId === a.id)
                    .map((s) => (
                      <tr key={s.id}>
                        <td className="indent">{s.name}</td>
                        <td />
                        {columns([s.id], a.capital)}
                      </tr>
                    ))}
                </Fragment>
              ))}
            </tbody>
            {accounts.length > 1 && (
              <tfoot>
                <tr>
                  <td>All accounts</td>
                  <td className="num">{fmtInt(totalCapital)}</td>
                  {columns(allIds, totalCapital, true)}
                </tr>
              </tfoot>
            )}
          </table>
        </div>
        <div className="footnote">
          Today updates live from positions. Past days come from the P&amp;L journal, which records each strategy's day P&amp;L
          while the server runs; positions with no strategy are not counted.
        </div>
      </section>

      <section className="card">
        <div className="card-head">
          <h2>History</h2>
          <div className="tabs" role="tablist">
            {(["week", "month"] as const).map((v) => (
              <button key={v} role="tab" aria-selected={history === v} className={history === v ? "active" : ""} onClick={() => setHistory(v)}>
                {v === "week" ? "Weekly" : "Monthly"}
              </button>
            ))}
          </div>
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>{history === "week" ? "Week" : "Month"}</th>
                {accounts.map((a) => (
                  <th key={a.id} className="num">
                    {a.name}
                  </th>
                ))}
                {accounts.length > 1 && <th className="num">All</th>}
              </tr>
            </thead>
            <tbody>
              {periods.map((p) => (
                <tr key={p}>
                  <td>{label(p)}</td>
                  {accounts.map((a) => (
                    <PnlCell key={a.id} pnl={sumFor(buckets.get(p), idsOf(a.id))} capital={a.capital} />
                  ))}
                  {accounts.length > 1 && <PnlCell pnl={sumFor(buckets.get(p), allIds)} capital={totalCapital} strong />}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </>
  );
}
