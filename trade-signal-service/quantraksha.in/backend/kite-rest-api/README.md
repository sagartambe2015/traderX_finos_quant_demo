# kite-rest-api

A Spring Boot 3 (Java 17) REST API over Zerodha Kite Connect, for the React UI to call. It uses the `kiteconnect` 4.0.1 jar from Maven Central. Responses are Kite's own snake_case JSON, rendered by Gson from the SDK models' `@SerializedName` annotations.

## Run

```bash
export KITE_API_KEY=... KITE_API_SECRET=...
# Optional: share the Node relay's daily login instead of logging in separately
export KITE_TOKEN_FILE=../../frontend/zerodha-nodejs/access_token.txt

mvn spring-boot:run                      # or: mvn package && java -jar target/kite-rest-api-0.1.0.jar
```

It listens on `127.0.0.1:4100`, next to the Node relay on :4000. You can also set:

| Env | Default | Purpose |
| --- | --- | --- |
| `HOST` / `PORT` | `127.0.0.1` / `4100` | Bind address |
| `KITE_TOKEN_FILE` | `access_token.txt` | Saved access token (plain string, same format as the Node relay) |
| `UI_URL` | `http://localhost:5173/` | Where `/api/callback` redirects after login |
| `CORS_ORIGINS` | `http://localhost:5173,http://127.0.0.1:5173` | Allowed browser origins. Any other `Origin` gets a 403 |

## Endpoints

Swagger UI: <http://127.0.0.1:4100/swagger-ui.html>. The OpenAPI spec is at `/v3/api-docs` (springdoc).

| Method | Path | Returns |
| --- | --- | --- |
| GET | `/api/status` | `{loggedIn, userId, userName, loginUrl}` |
| POST | `/api/session` | Body `{"requestToken": "<token or full redirect URL>"}`. Creates the day's session |
| GET | `/api/callback?request_token=` | Usable as the Kite app's Redirect URL. Logs in, then redirects to `UI_URL` |
| DELETE | `/api/session` | Invalidates the token at Kite (this also logs out the Node relay if they share the token file) |
| GET | `/api/profile`, `/api/margins`, `/api/margins/{equity\|commodity}` | Account |
| GET | `/api/positions`, `/api/holdings` | `{net, day}` / list |
| GET | `/api/orders`, `/api/orders/{id}/history`, `/api/trades` | Today's orders and trades (read-only) |
| GET | `/api/quote`, `/api/ohlc`, `/api/ltp` `?i=NSE:INFY&i=NSE:TCS` | Map keyed by instrument |
| GET | `/api/levels/{instrumentToken}?days=2` | N-day breakout levels: `hh` (highest high) and `ll` (lowest low) of the last N completed daily candles, plus the candles used. `days=2` gives 2D HH / 2D LL. Today's forming candle is excluded |

Errors look like `{"error_type": "...", "message": "..."}`. The status codes are:
- 401 for `NotLoggedIn` or an expired `TokenException` (the saved token is then deleted)
- 400 for bad input
- 403 for a permission error
- 502 for other Kite or network failures

When the session is restored from the token file, `userId` is `null`, because the SDK's `Profile` model has no `user_id` field. It is filled in on the next login.
