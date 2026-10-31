import { useState } from "react";
import { useFeed } from "./useFeed";
import { Login } from "./components/Login";
import { Watchlist } from "./components/Watchlist";
import { PositionsTable, totalPnl } from "./components/Positions";
import { Returns } from "./components/Returns";
import { Manage } from "./components/Manage";
import { Journal } from "./components/Journal";
import { TradeCaptures } from "./components/TradeCaptures";
import { fmtSigned, signClass } from "./format";

type View = "live" | "portfolio" | "trades";

function savedView(): View {
  try {
    const saved = localStorage.getItem("view");
    return saved === "portfolio" || saved === "trades" ? saved : "live";
  } catch {
    return "live";
  }
}

export default function App() {
  const feed = useFeed();
  const [view, setViewState] = useState<View>(savedView);
  const setView = (v: View) => {
    setViewState(v);
    try {
      localStorage.setItem("view", v);
    } catch {
      // storage unavailable; the tab just won't be remembered
    }
  };
  const { status, socket } = feed;

  const conn =
    socket !== "open"
      ? { label: "Server offline", cls: "bad" }
      : !status?.authenticated
        ? { label: "Not logged in", cls: "warn" }
        : status.ticker === "connected"
          ? { label: "Live", cls: "ok" }
          : { label: `Ticker ${status.ticker}`, cls: "warn" };

  const pnl = feed.positions ? totalPnl(feed.positions.net, feed.ticks) : undefined;

  return (
    <div className="app">
      <header className="topbar">
        <div className="topbar-left">
          <h1>Kite Dashboard</h1>
          <nav className="tabs" role="tablist">
            {(["live", "portfolio", "trades"] as const).map((v) => (
              <button key={v} role="tab" aria-selected={view === v} className={view === v ? "active" : ""} onClick={() => setView(v)}>
                {v === "live" ? "Live" : v === "portfolio" ? "Portfolio" : "Trades"}
              </button>
            ))}
          </nav>
        </div>
        <div className="topbar-right">
          {pnl !== undefined && (
            <span className="pnl-summary">
              Net P&amp;L <strong className={signClass(pnl)}>{fmtSigned(pnl)}</strong>
            </span>
          )}
          {status?.user && <span className="user">{status.user}</span>}
          <span className={`conn ${conn.cls}`}>
            <span className="dot" />
            {conn.label}
          </span>
        </div>
      </header>

      {status?.error && <div className="banner">{status.error}</div>}

      {view === "trades" ? (
        // Comes from kite-rest-api's database, so it doesn't need a Kite login
        <main className="stack-page">
          <TradeCaptures />
        </main>
      ) : socket !== "open" ? (
        <div className="empty">
          Can't reach the relay server. Start it with <code>npm run dev</code> in <code>frontend/zerodha-nodejs/</code>.
        </div>
      ) : status && !status.authenticated ? (
        <Login loginUrl={status.loginUrl} />
      ) : view === "live" ? (
        <main className="grid">
          <Watchlist watchlist={feed.watchlist} ticks={feed.ticks} dir={feed.dir} />
          <PositionsTable positions={feed.positions} portfolio={feed.portfolio} ticks={feed.ticks} dir={feed.dir} />
        </main>
      ) : (
        <main className="stack-page">
          <Returns portfolio={feed.portfolio} positions={feed.positions} ticks={feed.ticks} />
          <Manage portfolio={feed.portfolio} />
          <Journal portfolio={feed.portfolio} />
        </main>
      )}
    </div>
  );
}
