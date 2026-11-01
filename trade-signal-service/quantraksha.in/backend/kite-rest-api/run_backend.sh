#!/usr/bin/env bash
# Run the Kite REST API (Spring Boot) on 127.0.0.1:4100.
#
#   ./run_backend.sh          dev mode: mvn spring-boot:run
#   ./run_backend.sh prod     mvn package, then java -jar target/kite-rest-api-<version>.jar
#   ./run_backend.sh build    mvn package only
#
# Credentials come from the environment, else from ./.env, else from the
# dashboard's ../../frontend/zerodha-nodejs/.env. The access token is shared
# with the Node relay by default, so one daily login serves both.
set -euo pipefail

cd "$(dirname "$0")"

mode="${1:-dev}"
case "$mode" in
  dev|prod|build) ;;
  *) echo "usage: $0 [dev|prod|build]" >&2; exit 1 ;;
esac

dashboard_dir="../../frontend/zerodha-nodejs"

# Java >= 17 and Maven are required.
if ! command -v java >/dev/null 2>&1; then
  echo "error: java is not installed (need >= 17)" >&2
  exit 1
fi
java_major="$(java -version 2>&1 | awk -F'"' '/version/ {split($2, v, "."); print (v[1] == "1" ? v[2] : v[1]); exit}')"
if [[ -z "$java_major" || "$java_major" -lt 17 ]]; then
  echo "error: java ${java_major:-unknown} found, need >= 17" >&2
  exit 1
fi
if ! command -v mvn >/dev/null 2>&1; then
  echo "error: mvn is not installed" >&2
  exit 1
fi

# Credentials: only fill variables that aren't already set in the environment.
load_env_var() {
  local var="$1" file value
  [[ -n "${!var:-}" ]] && return 0
  for file in .env "$dashboard_dir/.env"; do
    [[ -f "$file" ]] || continue
    value="$(grep -E "^${var}=" "$file" | tail -n 1 | cut -d= -f2- | sed -E 's/^["'\'']//; s/["'\'']$//' || true)"
    if [[ -n "$value" ]]; then
      export "$var=$value"
      return 0
    fi
  done
}

if [[ "$mode" != build ]]; then
  for var in KITE_API_KEY KITE_API_SECRET; do
    load_env_var "$var"
    if [[ -z "${!var:-}" ]]; then
      echo "error: ${var} is not set (export it, or add it to ./.env or $dashboard_dir/.env)" >&2
      exit 1
    fi
  done
  for var in KITE_TOKEN_FILE HOST PORT UI_URL CORS_ORIGINS; do
    load_env_var "$var"
  done

  # Share the Node relay's daily login unless told otherwise.
  if [[ -z "${KITE_TOKEN_FILE:-}" && -d "$dashboard_dir" ]]; then
    export KITE_TOKEN_FILE="$(cd "$dashboard_dir" && pwd)/access_token.txt"
  fi
fi

banner() {
  echo "Kite REST API: http://${HOST:-127.0.0.1}:${PORT:-4100}  (token file: ${KITE_TOKEN_FILE:-access_token.txt})"
}

case "$mode" in
  dev)
    banner
    exec mvn spring-boot:run
    ;;
  build|prod)
    mvn -q -DskipTests package
    jar="$(ls target/kite-rest-api-*.jar | grep -v '\.original$' | head -n 1)"
    echo "Built $jar"
    if [[ "$mode" == prod ]]; then
      banner
      exec java -jar "$jar"
    fi
    ;;
esac
