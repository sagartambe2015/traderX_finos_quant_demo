import { useState, type FormEvent } from "react";
import type { Portfolio } from "../../../shared/protocol";
import { api } from "../useFeed";
import { fmtSigned, signClass } from "../format";
import { istToday } from "../returns";

const SHOWN = 30;

/** Daily P&L journal: manual entries, CSV backfill, and the recent history. */
export function Journal({ portfolio }: { portfolio: Portfolio }) {
  const { strategies, accounts, journal } = portfolio;
  const [entry, setEntry] = useState({ date: istToday(), strategy: "", pnl: "" });
  const [csv, setCsv] = useState("");
  const [message, setMessage] = useState<{ error: boolean; text: string } | null>(null);

  async function submit(entries: { date: string; strategy: string; pnl: number }[]) {
    setMessage(null);
    try {
      const res = await api<{ imported: number }>("POST", "/api/journal", { entries });
      setMessage({ error: false, text: `Saved ${res.imported} ${res.imported === 1 ? "entry" : "entries"}.` });
      return true;
    } catch (err: any) {
      setMessage({ error: true, text: err.message });
      return false;
    }
  }

  async function addOne(e: FormEvent) {
    e.preventDefault();
    const ok = await submit([{ ...entry, strategy: entry.strategy || strategies[0]?.id, pnl: Number(entry.pnl) }]);
    if (ok) setEntry({ ...entry, pnl: "" });
  }

  async function importCsv(e: FormEvent) {
    e.preventDefault();
    const rows = csv
      .split("\n")
      .map((line) => line.split(",").map((c) => c.trim()))
      .filter((cols) => cols.length >= 3 && cols[0])
      // Skip a header row such as "date,strategy,pnl"
      .filter((cols, i) => !(i === 0 && !/\d/.test(cols[0])));
    if (rows.length === 0) return setMessage({ error: true, text: "Paste lines of date,strategy,pnl" });
    const ok = await submit(rows.map(([date, strategy, pnl]) => ({ date, strategy, pnl: Number(pnl) })));
    if (ok) setCsv("");
  }

  const remove = (date: string, strategyId: string) =>
    api("DELETE", `/api/journal/${date}/${strategyId}`).catch((err) => setMessage({ error: true, text: err.message }));

  const recent = [...journal].reverse().slice(0, SHOWN);
  const strategyOf = (id: string) => strategies.find((s) => s.id === id);

  return (
    <section className="card">
      <div className="card-head">
        <h2>P&amp;L journal</h2>
      </div>
      {message && <div className={message.error ? "form-error" : "form-ok"}>{message.text}</div>}

      {strategies.length === 0 ? (
        <div className="empty-row">Add a strategy to start recording P&amp;L.</div>
      ) : (
        <div className="manage">
          <div>
            <h3>Add a day manually</h3>
            <form className="stack" onSubmit={addOne}>
              <input type="date" value={entry.date} onChange={(e) => setEntry({ ...entry, date: e.target.value })} aria-label="Date" />
              <select value={entry.strategy || strategies[0].id} onChange={(e) => setEntry({ ...entry, strategy: e.target.value })} aria-label="Strategy">
                {strategies.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.name}
                  </option>
                ))}
              </select>
              <input placeholder="P&L (₹), e.g. -2500" inputMode="decimal" value={entry.pnl} onChange={(e) => setEntry({ ...entry, pnl: e.target.value })} />
              <div className="row">
                <button type="submit">Save</button>
              </div>
            </form>
            <p className="muted hint">A manual entry replaces the recorded figure for that strategy and day.</p>
          </div>
          <div>
            <h3>Import CSV</h3>
            <form className="stack" onSubmit={importCsv}>
              <textarea
                rows={5}
                value={csv}
                onChange={(e) => setCsv(e.target.value)}
                placeholder={"date,strategy,pnl\n2026-10-20,Crude momentum,4500\n2026-10-21,Crude momentum,-1200"}
                spellCheck={false}
                aria-label="CSV rows"
              />
              <div className="row">
                <button type="submit">Import</button>
              </div>
            </form>
            <p className="muted hint">
              One row per strategy per day; <code>strategy</code> is the strategy name. Use it to backfill past days, e.g. from Zerodha Console.
              If any row is invalid, nothing is imported.
            </p>
          </div>
        </div>
      )}

      {recent.length > 0 && (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Date</th>
                <th>Strategy</th>
                <th className="hide-sm">Account</th>
                <th className="num">P&amp;L</th>
                <th>Source</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {recent.map((e) => {
                const s = strategyOf(e.strategyId);
                return (
                  <tr key={`${e.date}|${e.strategyId}|${e.source}`}>
                    <td>{e.date}</td>
                    <td>{s?.name ?? "—"}</td>
                    <td className="hide-sm">{accounts.find((a) => a.id === s?.accountId)?.name}</td>
                    <td className={`num ${signClass(e.pnl)}`}>{fmtSigned(e.pnl)}</td>
                    <td>
                      <span className="tag">{e.source === "auto" ? "recorded" : "manual"}</span>
                    </td>
                    <td className="num">
                      {e.source === "manual" && (
                        <button className="icon" onClick={() => remove(e.date, e.strategyId)} aria-label={`Delete ${e.date} entry`}>
                          ×
                        </button>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
          {journal.length > SHOWN && <div className="footnote">Showing the latest {SHOWN} of {journal.length} entries.</div>}
        </div>
      )}
    </section>
  );
}
