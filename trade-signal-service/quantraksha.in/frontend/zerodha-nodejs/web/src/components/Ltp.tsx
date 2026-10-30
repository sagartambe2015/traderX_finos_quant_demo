import type { Direction } from "../useFeed";
import { fmtPrice } from "../format";

/** Last price that briefly flashes green/red on each change. */
export function Ltp({ value, dir, ts }: { value: number | undefined; dir: Direction | undefined; ts: number | undefined }) {
  const cls = dir === 1 ? "flash-up" : dir === -1 ? "flash-down" : "";
  // Keying on the tick timestamp remounts the span so the CSS animation replays on every update
  return (
    <span key={ts} className={`ltp ${cls}`}>
      {fmtPrice(value)}
    </span>
  );
}
