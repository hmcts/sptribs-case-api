# Configuration

This page lists the configuration for `sptribs-case-api`: environment variables, feature flags, Key Vault secret names, identity, Helm values and per-environment overrides. It ends with short notes on the two citizen web apps that call this service. In summary:

- The service is a Spring Boot application on port 4013. Defaults are in [`application.yaml`](../src/main/resources/application.yaml).
- Secrets come from one Key Vault per environment, `sptribs-<env>` (declared as `keyVaults.sptribs` in the Helm chart), populated partly by Terraform in [`sptribs-shared-infrastructure`](https://github.com/hmcts/sptribs-shared-infrastructure).
- S2S name `sptribs_case_api`. Inbound S2S allow-list `S2S_AUTHORISED_SERVICES`. Notify key `uk-gov-notify-api-key` → `UK_GOV_NOTIFY_API_KEY_1` → `uk.gov.notify.api.sptribskey`.
- Letters are sent through GOV.UK Notify's `sendLetter`, not through Send Letter Service.
- Feature flags are plain environment variables (`CITIZEN_DASHBOARD_ENABLED`, `SPRING_JMS_SERVICEBUS_ENABLED`, the `feature.*` task gates and so on). A LaunchDarkly client is built, but no code evaluates a flag.
- Per-environment overrides live in [`cnp-flux-config`](https://github.com/hmcts/cnp-flux-config) under `apps/sptribs/<app>/<env>.yaml`.

## Environment variables and config keys

| Env var / key | Config key it feeds | Default (application.yaml) | Purpose |
|---|---|---|---|
| `server.port` | `server.port` | `4013` | HTTP port. CCD routes decentralised callbacks here |
| `POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_NAME` | `spring.datasource.url` | port `6432`, name `sptribs` | sptribs Postgres (service tables and the `ccd` schema) |
| `POSTGRES_CONNECTION_OPTIONS` | `spring.datasource.url` | — | JDBC options. Blank in preview |
| `spring.flyway.out-of-order` | — | `true` | Flyway allows out-of-order migrations |
| `spring.config.import` | — | `/mnt/secrets/sptribs/` | Mounted Key Vault secrets |
| multipart max size | `spring.servlet.multipart.*` | 500MB | Upload limit for file and request |
| `AZURE_SERVICE_BUS_CONNECTION_STRING` / `AZURE_SERVICE_BUS_PREVIEW_CONNECTION_STRING` | Service Bus connection | `dummy` | Publishes CCD case events for Work Allocation |
| `SPRING_JMS_SERVICEBUS_ENABLED` | `spring.jms.servicebus.enabled` | `true` | Turns Service Bus publishing on or off |
| `CCD_SERVICEBUS_DESTINATION` | `ccd.servicebus.destination` | `ccd-case-events-aat` | Topic. Helm sets `ccd-case-events-{{ .Values.global.environment }}` |
| `SPTRIBS_SERVICEBUS_SCHEDULER_ENABLED` | `ccd.servicebus.*` | `true` | Outbox publisher scheduler |
| `SPTRIBS_SERVICEBUS_BATCH_SIZE` | `ccd.servicebus.*` | `50` | Events per publish batch |
| `SPTRIBS_SERVICEBUS_PUBLISHED_RETENTION_DAYS` | `ccd.servicebus.*` | `7` | Retention of published events |
| `SPTRIBS_SERVICEBUS_SCHEDULE` | `ccd.servicebus.*` | `*/10 * * * * *` | Publisher schedule (every 10 seconds) |
| `UK_GOV_NOTIFY_API_KEY_1` | `uk.gov.notify.api.sptribskey` | `dummy` | Notify API key read by `NotificationsConfiguration` |
| — | `uk.gov.notify.api.baseUrl` | `https://api.notifications.service.gov.uk` | Notify endpoint |
| — | `uk.gov.notify.email.templatesCIC` | template map | Email and letter template map (`_CY` Welsh and `_NEW_CD` dashboard variants). See [notifications](notifications.md) |
| `S2S_URL` | `idam.s2s-auth.url` | AAT `rpe-service-auth-provider` | S2S lease endpoint |
| `S2S_SECRET` | `idam.s2s-auth.secret` | placeholder | S2S TOTP secret |
| — | `idam.s2s-auth.microservice` | `sptribs_case_api` (fixed) | Name the API leases tokens as |
| `S2S_AUTHORISED_SERVICES` | `s2s-authorised.services` | `ccd_data,sptribs_frontend,sptribs_dss_update_case_web` | Inbound S2S allow-list |
| `IDAM_API_BASEURL` | `idam.api.url` | `http://localhost:5000` | IDAM (`hmcts-access` in prod) |
| `IDAM_CLIENT_ID`, `IDAM_CLIENT_SECRET`, `IDAM_CLIENT_REDIRECT_URI` | `idam.client.*` | local placeholders | OAuth2 client |
| `IDAM_SYSTEM_UPDATE_USERNAME`, `IDAM_SYSTEM_UPDATE_PASSWORD` | `idam.systemupdate.*` | local placeholders | System user for scheduled tasks and system events |
| `CASE_DATA_STORE_BASEURL` | `core_case_data.api.url` | AAT `ccd-data-store-api` | CCD data store |
| `CASE_DOCUMENT_AM_BASEURL` | `case_document_am.url` | AAT `ccd-case-document-am-api` | CDAM |
| `DOC_ASSEMBLY_URL` | `doc_assembly.url` | AAT `dg-docassembly` | Docmosis rendering of orders and decisions |
| `BUNDLING_API_URL` | `bundling.api.baseUrl` | AAT `em-ccd-orchestrator` | Bundle stitching |
| `PDF_API_URL` | `pdf.api.url` | `http://localhost:5500` | Correspondence PDFs (`rpe-pdf-service`) |
| `DOCUMENT_MANAGEMENT_URL` | `document_management.url` | AAT `dm-store` | Document store |
| `LOCATION_API_URL`, `JUDICIAL_API_URL` | `location.api.baseUrl`, `judicial.api.baseUrl` | AAT `rd-location-ref-api`, `rd-judicial-api` | Court venues and judicial users (`LocationClient`, `JudicialClient`) |
| `JUDICIAL_USERS_PAGE_SIZE` | — | `200` | Page size for the judicial users request |
| `TASK_MONITOR_API_URL`, `TASK_MANAGEMENT_API_URL` | `wa_task_monitor_api.url`, `wa_task_management_api.url` | AAT `wa-task-monitor`, `wa-task-management-api` | Used by functional-test helpers |
| `SEND_LETTER_SERVICE_BASEURL` | `send-letter.url` | AAT `rpe-send-letter-service` | Present in config. Letters are posted through GOV.UK Notify (`NotificationServiceCIC.sendLetter()` calls `NotificationClient.sendLetter(...)`) |
| `SPTRIBS_FRONTEND_URL` | `sptribs-frontend.dashboard-url` | demo frontend `/dashboard` URL | Citizen dashboard link in `_NEW_CD` emails |
| `SUPPLEMENTARY_DATA_HMCTSID` | `case-flags.supplementary-data.hmctsid` | `BBA2` | HMCTS service ID on supplementary data |
| — | `bank-holidays.api.url` | `https://www.gov.uk/bank-holidays/scotland.json` | Working-day calculations |
| `TASK_NAME` | read by [`CaseApiApplication`](../src/main/java/uk/gov/hmcts/sptribs/CaseApiApplication.java) | unset | When set, runs the named task bean through `ScheduledTaskRunner.run()` and then closes the context. See [scheduled tasks](scheduled-tasks.md) |
| `LAUNCH_DARKLY_SDK_KEY`, `ENVIRONMENT` | `launchDarkly.sdk-key`, `launchDarkly.env` | empty, `local` | LaunchDarkly client |

**Notify key mapping.** Key Vault `uk-gov-notify-api-key` → env var `UK_GOV_NOTIFY_API_KEY_1` → `uk.gov.notify.api.sptribskey` (`${UK_GOV_NOTIFY_API_KEY_1:dummy}`) → [`NotificationsConfiguration`](../src/main/java/uk/gov/hmcts/sptribs/common/config/NotificationsConfiguration.java) builds the `NotificationClient` from it.

## Feature flags

| Flag (env var / key) | Default | Effect |
|---|---|---|
| `CITIZEN_DASHBOARD_ENABLED` → `feature.citizen-dashboard.enabled` | `false` in `application.yaml` and the base Helm values. `true` in preview, AAT, demo, perftest, ithc and prod | Selects `_NEW_CD` templates and injects the dashboard URL |
| `SPRING_JMS_SERVICEBUS_ENABLED` → `spring.jms.servicebus.enabled` | `true` | Service Bus publishing. Preview sets `false` unless the `pr-values:wa` label applies `values.wa.preview.template.yaml` |
| `SPTRIBS_SERVICEBUS_SCHEDULER_ENABLED` | `true` | Publisher scheduler |
| `CLEAN_DELETED_DOCUMENTS_ENABLED`, `CLEAN_DELETED_DOCUMENTS_TEST_CASE_REF` → `feature.clean-deleted-documents-task.*` | `false`, empty | Gate and test-case reference for `SystemCleanDeletedDocumentsTask` |
| `MIGRATE_CASE_DOCUMENTS_TO_TABLE_ENABLED`, `MIGRATE_CASE_DOCUMENTS_TO_TABLE_TEST_CASE_REF` → `feature.migrate-to-documents-table-task.*` | `false`, empty | Gate and test-case reference for `SystemMigrateCaseDocumentsDocumentTableTask`. The task reads its batch size and pause from `feature.migrate-initial-case-documents-task.*` (`INITIAL_DOCUMENTS_MIGRATION_BATCH_SIZE`, default `100`, and `INITIAL_DOCUMENTS_MIGRATION_BATCH_PAUSE`, default `5000` ms) |
| Other `feature.*` gates | `false` unless noted | `REMOVE_FIRST_HEARING_DATE_ENABLED`, `RESTORE_ORDERS_TASK_ENABLED`, `INITIAL_DOCUMENTS_MIGRATION_ENABLED`, `GLOBAL_SEARCH_MIGRATION_ENABLED`, `CREATE_TEST_CASES`, `WA_FUNCTIONAL_TESTS_ENABLED`. `SECURITY_CLASSIFICATION` defaults to `true`, and prod sets `false` |
| LaunchDarkly (`LAUNCH_DARKLY_SDK_KEY`) | — | [`LaunchDarklyConfiguration`](../src/main/java/uk/gov/hmcts/sptribs/common/config/LaunchDarklyConfiguration.java) builds an `LDClient` bean. No code evaluates a flag, so there are no LaunchDarkly flag keys |

## Key Vault secrets

All come from vault `sptribs` in the Helm chart, which resolves to `sptribs-<env>`.

| Secret name | Env var | Purpose |
|---|---|---|
| `app-insights-instrumentation-key` | (property of the same name) | App Insights |
| `app-insights-connection-string` | — | App Insights |
| `uk-gov-notify-api-key` | `UK_GOV_NOTIFY_API_KEY_1` | GOV.UK Notify |
| `s2s-case-api-secret` | `S2S_SECRET` | S2S TOTP secret for `sptribs_case_api` |
| `idam-secret` | `IDAM_CLIENT_SECRET` | IDAM OAuth2 client secret |
| `launchdarkly-sdk-key` | `LAUNCH_DARKLY_SDK_KEY` | LaunchDarkly |
| `idam-systemupdate-username`, `idam-systemupdate-password` | `IDAM_SYSTEM_UPDATE_USERNAME`, `IDAM_SYSTEM_UPDATE_PASSWORD` | System user for scheduled tasks and system events |
| `ccd-case-events-sendonly-connection-string` | `AZURE_SERVICE_BUS_CONNECTION_STRING` | Send-only rule on the CCD case-events topic |
| `sptribs-POSTGRES-HOST`, `-PORT`, `-DATABASE`, `-USER`, `-PASS` | `POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_NAME`, `POSTGRES_USERNAME`, `POSTGRES_PASSWORD` | Database connection |

Preview swaps in the `-preview` App Insights secrets, drops the Service Bus and Postgres secrets, and sets the Postgres connection from template values and a `postgres` Kubernetes secret.

[`Jenkinsfile_CNP`](../Jenkinsfile_CNP) also reads secrets for tests and preview from `s2s-<env>` (`microservicekey-ccd-data`, `-ccd-definition`, `-ccd-gw`, `-ccd-admin`), `ccd-<env>` and `sptribs-<env>`: IDAM test users (solicitor, caseworker, citizen, system update), WA test users, `ccd-importer-username` and `-password`, and the preview Postgres credentials.

For local runs, the Gradle `loadEnvSecrets` task pulls the `sptribs-case-api-dot-env` secret from the `sptribs-aat` vault into `.aat-env`. `loadLocalEnvSecrets` pulls the same secret name from `sptribs-preview` into `.local-env`. See [running locally](running-locally.md).

### Secrets written by Terraform

`module.key-vault` in `sptribs-shared-infrastructure/tf-key-vault.tf` provisions the vault. The vault and its resource group are both named `${product}-${env}`. Terraform writes these secrets:

| Secret | Upstream source |
|---|---|
| `app-insights-instrumentation-key`, `app-insights-connection-string` | `module.application_insights` (`sptribs-appinsights`). Not written in ITHC |
| `app-insights-instrumentation-key-preview`, `app-insights-connection-string-preview` | Preview App Insights module. AAT only |
| `s2s-case-api-secret` | `microservicekey-sptribs-case-api` in vault `s2s-<env>`. Not written in ITHC |
| `ccd-importer-username`, `ccd-importer-password` | Copied from another team's vault. Not written in ITHC |
| `docmosis-access-key` | vault `em-stitching-<env>` |
| `sptribs-POSTGRES-USER`, `sptribs-POSTGRES-PASS` | `module.postgresql` (server `sptribs-<env>-flexible-db-v15`) |
| `ccd-case-events-sendonly-connection-string` | Rule `sptribs-sendonly` (send only) on topic `ccd-case-events-<env>` |

The other secrets the service consumes, such as `uk-gov-notify-api-key`, `idam-secret`, `idam-systemupdate-*`, `launchdarkly-sdk-key` and `sptribs-POSTGRES-HOST`, `-PORT` and `-DATABASE`, are set outside this Terraform.

## Identity

| Item | Value |
|---|---|
| S2S microservice name | `sptribs_case_api` |
| S2S-authorised services (Helm and demo/perftest flux) | `ccd_definition,ccd_data,xui_webapp,sptribs_case_api,ccd_gw,sptribs_frontend,sptribs_dss_update_case_web` |
| S2S-authorised services (`application.yaml` fallback) | `ccd_data,sptribs_frontend,sptribs_dss_update_case_web` |
| IDAM client | `IDAM_CLIENT_ID` (`idam.client.id`) |
| AAD identity (`aadIdentityName`) | `sptribs` |

## Helm chart and values

| File | Role |
|---|---|
| [`charts/sptribs-case-api/values.yaml`](../charts/sptribs-case-api/values.yaml) | Base: port 4013, image `hmctsprod.azurecr.io/sptribs/case-api`, ingress `sptribs-case-api-{{ env }}.service.core-compute-{{ env }}.internal`, spot instances on, CCD, XUI and WA sub-charts `enabled: false` |
| [`charts/sptribs-case-api/values.preview.template.yaml`](../charts/sptribs-case-api/values.preview.template.yaml) | PR deploys: `ccd-idam-pr` tag, in-release CCD stack, database `pr-${CHANGE_ID}-sptribs-case-api` on the shared preview server, CCD `definitionImporter` off (user-profile importer on), Service Bus off, `CITIZEN_DASHBOARD_ENABLED: true`, 1Gi/2Gi memory |
| [`charts/sptribs-case-api/values.aat.template.yaml`](../charts/sptribs-case-api/values.aat.template.yaml) | AAT pipeline stage: sets only `image` and `ingressHost` |
| [`charts/sptribs-case-api/values.wa.preview.template.yaml`](../charts/sptribs-case-api/values.wa.preview.template.yaml) | Adds WA pods and a per-PR Service Bus namespace, and turns Service Bus publishing on. Applied by the PR label `pr-values:wa` |
| flux `apps/sptribs/sptribs-case-api/sptribs-case-api.yaml` | HelmRelease, chart `./stable/sptribs-case-api` from `hmcts-charts`. 2 replicas (max 4), 2Gi/4Gi memory, 1000m/2000m CPU |

## Per-environment overrides (flux)

| Env | Overrides |
|---|---|
| preview | `SPRING_JMS_SERVICEBUS_ENABLED: false` (chart template; `true` with the `pr-values:wa` label) |
| aat | `SPRING_JMS_SERVICEBUS_ENABLED`, `SPTRIBS_SERVICEBUS_SCHEDULER_ENABLED`, `WA_FEATURE_ENABLED` and `CITIZEN_DASHBOARD_ENABLED` all `true`. `SPTRIBS_FRONTEND_URL` |
| demo | `S2S_AUTHORISED_SERVICES`, `WA_FEATURE_ENABLED`, `CITIZEN_DASHBOARD_ENABLED`, `SPTRIBS_FRONTEND_URL`, `TASK_MONITOR_API_URL`, `TASK_MANAGEMENT_API_URL`, `ROLE_ASSIGNMENT_API_URL` |
| perftest | `S2S_AUTHORISED_SERVICES`, `WA_FEATURE_ENABLED`, `CITIZEN_DASHBOARD_ENABLED`, `SPTRIBS_FRONTEND_URL`, `JAVA_TOOL_OPTIONS` (memory, App Insights agent) |
| ithc | `S2S_AUTHORISED_SERVICES`, `WA_FEATURE_ENABLED`, `CITIZEN_DASHBOARD_ENABLED`, `SPTRIBS_FRONTEND_URL` |
| prod | Spot instances off. `IDAM_API_BASEURL` (`hmcts-access`), `NOTIFY_TEMPLATE_SIGN_IN_PROFESSIONAL_USERS_URL`, `SECURITY_CLASSIFICATION: false`, `SPRING_JMS_SERVICEBUS_ENABLED` and `SPTRIBS_SERVICEBUS_SCHEDULER_ENABLED` `true`, `CITIZEN_DASHBOARD_ENABLED: true`, `SPTRIBS_FRONTEND_URL` |

## Cron jobs

Cron jobs run the case-api image with `TASK_NAME` set, through the `sptribs-cron` chart (major 1.x, from `hmctspublic`), and mount a subset of the `sptribs` vault secrets.

| Env | Cron releases deployed |
|---|---|
| prod, perftest, ithc | `sptribs-cron-clear-inactive-dss-draft-cases` |
| demo | clear-inactive-dss-draft-cases, trigger-stitch-hearing-bundle, trigger-complete-hearing-outcome |
| aat | As demo, plus remove-first-hearing-date (`suspend: true` in its base manifest and the `aat/00` overlay; the `aat/01` overlay sets `suspend: false`) |

The other `sptribs-cron-*` directories (migrate-cases, migrate-case-documents-to-doc-table, system-migrate-initial-docs, system-restore-orders, clean-further-documents) are not referenced by any environment. They are retired one-off jobs. See [scheduled tasks](scheduled-tasks.md).

## The citizen web apps

Two Node apps call this service. Their full configuration is in their own repos.

| App | Port | Calls this service for | Config |
|---|---|---|---|
| [`sptribs-frontend`](https://github.com/hmcts/sptribs-frontend) | 4000 | `/cases/cica/...` and document download | `SPTRIBS_CASE_API_URL` → `services.sptribs.url` |
| [`sptribs-dss-update-case-web`](https://github.com/hmcts/sptribs-dss-update-case-web) | 3100 | Access check, subject verification and case updates (`/cases/cica/{caseId}/...`) | `CASE_API_URL` → `services.sptribs.url` |

Both apps lease S2S tokens as `sptribs_case_api` using the `s2s-case-api-secret` secret. Both read secrets from `sptribs-<env>` through their properties-volume module, and both use the same Azure Managed Redis instance for sessions. See [data and persistence](data-and-persistence.md).

## See also

- [Running locally](running-locally.md): local secrets and Gradle tasks
- [Release and deploy](release-and-deploy.md): the pipeline and environments
- [Data and persistence](data-and-persistence.md): the database and its secrets
- [Notifications](notifications.md): Notify templates and keys
