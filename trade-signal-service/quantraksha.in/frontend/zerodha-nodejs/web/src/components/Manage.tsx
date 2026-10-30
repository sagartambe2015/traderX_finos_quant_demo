import { useState, type FormEvent } from "react";
import type { Account, Portfolio, Strategy } from "../../../shared/protocol";
import { api } from "../useFeed";
import { fmtInt } from "../format";

/** Accounts (capital buckets) and the strategies mapped to them. */
export function Manage({ portfolio }: { portfolio: Portfolio }) {
  const { accounts, strategies } = portfolio;
  const [error, setError] = useState<string | null>(null);

  const [account, setAccount] = useState<{ id?: string; name: string; capital: string }>({ name: "", capital: "" });
  const [strategy, setStrategy] = useState<{ id?: string; name: string; accountId: string; rules: string }>({
    name: "",
    accountId: "",
    rules: "",
  });

  async function run(fn: () => Promise<unknown>) {
    setError(null);
    try {
      await fn();
      return true;
    } catch (err: any) {
      setError(err.message);
      return false;
    }
  }

  async function saveAccount(e: FormEvent) {
    e.preventDefault();
    const ok = await run(() => api("POST", "/api/accounts", { ...account, capital: Number(account.capital) }));
    if (ok) setAccount({ name: "", capital: "" });
  }

  async function saveStrategy(e: FormEvent) {
    e.preventDefault();
    const rules = strategy.rules.split(/[,\n]/).map((r) => r.trim()).filter(Boolean);
    const ok = await run(() => api("POST", "/api/strategies", { ...strategy, accountId: strategy.accountId || accounts[0]?.id, rules }));
    if (ok) setStrategy({ name: "", accountId: "", rules: "" });
  }

  const editAccount = (a: Account) => setAccount({ id: a.id, name: a.name, capital: String(a.capital) });
  const editStrategy = (s: Strategy) => setStrategy({ id: s.id, name: s.name, accountId: s.accountId, rules: s.rules.join(", ") });

  function deleteAccount(a: Account) {
    if (confirm(`Delete account "${a.name}"?`)) run(() => api("DELETE", `/api/accounts/${a.id}`));
  }

  function deleteStrategy(s: Strategy) {
    if (confirm(`Delete strategy "${s.name}"? Its P&L history and manual position assignments are deleted too.`)) {
      run(() => api("DELETE", `/api/strategies/${s.id}`));
    }
  }

  return (
    <section className="card">
      <div className="card-head">
        <h2>Accounts &amp; strategies</h2>
      </div>
      {error && <div className="form-error">{error}</div>}

      <div className="manage">
        <div>
          <h3>Accounts</h3>
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th className="num">Capital</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {accounts.map((a) => (
                <tr key={a.id}>
                  <td>{a.name}</td>
                  <td className="num">{fmtInt(a.capital)}</td>
                  <td className="num actions">
                    <button className="link" onClick={() => editAccount(a)}>
                      Edit
                    </button>
                    <button className="icon" onClick={() => deleteAccount(a)} aria-label={`Delete ${a.name}`}>
                      ×
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <form className="stack" onSubmit={saveAccount}>
            <input placeholder="Account name, e.g. Commodity" value={account.name} onChange={(e) => setAccount({ ...account, name: e.target.value })} />
            <input
              placeholder="Capital (₹)"
              inputMode="decimal"
              value={account.capital}
              onChange={(e) => setAccount({ ...account, capital: e.target.value })}
            />
            <div className="row">
              <button type="submit">{account.id ? "Save account" : "Add account"}</button>
              {account.id && (
                <button type="button" className="secondary" onClick={() => setAccount({ name: "", capital: "" })}>
                  Cancel
                </button>
              )}
            </div>
          </form>
        </div>

        <div>
          <h3>Strategies</h3>
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Account</th>
                <th>Rules</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {strategies.map((s) => (
                <tr key={s.id}>
                  <td>{s.name}</td>
                  <td>{accounts.find((a) => a.id === s.accountId)?.name}</td>
                  <td className="rules">{s.rules.length ? s.rules.map((r) => <code key={r}>{r}</code>) : <span className="muted">manual only</span>}</td>
                  <td className="num actions">
                    <button className="link" onClick={() => editStrategy(s)}>
                      Edit
                    </button>
                    <button className="icon" onClick={() => deleteStrategy(s)} aria-label={`Delete ${s.name}`}>
                      ×
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {accounts.length === 0 ? (
            <p className="muted hint">Add an account first.</p>
          ) : (
            <form className="stack" onSubmit={saveStrategy}>
              <input placeholder="Strategy name, e.g. Crude momentum" value={strategy.name} onChange={(e) => setStrategy({ ...strategy, name: e.target.value })} />
              <select value={strategy.accountId || accounts[0].id} onChange={(e) => setStrategy({ ...strategy, accountId: e.target.value })} aria-label="Account">
                {accounts.map((a) => (
                  <option key={a.id} value={a.id}>
                    {a.name}
                  </option>
                ))}
              </select>
              <input
                placeholder="Rules, e.g. MCX:CRUDEOILM*, NFO:NIFTY*PE"
                value={strategy.rules}
                onChange={(e) => setStrategy({ ...strategy, rules: e.target.value })}
                spellCheck={false}
              />
              <div className="row">
                <button type="submit">{strategy.id ? "Save strategy" : "Add strategy"}</button>
                {strategy.id && (
                  <button type="button" className="secondary" onClick={() => setStrategy({ name: "", accountId: "", rules: "" })}>
                    Cancel
                  </button>
                )}
              </div>
            </form>
          )}
          <p className="muted hint">
            Rules match <code>EXCHANGE:TRADINGSYMBOL</code> with <code>*</code> as a wildcard. When several strategies match, the first in this
            list wins. Override any position from the Strategy column on the Live tab.
          </p>
        </div>
      </div>
    </section>
  );
}
