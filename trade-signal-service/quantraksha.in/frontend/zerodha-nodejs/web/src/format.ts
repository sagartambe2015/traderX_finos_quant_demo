const price = new Intl.NumberFormat("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
const int = new Intl.NumberFormat("en-IN", { maximumFractionDigits: 0 });

export const fmtPrice = (n: number | undefined) => (n == null || Number.isNaN(n) ? "—" : price.format(n));
export const fmtInt = (n: number | undefined) => (n == null || Number.isNaN(n) ? "—" : int.format(n));
export const fmtSigned = (n: number | undefined) => (n == null || Number.isNaN(n) ? "—" : (n > 0 ? "+" : "") + price.format(n));
export const fmtPct = (n: number | undefined) => (n == null || !Number.isFinite(n) ? "—" : (n > 0 ? "+" : "") + n.toFixed(2) + "%");

export const signClass = (n: number | undefined) => (!n ? "" : n > 0 ? "pos" : "neg");

/** "2026-11-19" → "19 Nov" */
export const fmtExpiry = (d: string | undefined) =>
  d ? new Date(d + "T00:00:00").toLocaleDateString("en-IN", { day: "numeric", month: "short" }) : "—";
