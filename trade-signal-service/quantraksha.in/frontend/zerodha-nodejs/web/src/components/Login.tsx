import { useState, type FormEvent } from "react";
import { api } from "../useFeed";

/** Accepts either the bare request_token or the full URL Kite redirected to. */
function extractRequestToken(input: string): string {
  const trimmed = input.trim();
  try {
    return new URL(trimmed).searchParams.get("request_token") ?? trimmed;
  } catch {
    return trimmed;
  }
}

export function Login({ loginUrl }: { loginUrl: string }) {
  const [value, setValue] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function submit(e: FormEvent) {
    e.preventDefault();
    const token = extractRequestToken(value);
    if (!token) return;
    setBusy(true);
    setError(null);
    try {
      await api("POST", "/api/session", { request_token: token });
      // The server broadcasts the new status over the socket, which swaps this view out
    } catch (err: any) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="card login">
      <h2>Log in to Kite</h2>
      <ol>
        <li>
          <a href={loginUrl} target="_blank" rel="noreferrer">
            Open the Kite login page
          </a>{" "}
          and sign in with your Zerodha ID and TOTP.
        </li>
        <li>Copy the URL you're redirected to (or just its <code>request_token</code>) and paste it below.</li>
      </ol>
      <form onSubmit={submit} className="add-form">
        <input
          value={value}
          onChange={(e) => setValue(e.target.value)}
          placeholder="https://…?request_token=…  or  request_token"
          aria-label="Redirect URL or request token"
          spellCheck={false}
        />
        <button type="submit" disabled={busy}>
          {busy ? "Logging in…" : "Log in"}
        </button>
      </form>
      {error && <div className="form-error">{error}</div>}
      <p className="footnote">
        Request tokens are single-use and expire within minutes. To skip pasting, set your Kite app's Redirect URL to{" "}
        <code>http://127.0.0.1:4000/api/callback</code>.
      </p>
    </section>
  );
}
