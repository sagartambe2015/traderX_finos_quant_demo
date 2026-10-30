import { KiteConnect } from "kiteconnect";
import * as fs from "fs";
import * as path from "path";

const apiKey = process.env.KITE_API_KEY;
const apiSecret = process.env.KITE_API_SECRET;
if (!apiKey || !apiSecret) {
  console.error("Missing KITE_API_KEY or KITE_API_SECRET. Copy .env.example to .env and fill them in.");
  process.exit(1);
}

// You can pass the request_token via CLI: npm start -- <request_token>
// or paste it directly below:
const fallbackRequestToken = "0qTIU8ZSGX5h3TDkn5SGobtKkxHKHDYu";
const requestToken = process.argv[2] || fallbackRequestToken;

const tokenFilePath = path.join(__dirname, "access_token.txt");
const kc = new KiteConnect({ api_key: apiKey });

async function init() {
  try {
    // 1. Check if we already have a saved access token from today
    if (fs.existsSync(tokenFilePath)) {
      const savedToken = fs.readFileSync(tokenFilePath, "utf8").trim();
      if (savedToken) {
        console.log("Found existing access token in access_token.txt, validating...");
        kc.setAccessToken(savedToken);
        try {
          const profile = await kc.getProfile();
          console.log("✓ Successfully authenticated with saved access token!");
          console.log("Profile:", profile);
          return;
        } catch (profileErr: any) {
          console.log("Saved access token is expired or invalid. Generating a new session...");
          fs.unlinkSync(tokenFilePath);
        }
      }
    }

    // 2. Generate a new session with request_token
    await generateSession();
    await getProfile();
  } catch (err: any) {
    console.error("\n❌ Authentication Failed!");
    console.log("\n--- HOW TO FIX ---");
    console.log("1. Zerodha request tokens are strictly single-use and expire within minutes.");
    console.log("2. Open this login link in your browser:\n");
    console.log(`   ${kc.getLoginURL()}\n`);
    console.log("3. Log in with your Zerodha ID & TOTP.");
    console.log("4. Copy the 'request_token' from the redirected browser URL, then run:");
    console.log("   npm start -- YOUR_NEW_REQUEST_TOKEN");
    console.log("   (Once generated, your access token is saved and valid for the entire day)\n");
  }
}

async function generateSession() {
  console.log(`Attempting to generate session with request token: ${requestToken.substring(0, 8)}...`);
  const response = await kc.generateSession(requestToken, apiSecret!);
  kc.setAccessToken(response.access_token);
  
  // Persist access token so subsequent runs don't need a new login
  fs.writeFileSync(tokenFilePath, response.access_token, "utf8");
  console.log("✓ Session generated and access_token saved to access_token.txt");
  console.log("Session:", response);
}

async function getProfile() {
  const profile = await kc.getProfile();
  console.log("✓ Profile retrieved successfully:", profile);
}

// Initialize the API calls
init();
