#!/usr/bin/env bash
IDAM_API_BASE_URL=https://idam-api.aat.platform.hmcts.net
SERVICE_AUTH_PROVIDER_API_BASE_URL=http://rpe-service-auth-provider-aat.service.core-compute-aat.internal
CCD_BASE_URL=http://ccd-data-store-api-aat.service.core-compute-aat.internal
OAUTH2_CLIENT_SECRET=$(az keyvault secret show --vault-name sptribs-aat -o tsv --query value --name idam-secret)
REDIRECT_URI=http://localhost:3001/oauth2/callback

<<USERCONFIG
    Configure users in below format
    read -r -d '' CCD_USERS <<EOM
      TestUser1@test.com|Password
      TestUser2@test.com|Password
    EOM
USERCONFIG


read -r -d '' CCD_USERS <<EOM
EOM

if [ -z "$CCD_USERS" ]
then
      echo "Users are not configured. Please see comments in script file."
      exit
else
      echo "Users configured"
fi

echo "Generating service auth token"
serviceToken=$(curl --insecure --fail --show-error --silent -X POST ${SERVICE_AUTH_PROVIDER_API_BASE_URL}/testing-support/lease -H "Content-Type: application/json" -d '{"microservice": "ccd_data"}')

users=$(echo "${CCD_USERS}" | tr "," "\n")

for user in $users; do
  email=$(echo $user | cut -f1 -d'|')
  password=$(echo $user | cut -f2 -d'|')


  echo "Retrieving user details for user $email"
  userDetails=$(curl --insecure --fail --show-error --silent -X GET -H "Authorization: Bearer $idamToken" "${IDAM_API_BASE_URL}/details")
  firstName=$(echo "$userDetails" | docker run --rm --interactive ghcr.io/jqlang/jq -r .forename)
  lastName=$(echo "$userDetails" | docker run --rm --interactive ghcr.io/jqlang/jq -r .surname)
  userId=$(echo "$userDetails" | docker run --rm --interactive ghcr.io/jqlang/jq -r .id)

done
