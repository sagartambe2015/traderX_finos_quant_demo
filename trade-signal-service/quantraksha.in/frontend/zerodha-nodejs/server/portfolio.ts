import { EventEmitter } from "events";
import * as fs from "fs";
import * as path from "path";
import { randomUUID } from "crypto";
import type { Account, JournalEntry, Portfolio, Strategy } from "../shared/protocol";
import { dataFile } from "./paths";

const PORTFOLIO_FILE = dataFile("portfolio.json");
const DATE = /^\d{4}-\d{2}-\d{2}$/;

export interface ManualEntryInput {
  date: string;
  /** Strategy id or name (case-insensitive). */
  strategy: string;
  pnl: number;
}

/** Turn "NFO:NIFTY*PE" into a regex. Patterns without an exchange match the trading symbol alone. */
function compileRule(rule: string): { withExchange: boolean; re: RegExp } {
  const pattern = rule.trim().toUpperCase();
  const body = pattern.replace(/[.+?^${}()|[\]\\]/g, "\\$&").replace(/\*/g, ".*");
  return { withExchange: pattern.includes(":"), re: new RegExp(`^${body}$`) };
}

/**
 * Accounts (capital buckets), strategies with their symbol rules, manual position assignments,
 * and the daily P&L journal used for weekly/monthly returns. Persisted to portfolio.json.
 * Emits "change" with the full Portfolio after every mutation.
 */
class PortfolioStore extends EventEmitter {
  private data: Portfolio = { accounts: [], strategies: [], overrides: {}, journal: [] };
  private compiled = new Map<string, ReturnType<typeof compileRule>[]>();

  constructor() {
    super();
    if (fs.existsSync(PORTFOLIO_FILE)) {
      try {
        this.data = { ...this.data, ...JSON.parse(fs.readFileSync(PORTFOLIO_FILE, "utf8")) };
      } catch {
        console.warn("portfolio.json is not valid JSON; starting empty");
      }
    }
    this.compileRules();
  }

  get(): Portfolio {
    return this.data;
  }

  // ---- accounts ----

  upsertAccount(input: Partial<Account>): Account {
    const name = String(input.name ?? "").trim();
    const capital = Number(input.capital);
    if (!name) throw new Error("Account name is required");
    if (!(capital > 0)) throw new Error("Capital must be a positive amount");
    this.assertUniqueName(this.data.accounts, name, input.id, "Account");

    let account = this.data.accounts.find((a) => a.id === input.id);
    if (account) Object.assign(account, { name, capital });
    else this.data.accounts.push((account = { id: randomUUID().slice(0, 8), name, capital }));
    this.commit();
    return account;
  }

  deleteAccount(id: string) {
    const used = this.data.strategies.filter((s) => s.accountId === id);
    if (used.length) throw new Error(`Move or delete its strategies first: ${used.map((s) => s.name).join(", ")}`);
    this.data.accounts = this.data.accounts.filter((a) => a.id !== id);
    this.commit();
  }

  // ---- strategies ----

  upsertStrategy(input: Partial<Strategy>): Strategy {
    const name = String(input.name ?? "").trim();
    if (!name) throw new Error("Strategy name is required");
    if (!this.data.accounts.some((a) => a.id === input.accountId)) throw new Error("Pick an account for the strategy");
    this.assertUniqueName(this.data.strategies, name, input.id, "Strategy");
    const rules = (input.rules ?? []).map((r) => String(r).trim().toUpperCase()).filter(Boolean);

    let strategy = this.data.strategies.find((s) => s.id === input.id);
    if (strategy) Object.assign(strategy, { name, accountId: input.accountId!, rules });
    else this.data.strategies.push((strategy = { id: randomUUID().slice(0, 8), name, accountId: input.accountId!, rules }));
    this.compileRules();
    this.commit();
    return strategy;
  }

  /** Also drops the strategy's journal history and any positions manually assigned to it. */
  deleteStrategy(id: string) {
    this.data.strategies = this.data.strategies.filter((s) => s.id !== id);
    this.data.journal = this.data.journal.filter((e) => e.strategyId !== id);
    for (const [symbol, sid] of Object.entries(this.data.overrides)) {
      if (sid === id) delete this.data.overrides[symbol];
    }
    this.compileRules();
    this.commit();
  }

  // ---- position → strategy ----

  /** strategyId: a strategy id, "" to force unassigned, or null to go back to the rules. */
  setOverride(symbol: string, strategyId: string | null) {
    if (!/^[A-Z]+:\S+$/.test(symbol)) throw new Error('symbol must be "EXCHANGE:TRADINGSYMBOL"');
    if (strategyId && !this.data.strategies.some((s) => s.id === strategyId)) throw new Error("Unknown strategy");
    if (strategyId === null) delete this.data.overrides[symbol];
    else this.data.overrides[symbol] = strategyId;
    this.commit();
  }

  assign(exchange: string, tradingsymbol: string): { strategyId?: string; ruleStrategyId?: string; manual: boolean } {
    const key = `${exchange}:${tradingsymbol}`;
    const ruleStrategyId = this.data.strategies.find((s) =>
      this.compiled.get(s.id)!.some((r) => r.re.test(r.withExchange ? key : tradingsymbol))
    )?.id;
    const override = this.data.overrides[key];
    if (override === undefined) return { strategyId: ruleStrategyId, ruleStrategyId, manual: false };
    return { strategyId: override || undefined, ruleStrategyId, manual: true };
  }

  // ---- journal ----

  /**
   * Replace the auto entries for `date` with today's per-strategy totals. Called on every positions
   * refresh, so the last refresh of the day leaves the closing figure in the journal.
   */
  recordDay(date: string, totals: Map<string, number>) {
    const kept = this.data.journal.filter((e) => !(e.source === "auto" && e.date === date));
    const fresh: JournalEntry[] = [...totals].map(([strategyId, pnl]) => ({ date, strategyId, pnl: round2(pnl), source: "auto" }));
    const before = this.data.journal.filter((e) => e.source === "auto" && e.date === date);
    if (sameEntries(before, fresh)) return;
    this.data.journal = [...kept, ...fresh];
    this.commit();
  }

  addManualEntries(inputs: ManualEntryInput[]): number {
    const entries: JournalEntry[] = inputs.map((input, i) => {
      const where = inputs.length > 1 ? `Row ${i + 1}: ` : "";
      const date = String(input.date ?? "").trim();
      if (!DATE.test(date) || Number.isNaN(Date.parse(date))) throw new Error(`${where}date must be YYYY-MM-DD`);
      const ref = String(input.strategy ?? "").trim().toLowerCase();
      const strategy = this.data.strategies.find((s) => s.id === ref || s.name.toLowerCase() === ref);
      if (!strategy) throw new Error(`${where}unknown strategy "${input.strategy}"`);
      const pnl = Number(input.pnl);
      if (!Number.isFinite(pnl)) throw new Error(`${where}P&L must be a number`);
      return { date, strategyId: strategy.id, pnl: round2(pnl), source: "manual" };
    });
    // Validate everything before changing anything, so a bad CSV row imports nothing
    for (const e of entries) {
      this.data.journal = this.data.journal.filter((j) => !(j.source === "manual" && j.date === e.date && j.strategyId === e.strategyId));
      this.data.journal.push(e);
    }
    this.commit();
    return entries.length;
  }

  deleteManualEntry(date: string, strategyId: string) {
    this.data.journal = this.data.journal.filter((e) => !(e.source === "manual" && e.date === date && e.strategyId === strategyId));
    this.commit();
  }

  // ---- internals ----

  private assertUniqueName(list: { id: string; name: string }[], name: string, id: string | undefined, kind: string) {
    if (list.some((x) => x.id !== id && x.name.toLowerCase() === name.toLowerCase())) {
      throw new Error(`${kind} "${name}" already exists`);
    }
  }

  private compileRules() {
    this.compiled = new Map(this.data.strategies.map((s) => [s.id, s.rules.map(compileRule)]));
  }

  private commit() {
    this.data.journal.sort((a, b) => a.date.localeCompare(b.date));
    fs.writeFileSync(PORTFOLIO_FILE, JSON.stringify(this.data, null, 2));
    this.emit("change", this.data);
  }
}

function round2(n: number) {
  return Math.round(n * 100) / 100;
}

function sameEntries(a: JournalEntry[], b: JournalEntry[]) {
  if (a.length !== b.length) return false;
  const byId = new Map(a.map((e) => [e.strategyId, e.pnl]));
  return b.every((e) => byId.get(e.strategyId) === e.pnl);
}

export const portfolio = new PortfolioStore();
