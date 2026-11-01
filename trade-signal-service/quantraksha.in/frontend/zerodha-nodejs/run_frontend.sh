#!/usr/bin/env bash
# Run the Kite live dashboard (relay + web UI).
#
#   ./run_frontend.sh            dev mode: relay on 127.0.0.1:4000 + Vite UI on :5173 (hot reload)
#   ./run_frontend.sh prod       build web/ and serve everything from the relay on :4000
#   ./run_frontend.sh login [request_token]   CLI login script (test.ts)
set -euo pipefail

cd "$(dirname "$0")"

mode="${1:-dev}"
case "$mode" in
  dev|prod|login) ;;
  *) echo "usage: $0 [dev|prod|login [request_token]]" >&2; exit 1 ;;
esac

# Node >= 20.6 is required for --env-file.
if ! command -v node >/dev/null 2>&1; then
  echo "error: node is not installed (need >= 20.6)" >&2
  exit 1
fi
node -e '
  const [maj, min] = process.versions.node.split(".").map(Number);
  if (maj < 20 || (maj === 20 && min < 6)) {
    console.error(`error: node ${process.versions.node} found, need >= 20.6`);
    process.exit(1);
  }'

# Credentials.
if [[ ! -f .env ]]; then
  cp .env.example .env
  echo "Created .env from .env.example. Fill in KITE_API_KEY and KITE_API_SECRET, then re-run." >&2
  exit 1
fi
for var in KITE_API_KEY KITE_API_SECRET; do
  if ! grep -qE "^${var}=.+" .env; then
    echo "error: ${var} is empty in .env" >&2
    exit 1
  fi
done

# Dependencies: install when node_modules is missing or older than the lockfile.
install_if_needed() {
  local dir="$1"
  if [[ ! -d "$dir/node_modules" || "$dir/package-lock.json" -nt "$dir/node_modules" ]]; then
    echo "Installing dependencies in $dir ..."
    npm --prefix "$dir" install
    touch "$dir/node_modules"
  fi
}
install_if_needed .
install_if_needed web

case "$mode" in
  dev)
    echo "Dashboard: http://localhost:5173  (relay on http://127.0.0.1:4000)"
    exec npm run dev
    ;;
  prod)
    npm run build
    echo "Dashboard: http://127.0.0.1:4000"
    exec npm run serve
    ;;
  login)
    shift
    exec npm start -- "$@"
    ;;
esac
