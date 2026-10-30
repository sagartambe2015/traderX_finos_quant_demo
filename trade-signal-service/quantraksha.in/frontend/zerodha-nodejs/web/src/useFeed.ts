import { useEffect, useReducer } from "react";
import type { Portfolio, Positions, ServerMessage, Status, TickData, WatchItem } from "../../shared/protocol";

export type Direction = 1 | -1 | 0;

export interface FeedState {
  /** Browser ↔ relay socket, separate from the relay's own Kite ticker state in status.ticker. */
  socket: "open" | "connecting";
  status: Status | null;
  watchlist: WatchItem[];
  positions: Positions | null;
  portfolio: Portfolio;
  ticks: Record<number, TickData>;
  /** Direction of the most recent LTP change, used for the up/down flash. */
  dir: Record<number, Direction>;
}

type Action = { type: "socket"; socket: FeedState["socket"] } | ServerMessage;

const initial: FeedState = {
  socket: "connecting",
  status: null,
  watchlist: [],
  positions: null,
  portfolio: { accounts: [], strategies: [], overrides: {}, journal: [] },
  ticks: {},
  dir: {},
};

function mergeTicks(state: FeedState, incoming: TickData[]): Pick<FeedState, "ticks" | "dir"> {
  const ticks = { ...state.ticks };
  const dir = { ...state.dir };
  for (const t of incoming) {
    const prev = ticks[t.token];
    if (prev && t.ltp !== prev.ltp) dir[t.token] = t.ltp > prev.ltp ? 1 : -1;
    ticks[t.token] = t;
  }
  return { ticks, dir };
}

function reducer(state: FeedState, action: Action): FeedState {
  switch (action.type) {
    case "socket":
      return { ...state, socket: action.socket };
    case "snapshot":
      return {
        ...state,
        status: action.status,
        watchlist: action.watchlist,
        positions: action.positions,
        portfolio: action.portfolio,
        ...mergeTicks({ ...state, ticks: {}, dir: {} }, action.ticks),
      };
    case "status":
      return { ...state, status: action.status };
    case "watchlist":
      return { ...state, watchlist: action.watchlist };
    case "positions":
      return { ...state, positions: action.positions };
    case "ticks":
      return { ...state, ...mergeTicks(state, action.ticks) };
    case "portfolio":
      return { ...state, portfolio: action.portfolio };
  }
}

/** Connects to the relay's /ws endpoint and reconnects with backoff if it drops. */
export function useFeed(): FeedState {
  const [state, dispatch] = useReducer(reducer, initial);

  useEffect(() => {
    let ws: WebSocket | null = null;
    let retry = 0;
    let timer: number | undefined;
    let closed = false;

    const connect = () => {
      const proto = location.protocol === "https:" ? "wss" : "ws";
      ws = new WebSocket(`${proto}://${location.host}/ws`);
      dispatch({ type: "socket", socket: "connecting" });
      ws.onopen = () => {
        retry = 0;
        dispatch({ type: "socket", socket: "open" });
      };
      ws.onmessage = (e) => dispatch(JSON.parse(e.data) as ServerMessage);
      ws.onclose = () => {
        if (closed) return;
        dispatch({ type: "socket", socket: "connecting" });
        timer = window.setTimeout(connect, Math.min(1000 * 2 ** retry++, 10_000));
      };
    };
    connect();

    return () => {
      closed = true;
      window.clearTimeout(timer);
      ws?.close();
    };
  }, []);

  return state;
}

export async function api<T = unknown>(method: string, url: string, body?: unknown): Promise<T> {
  const res = await fetch(url, {
    method,
    headers: body ? { "Content-Type": "application/json" } : undefined,
    body: body ? JSON.stringify(body) : undefined,
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.error ?? `${res.status} ${res.statusText}`);
  }
  return res.status === 204 ? (undefined as T) : res.json();
}
