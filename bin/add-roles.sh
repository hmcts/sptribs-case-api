#!/usr/bin/env bash

scriptPath=$(dirname $(realpath $0))

# Roles used during the CCD import
${scriptPath}/add-ccd-role.sh "caseworker-divorce-superuser"
${scriptPath}/add-ccd-role.sh "caseworker-sptribs-superuser"
${scriptPath}/add-ccd-role.sh "caseworker-divorce-solicitor"
${scriptPath}/add-ccd-role.sh "caseworker-divorce-systemupdate"
${scriptPath}/add-ccd-role.sh "payments"
${scriptPath}/add-ccd-role.sh "caseworker-st_cic"
${scriptPath}/add-ccd-role.sh "caseworker-sptribs-cic-courtadmin"
${scriptPath}/add-ccd-role.sh "caseworker-sptribs-cic-caseofficer"
${scriptPath}/add-ccd-role.sh "caseworker-sptribs-cic-districtregistrar"
${scriptPath}/add-ccd-role.sh "caseworker-sptribs-cic-districtjudge"
${scriptPath}/add-ccd-role.sh "caseworker-sptribs-cic-respondent"
${scriptPath}/add-ccd-role.sh "citizen-sptribs-cic-dss"
