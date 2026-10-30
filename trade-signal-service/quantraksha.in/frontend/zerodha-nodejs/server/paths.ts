import * as fs from "fs";
import * as path from "path";

/**
 * Where runtime state lives (access_token.txt, watchlist.json, portfolio.json). Defaults to the
 * project root; in Docker it points at a mounted volume so state survives redeploys.
 */
export const DATA_DIR = process.env.DATA_DIR ? path.resolve(process.env.DATA_DIR) : path.join(__dirname, "..");
fs.mkdirSync(DATA_DIR, { recursive: true });

export function dataFile(name: string): string {
  return path.join(DATA_DIR, name);
}
