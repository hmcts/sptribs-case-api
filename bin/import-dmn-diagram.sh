#!/usr/bin/env bash

set -eu
workspace=${1}
env=${2}
tenant_id=${3}
product=${4}

microservice=sptribs_case_api
s2sSecret=${S2S_SECRET:-AABBCCDDEEFFGGHH}
oneTimePassword=$(docker run --rm hmctspublic.azurecr.io/imported/toolbelt/oathtool --totp -b ${s2sSecret})

serviceToken=$(curl --insecure --fail --show-error --silent -X POST \
  ${S2S_URL_BASE:-http://rpe-service-auth-provider-aat.service.core-compute-aat.internal}/testing-support/lease \
  -H "Content-Type: application/json" \
  -d '{
    "microservice": "'${microservice}'",
    "oneTimePassword": "'${oneTimePassword}'"
  }')

dmnFilepath="$(realpath $workspace)/src/main/resources/dmn"

for file in $(find ${dmnFilepath} -name '*.dmn')
do
  uploadResponse=$(curl --insecure -v --silent -w "\n%{http_code}" --show-error -X POST \
    ${CAMUNDA_BASE_URL:-http://localhost:9404}/engine-rest/deployment/create \
    -H "Accept: application/json" \
    -H "ServiceAuthorization: Bearer ${serviceToken}" \
    -F "deployment-name=$(basename ${file})" \
    -F "deploy-changed-only=true" \
    -F "deployment-source=$product" \
    ${tenant_id:+'-F' "tenant-id=$tenant_id"} \
    -F "file=@${dmnFilepath}/$(basename ${file})")

upload_http_code=$(echo "$uploadResponse" | tail -n1)
upload_response_content=$(echo "$uploadResponse" | sed '$d')

if [[ "${upload_http_code}" == '200' ]]; then
  echo "$(basename ${file}) diagram uploaded successfully (${upload_response_content})"
  continue;
fi

echo "$(basename ${file}) upload failed with http code ${upload_http_code} and response (${upload_response_content})"
continue;

done
