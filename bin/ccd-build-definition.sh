#!/usr/bin/env bash

set -eu

scriptPath=$(dirname $(realpath $0))
echo "Script Path ${scriptPath}"

root_dir=$(realpath $(dirname ${0})/..)
build_dir=${root_dir}/build/ccd-config

mkdir -p ${build_dir}

azure_config_dir="/opt/jenkins/.azure-${DEPLOYMENT_ENVIRONMENT:-aat}"
[[ -d "${azure_config_dir}" ]] || azure_config_dir="/opt/jenkins/.azure-aat"

if [[ -d "${azure_config_dir}" ]]; then
  # Use environment's azure config
  env AZURE_CONFIG_DIR="${azure_config_dir}" az acr login --name hmctsprod
else
  # No environment config set (e.g. local dev), use default.
  az acr login --name hmctsprod
fi

for dir in $(find ${root_dir}/build/definitions/ -maxdepth 1 -mindepth  1 -type d -exec basename {} \;)
do
  config_dir=${root_dir}/build/definitions/${dir}
  definitionOutputFile=${build_dir}/ccd-${dir}-${CCD_DEF_NAME:-dev}.xlsx

   if [[ ! -e ${definitionOutputFile} ]]; then
   touch ${definitionOutputFile}
   fi

  (${scriptPath}/generate-ccd-definition.sh $config_dir $definitionOutputFile "-e *-nonprod.json,*-testing.json") &
done

wait
