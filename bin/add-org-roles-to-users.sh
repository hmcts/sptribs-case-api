#!/usr/bin/env bash

# Setup Users
echo ""
echo "Setting up WA Users and role assignments..."
./bin/utils/organisational-role-assignment.sh "${IDAM_SOLICITOR_USERNAME}" "${IDAM_SOLICITOR_PASSWORD}" "PUBLIC" "case-allocator" '{"jurisdiction":"ST_CIC","primaryLocation":"765324"}'
./bin/utils/organisational-role-assignment.sh "${IDAM_SYSTEM_UPDATE_USERNAME}" "${IDAM_SYSTEM_UPDATE_PASSWORD}" "PUBLIC" "task-supervisor" '{"jurisdiction":"ST_CIC","primaryLocation":"765324"}'
./bin/utils/organisational-role-assignment.sh "${IDAM_SOLICITOR1_USERNAME}" "${IDAM_SOLICITOR1_PASSWORD}" "PUBLIC" "tribunal-caseworker" '{"jurisdiction":"ST_CIC","primaryLocation":"765324"}'
