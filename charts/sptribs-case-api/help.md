[2024-06-13T14:38:15.671Z] Error: 4 errors occurred:
[2024-06-13T14:38:15.671Z] 	* Deployment.apps "sptribs-case-api-pr-1800-ccd-definition-store" is invalid: spec.template.spec.containers[0].env[1].valueFrom.secretKeyRef.name: Invalid value: "": a lowercase RFC 1123 subdomain must consist of lower case alphanumeric characters, '-' or '.', and must start and end with an alphanumeric character (e.g. 'example.com', regex used for validation is '[a-z0-9]([-a-z0-9]*[a-z0-9])?(\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*')

[2024-06-13T14:38:15.671Z] 	* Deployment.apps "sptribs-case-api-pr-1800-am-role-assignment-service" is invalid: spec.template.spec.containers[0].env[1].valueFrom.secretKeyRef.name: Invalid value: "": a lowercase RFC 1123 subdomain must consist of lower case alphanumeric characters, '-' or '.', and must start and end with an alphanumeric character (e.g. 'example.com', regex used for validation is '[a-z0-9]([-a-z0-9]*[a-z0-9])?(\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*')

[2024-06-13T14:38:15.672Z] 	* Deployment.apps "sptribs-case-api-pr-1800-ccd-user-profile-api" is invalid: spec.template.spec.containers[0].env[1].valueFrom.secretKeyRef.name: Invalid value: "": a lowercase RFC 1123 subdomain must consist of lower case alphanumeric characters, '-' or '.', and must start and end with an alphanumeric character (e.g. 'example.com', regex used for validation is '[a-z0-9]([-a-z0-9]*[a-z0-9])?(\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*')

[2024-06-13T14:38:15.673Z] 	* Deployment.apps "sptribs-case-api-pr-1800-ccd-data-store-api" is invalid: spec.template.spec.containers[0].env[1].valueFrom.secretKeyRef.name: Invalid value: "": a lowercase RFC 1123 subdomain must consist of lower case alphanumeric characters, '-' or '.', and must start and end with an alphanumeric character (e.g. 'example.com', regex used for validation is '[a-z0-9]([-a-z0-9]*[a-z0-9])?(\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*')

Then
[2024-06-21T13:27:24.964Z] NAME                                                              READY   STATUS             RESTARTS       AGE
[2024-06-21T13:27:24.964Z] sptribs-case-api-pr-1808-am-role-assignment-service-c747ffsr7c2   0/1     CrashLoopBackOff   6 (102s ago)   8m20s
[2024-06-21T13:27:24.966Z] sptribs-case-api-pr-1808-ccd-data-store-api-7fc9bd85d7-hpx6h      0/1     CrashLoopBackOff   6 (63s ago)    8m20s
[2024-06-21T13:27:24.966Z] sptribs-case-api-pr-1808-ccd-definition-store-5b6dc578cd-jcjrc    0/1     CrashLoopBackOff   6 (93s ago)    8m20s
[2024-06-21T13:27:24.966Z] sptribs-case-api-pr-1808-ccd-user-profile-api-7fb85864d-ddnwg     0/1     CrashLoopBackOff   6 (2m5s ago)   8m20s

kgp | grep 1808
sptribs-case-api-pr-1808-am-role-assignment-service-db9bfdj6lx9   0/1     CrashLoopBackOff   5 (2m30s ago)   6m9s - updated
sptribs-case-api-pr-1808-ccd-definition-store-8f969fdc9-dqb49     0/1     CrashLoopBackOff   5 (107s ago)    6m9s
sptribs-case-api-pr-1808-ccd-user-profile-api-7fb85864d-96k6k     0/1     CrashLoopBackOff   5 (2m17s ago)   6m9s

definition store
Message    : FATAL: password authentication failed for user "****"

User Profile
Connection to localhost:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.

kgp | grep 1808
sptribs-case-api-pr-1808-am-role-assignment-service-7bbb9c9hdqk   0/1     Error              3 (38s ago)   88s
Caused by: org.postgresql.util.PSQLException: FATAL: database "role-assignment" does not exist

sptribs-case-api-pr-1808-ccd-user-profile-api-7fb85864d-2ll6s     0/1     CrashLoopBackOff   3 (24s ago)   88s
Caused by: org.postgresql.util.PSQLException: Connection to localhost:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.

sptribs-case-api-pr-1808-ccd-user-profile-api-7fb85864d-4258d     0/1     CrashLoopBackOff   5 (39s ago)     4m30s
[main] ERROR com.zaxxer.hikari.pool.HikariPool - HikariPool-1 - Exception during pool initialization.
org.postgresql.util.PSQLException: Connection to localhost:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.

Deployed now
Tests?




