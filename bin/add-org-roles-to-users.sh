#!/usr/bin/env bash

# Setup Users
echo ""
echo "Setting up WA Users and role assignments..."
./bin/utils/organisational-role-assignment.sh "${CIC_CASEOFFICER_USERNAME}" "${CIC_CASEOFFICER_PASSWORD}" "PUBLIC" "case-allocator" '{"jurisdiction":"EMPLOYMENT","primaryLocation":"765324"}'
./bin/utils/organisational-role-assignment.sh "${CIC_CASEOFFICER_USERNAME}" "${CIC_CASEOFFICER_PASSWORD}" "PUBLIC" "task-supervisor" '{"jurisdiction":"EMPLOYMENT","primaryLocation":"765324"}'
./bin/utils/organisational-role-assignment.sh "${CIC_CASEOFFICER_USERNAME}" "${CIC_CASEOFFICER_PASSWORD}" "PUBLIC" "tribunal-caseworker" '{"jurisdiction":"EMPLOYMENT","primaryLocation":"765324"}'
