#!/usr/bin/env bash

set -eu

environment_argument="${1:-}"
ccd_environment="${environment_argument:-${CCD_ENVIRONMENT:-${ENVIRONMENT:-dev}}}"
ccd_environment=$(echo "${ccd_environment}" | tr '[:upper:]' '[:lower:]')
force_environment="${environment_argument:+true}"

set_env_if_needed() {
  local key=${1}
  local value=${2}
  local force=${3}
  local current="${!key-}"

  if [ "${force}" = "true" ] || [ -z "${current}" ]; then
    export "${key}=${value}"
  fi
}

apply_environment_defaults() {
  local environment_name=${1}
  local force=${2:-false}

  case "${environment_name}" in
    prod)
      set_env_if_needed ENVIRONMENT "prod" "${force}"
      set_env_if_needed CCD_DEF_NAME "prod" "${force}"
      set_env_if_needed CASE_API_URL "http://sptribs-case-api-prod.service.core-compute-prod.internal" "${force}"
      set_env_if_needed S2S_URL_BASE "http://rpe-service-auth-provider-prod.service.core-compute-prod.internal" "${force}"
      set_env_if_needed SERVICE_AUTH_PROVIDER_URL "http://rpe-service-auth-provider-prod.service.core-compute-prod.internal" "${force}"
      set_env_if_needed IDAM_API_URL_BASE "https://idam-api.platform.hmcts.net" "${force}"
      set_env_if_needed CAMUNDA_BASE_URL "http://camunda-api-prod.service.core-compute-prod.internal" "${force}"
      set_env_if_needed CASE_API_DOCUMENT_DOWNLOAD_URL "http://manage-case.platform.hmcts.net/" "${force}"
      set_env_if_needed DEFINITION_STORE_URL_BASE "http://ccd-definition-store-api-prod.service.core-compute-prod.internal" "${force}"
      ;;
    aat)
      set_env_if_needed ENVIRONMENT "aat" "${force}"
      set_env_if_needed CCD_DEF_NAME "aat" "${force}"
      set_env_if_needed CASE_API_URL "http://sptribs-case-api-aat.service.core-compute-aat.internal" "${force}"
      set_env_if_needed S2S_URL_BASE "http://rpe-service-auth-provider-aat.service.core-compute-aat.internal" "${force}"
      set_env_if_needed SERVICE_AUTH_PROVIDER_URL "http://rpe-service-auth-provider-aat.service.core-compute-aat.internal" "${force}"
      set_env_if_needed IDAM_API_URL_BASE "https://idam-api.aat.platform.hmcts.net" "${force}"
      set_env_if_needed FEE_API_URL "http://fees-register-api-aat.service.core-compute-aat.internal" "${force}"
      set_env_if_needed CAMUNDA_BASE_URL "http://camunda-api-aat.service.core-compute-aat.internal" "${force}"
      ;;
    demo)
      set_env_if_needed ENVIRONMENT "demo" "${force}"
      set_env_if_needed CCD_DEF_NAME "demo" "${force}"
      set_env_if_needed CASE_API_URL "http://sptribs-case-api-demo.service.core-compute-demo.internal" "${force}"
      set_env_if_needed S2S_URL_BASE "http://rpe-service-auth-provider-demo.service.core-compute-demo.internal" "${force}"
      set_env_if_needed IDAM_API_URL_BASE "https://idam-api.demo.platform.hmcts.net" "${force}"
      set_env_if_needed CAMUNDA_BASE_URL "http://camunda-api-demo.service.core-compute-demo.internal" "${force}"
      set_env_if_needed CASE_API_DOCUMENT_DOWNLOAD_URL "http://manage-case.demo.platform.hmcts.net/" "${force}"
      ;;
    perftest)
      set_env_if_needed ENVIRONMENT "perftest" "${force}"
      set_env_if_needed CCD_DEF_NAME "perftest" "${force}"
      set_env_if_needed CASE_API_URL "http://sptribs-case-api-perftest.service.core-compute-perftest.internal" "${force}"
      set_env_if_needed S2S_URL_BASE "http://rpe-service-auth-provider-perftest.service.core-compute-perftest.internal" "${force}"
      set_env_if_needed IDAM_API_URL_BASE "https://idam-api.perftest.platform.hmcts.net" "${force}"
      set_env_if_needed FEE_API_URL "http://fees-register-api-demo.service.core-compute-demo.internal" "${force}"
      set_env_if_needed CAMUNDA_BASE_URL "http://camunda-api-perftest.service.core-compute-perftest.internal" "${force}"
      ;;
    ithc)
      set_env_if_needed ENVIRONMENT "ithc" "${force}"
      set_env_if_needed CCD_DEF_NAME "ithc" "${force}"
      set_env_if_needed CASE_API_URL "http://sptribs-case-api-ithc.service.core-compute-ithc.internal" "${force}"
      set_env_if_needed S2S_URL_BASE "http://rpe-service-auth-provider-ithc.service.core-compute-ithc.internal" "${force}"
      set_env_if_needed IDAM_API_URL_BASE "https://idam-api.ithc.platform.hmcts.net" "${force}"
      set_env_if_needed FEE_API_URL "http://fees-register-api-demo.service.core-compute-ithc.internal" "${force}"
      set_env_if_needed CAMUNDA_BASE_URL "http://camunda-api-ithc.service.core-compute-ithc.internal" "${force}"
      ;;
    *)
      set_env_if_needed ENVIRONMENT "${environment_name}" "${force}"
      set_env_if_needed CCD_DEF_NAME "${environment_name}" "${force}"
      set_env_if_needed CASE_API_URL "http://localhost:4013" "${force}"
      set_env_if_needed S2S_URL_BASE "aat" "${force}"
      ;;
  esac
}

apply_environment_defaults "${ccd_environment}" "${force_environment:-false}"

scriptPath=$(dirname $(realpath $0))
echo "Script Path ${scriptPath}"
echo "Using CCD environment ${ENVIRONMENT} with CASE_API_URL ${CASE_API_URL}"

root_dir=$(realpath $(dirname ${0})/..)
build_dir=${root_dir}/build/ccd-config

mkdir -p ${build_dir}

docker logout hmctspublic.azurecr.io

for dir in $(find ${root_dir}/build/definitions/ -maxdepth 1 -mindepth  1 -type d -exec basename {} \;)
do
  config_dir=${root_dir}/build/definitions/${dir}
  definitionOutputFile=${build_dir}/ccd-${dir}-${CCD_DEF_NAME:-dev}.xlsx

  (${scriptPath}/generate-ccd-definition.sh $config_dir $definitionOutputFile "-e *-nonprod.json,*-testing.json") &
done

wait
