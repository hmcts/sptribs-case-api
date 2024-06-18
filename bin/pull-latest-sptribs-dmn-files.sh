#!/usr/bin/env bash

branchName=$1

#Checkout specific branch of  camunda bpmn definition
git clone https://github.com/hmcts/st-wa-task-configuration.git
cd st-wa-task-configuration

echo "Switch to ${branchName} branch on st-wa-task-configuration"
git checkout ${branchName}
cd ..

#Copy camunda folder which contains dmn files
cp -r ./st-wa-task-configuration/src/main/resources .
rm -rf ./st-wa-task-configuration

./bin/import-dmn-diagram.sh . sptribs sptribs
