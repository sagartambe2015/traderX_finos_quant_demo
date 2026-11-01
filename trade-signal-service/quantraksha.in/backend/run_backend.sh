#!/usr/bin/env bash
# Run the backend from this folder. Delegates to kite-rest-api/run_backend.sh,
# which holds the Java/Maven checks and credential loading.
#
#   ./run_backend.sh          dev mode: mvn spring-boot:run
#   ./run_backend.sh prod     mvn package, then java -jar
#   ./run_backend.sh build    mvn package only
set -euo pipefail

exec "$(cd "$(dirname "$0")" && pwd)/kite-rest-api/run_backend.sh" "$@"
