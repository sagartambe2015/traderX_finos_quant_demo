import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// In dev, the Node relay (server/index.ts) runs on :4000; proxy API and WebSocket calls to it.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      "/api": "http://127.0.0.1:4000",
      "/ws": { target: "ws://127.0.0.1:4000", ws: true },
    },
  },
});
