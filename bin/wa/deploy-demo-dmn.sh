#!/usr/bin/env bash

#Local convenience wrapper for demo deployments of DMN files for WA
#Wraps the pipeline deploy script with demo specific values

SCRIPT_DIR=$(dirname "$(realpath "$0")")
REPO_ROOT=$(realpath "$SCRIPT_DIR/..")
ENV="demo"
TENANT_ID="st_cic"
PRODUCT="st_cic"

export ENVIRONMENT="$ENV"
export CAMUNDA_BASE_URL="http://camunda-api-demo.service.core-compute-demo.internal"
export S2S_SECRET=$(az keyvault secret show \
  --vault-name sptribs-demo \
  --name s2s-case-api-secret \
  --query value -o tsv)

bash "$REPO_ROOT/bin/wa/import-dmn-diagram.sh" \
  "$REPO_ROOT" \
  "$ENV" \
  "$TENANT_ID" \
  "$PRODUCT"
