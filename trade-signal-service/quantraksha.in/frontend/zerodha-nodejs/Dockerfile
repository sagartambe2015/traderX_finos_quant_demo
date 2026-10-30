# Kite live dashboard: Node relay (server/) serving the built React UI (web/) on one port.

# ---- build the web UI ----
FROM node:22-alpine AS web
WORKDIR /app
COPY web/package.json web/package-lock.json ./web/
RUN npm --prefix web ci
COPY shared ./shared
COPY web ./web
RUN npm --prefix web run build

# ---- runtime ----
FROM node:22-alpine
WORKDIR /app
ENV NODE_ENV=production \
    HOST=0.0.0.0 \
    PORT=4000 \
    DATA_DIR=/app/data

COPY package.json package-lock.json ./
RUN npm ci --omit=dev && npm cache clean --force

COPY tsconfig.json ./
COPY server ./server
COPY shared ./shared
COPY --from=web /app/web/dist ./web/dist

# Runtime state (access token, watchlist, portfolio journal) lives on a volume
RUN mkdir -p /app/data && chown -R node:node /app/data
VOLUME /app/data
USER node

EXPOSE 4000
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
  CMD wget -qO- "http://127.0.0.1:${PORT}/healthz" >/dev/null || exit 1

# Env comes from the container (Dokploy "Environment"), not a .env file
CMD ["node_modules/.bin/tsx", "server/index.ts"]
