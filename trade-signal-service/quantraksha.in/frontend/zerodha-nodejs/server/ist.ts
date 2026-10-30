/** Calendar date in India (YYYY-MM-DD), which is what trading days and expiries are keyed on. */
export function istDate(d: Date = new Date()): string {
  return d.toLocaleDateString("en-CA", { timeZone: "Asia/Kolkata" });
}
