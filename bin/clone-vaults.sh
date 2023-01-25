#!/bin/bash

# Set source and target environments
sourceEnv="aat"
targetEnv="demo"

# Set source key vault name and subscription
sourceKvName="sptribs-$sourceEnv"
sourceSubscription="DCD-CNP-DEV"

# Set target key vault name and subscription
targetKvName="sptribs-$targetEnv"
targetSubscription="DCD-CNP-DEV"

# Get secrets from source key vault
sourceSecrets=$(az keyvault secret list --vault-name $sourceKvName --subscription $sourceSubscription --query "[].{id:id,name:name}" -o json | jq -c '.[]')

# Iterate through source secrets and get their values
for secret in $sourceSecrets
do
    echo "Getting value for $(echo $secret | jq -r '.name')"
    val=$(az keyvault secret show --id $(echo $secret | jq -r '.id') --subscription $sourceSubscription -o json | jq -c '.')
    secret="$secret $(echo $val | jq -r '.value')"
done

# Create/update secrets in target key vault
for secret in $sourceSecrets
do
    echo "Creating/updating secret $(echo $secret | jq -r '.name') in $targetEnv key vault"
    az keyvault secret set --name $(echo $secret | jq -r '.name') --vault-name $targetKvName --subscription $targetSubscription --value $(echo $secret | jq -r '.value')
done
