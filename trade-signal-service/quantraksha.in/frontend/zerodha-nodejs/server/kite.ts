import { KiteConnect } from "kiteconnect";
import * as fs from "fs";
import { dataFile } from "./paths";

const apiKey = process.env.KITE_API_KEY;
const apiSecret = process.env.KITE_API_SECRET;
if (!apiKey || !apiSecret) {
  console.error("Missing KITE_API_KEY or KITE_API_SECRET. Copy .env.example to .env and fill them in.");
  process.exit(1);
}

export const API_KEY = apiKey;

// Shared with test.ts so a login from either one is picked up by the other
const tokenFilePath = dataFile("access_token.txt");

export const kc = new KiteConnect({ api_key: apiKey });

export interface Session {
  accessToken: string;
  userName: string;
  userId: string;
}

let session: Session | null = null;

export function getSession(): Session | null {
  return session;
}

export function getLoginURL(): string {
  return kc.getLoginURL();
}

/** Try the access token saved on disk; returns null if missing or expired. */
export async function restoreSession(): Promise<Session | null> {
  if (!fs.existsSync(tokenFilePath)) return null;
  const saved = fs.readFileSync(tokenFilePath, "utf8").trim();
  if (!saved) return null;
  kc.setAccessToken(saved);
  try {
    const profile = await kc.getProfile();
    session = { accessToken: saved, userName: profile.user_name, userId: profile.user_id };
    return session;
  } catch {
    console.log("Saved access token is expired or invalid.");
    fs.unlinkSync(tokenFilePath);
    return null;
  }
}

/** Exchange a single-use request_token for an access token and persist it. */
export async function createSession(requestToken: string): Promise<Session> {
  const response = await kc.generateSession(requestToken, apiSecret!);
  kc.setAccessToken(response.access_token);
  fs.writeFileSync(tokenFilePath, response.access_token, "utf8");
  session = { accessToken: response.access_token, userName: response.user_name, userId: response.user_id };
  return session;
}

/** Kite returns 403 TokenException once the daily session expires. */
export function isTokenError(err: any): boolean {
  return err?.error_type === "TokenException" || err?.status === 403;
}

export function clearSession() {
  session = null;
  if (fs.existsSync(tokenFilePath)) fs.unlinkSync(tokenFilePath);
}
