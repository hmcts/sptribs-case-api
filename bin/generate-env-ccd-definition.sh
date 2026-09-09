#!/usr/bin/env bash

set -eu

ENV=${1:-dev}

SCRIPT_DIR=$(dirname "$(realpath "$0")")
REPO_ROOT=$(realpath "$SCRIPT_DIR/..")
ENV_FILE="$SCRIPT_DIR/local-ccd-config-env/target-env.${ENV}"

if [[ -f "$ENV_FILE" ]]; then
  source "$ENV_FILE"
fi

cd "$REPO_ROOT"

echo "Generating CCD config for environment: ${ENV}"

./gradlew generateCCDConfig --rerun-tasks

echo "Generating CCD definition (xlsx)"
./bin/ccd-build-definition.sh

echo "Done - output in build/ccd-config"
