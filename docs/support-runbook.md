# Support runbook

This runbook helps engineers diagnose problems with `sptribs-case-api` in a live environment. It covers where to look, the alert that exists, triage recipes for the common failure areas, how to fix case data and where to escalate. The citizen front ends ([sptribs-frontend](https://github.com/hmcts/sptribs-frontend) and [sptribs-dss-update-case-web](https://github.com/hmcts/sptribs-dss-update-case-web)) appear where a problem crosses into them.

The usual order is: App Insights `sptribs-appinsights`, then pod logs in the `sptribs` namespace, then the `/health` endpoint, then Jenkins.

## Where to look

| Place | What it gives you | How to get there |
|---|---|---|
| App Insights `sptribs-appinsights` | Exceptions, requests, dependency calls, and frontend custom events | Azure portal, product resource group. Created in every environment |
| Pod logs | Application logs (root level `info`) | `kubectl logs -n sptribs deploy/sptribs-case-api-java` |
| Health and actuator | `health` (with details), `info` and `prometheus`, at base path `/` | `curl http://sptribs-case-api-<env>.service.core-compute-<env>.internal/health` (VPN) |
| Flux (cnp-flux-config, `apps/sptribs/`) | HelmReleases, image policies, cron jobs and Logstash per environment | `kubectl get helmrelease,cronjob,pods -n sptribs` |
| Logstash (`sptribs-logstash`) | CCD Elasticsearch indexing from `ccd.es_queue`. Failed events go to the `ccd-logstash-dead-letter` index | Check this first when a case is missing from search or the work basket |
| Jenkins | `Jenkinsfile_CNP` (build and deploy) and `Jenkinsfile_nightly` (weekdays at about 08:00) | Notifications go to `#special-tribunals-builds` |

## Alerts

The alert definitions are in [`tf-app-insights-alerts.tf`](https://github.com/hmcts/sptribs-shared-infrastructure/blob/master/tf-app-insights-alerts.tf) in sptribs-shared-infrastructure:

| Resource | Behaviour |
|---|---|
| Action group `sptribs-ag1` | Created in every environment. Receives the alerts below |
| Exceptions metric alert (`exceptions_alert`) | Fires when the maximum of `performanceCounters/exceptionsPerSecond` is 2 or more, with the Azure default 5-minute window. Created where `custom_alerts_enabled` is true: AAT, demo, perftest and prod |

If the alert fires, open the Exceptions blade in `sptribs-appinsights` for that window and group by problem ID. Then follow the matching recipe below. Dependency failures (Notify, CDAM, Docmosis, bundling, Service Bus) are usually found by triage from exceptions and dependency calls.

## Triage recipes

### A CCD callback error shown to a caseworker

- **Symptoms:** ExUI shows an error on submit or on "about to start" for an event on `CriminalInjuriesCompensation`. CCD routes these callbacks to this service, because the case type is decentralised.
- **Where to look:** App Insights requests and exceptions for `sptribs-case-api` at the time of the error. Check the dependency calls in the same operation, because a downstream failure (CDAM, Docmosis, reference data) often appears as a callback failure.
- **Fix:**
  1. If the exception is in this service, reproduce it in AAT with a test caseworker, then fix and deploy.
  2. If the API is healthy and no request reached it, the problem is between ExUI and CCD. Escalate to the CCD platform team with the case reference, event ID and timestamp.
  3. If the user can't see the event at all, check their role assignments in AM. See [Roles and access](roles-and-access.md).

### A notification was not received

- **Symptoms:** a party says they got no email or letter.
- **Where to look:**
  1. The `case_correspondences` table in the sptribs Postgres database. A row there means GOV.UK Notify accepted the message, and `id` is the Notify notification ID.
  2. App Insights, for exceptions from `NotificationServiceCIC`.
- **Things to know:**
  - `NotificationServiceCIC` logs the error and rethrows it as `NotificationException`. The event's `submitted` callback catches it, so the CCD event still completes, and the caseworker sees a confirmation header saying the notification failed and should be resent.
  - Most events send to each party in turn, so a failure for one party can stop the sends to the parties after it.
  - A send is not retried automatically.
  - The PDF copy is rendered and stored after Notify has accepted the message. If the event reports a failure, the message may still have been sent. Ask the party whether they received it before you resend.
  - Letters go through GOV.UK Notify (`NotificationClient.sendLetter`).
  - Most dispatchers email when the contact preference is `EMAIL` and post otherwise. They need a valid address for the chosen channel.
- **Fix:** correct the contact details or preference on the case, then send the notification again from the relevant event (for example Contact Parties). Check that the Key Vault secret `uk-gov-notify-api-key` (env `UK_GOV_NOTIFY_API_KEY_1`) is present and current. See [Notifications](notifications.md).

### Document or CDAM upload failures

- **Symptoms:** an upload fails in ExUI or the DSS update app, or a citizen dashboard download fails.
- **Where to look:** App Insights dependency calls to `ccd-case-document-am-api` (`case_document_am.url`), and exceptions from `CaseDocumentClientApi` and `DocumentDownloadService`. On the citizen side, sptribs-frontend sorts dashboard failures into timeout, connection_error, rate_limited, unauthorised, 4xx and 5xx events.
- **Fix:** a 403 from CDAM usually means an S2S or case-access problem. Confirm that `sptribs_case_api` is in CDAM's authorised services, then follow the citizen access recipe below. If CDAM is returning 5xx, escalate to the CCD platform team. The API accepts multipart uploads up to 500 MB, so a larger file is rejected before CDAM is called. See [Documents and bundles](documents-and-bundles.md).

### Stitching or bundle failures

- **Symptoms:** the bundle event fails, or the bundle never appears.
- **Where to look:** dependency calls to `em-ccd-orchestrator` (`BUNDLING_API_URL`) from `BundlingService`. `CaseworkerCreateBundle` sets the case's bundle configuration to `st_cic_bundle_all_case.yaml`, and em-ccd-orchestrator loads that file from its own `src/main/resources/bundleconfiguration/`. That copy sets the cover page template and filters folders on `/documentLink/category_id`.
- **Fix:** an unreadable or corrupt PDF in the case makes stitching fail, so find it and remove or replace it. A bundle layout change needs an [em-ccd-orchestrator](https://github.com/hmcts/em-ccd-orchestrator) release. A missing cover page template, or an orchestrator or stitching outage, goes to the Evidence Management team.

In AAT and demo only, the `SystemTriggerStitchCollateHearingBundleTask` cron finds cases in `AwaitingHearing` with a hearing date in the next 14 days and no `stitchHearingBundleTask=Yes`. It submits an event that sets the flag and raises a WA task to collate the bundle. It is not deployed to prod. See [Scheduled tasks](scheduled-tasks.md).

### Docmosis or order generation failures

- **Symptoms:** Issue Decision, Create Order or a draft-order event fails, or produces a blank PDF.
- **Where to look:** dependency calls to `dg-docassembly` (`DOC_ASSEMBLY_URL`) from `DocAssemblyService`. The template names, such as `ST-CIC-DEC-ENG-CIC1_Eligibility`, are in [`application.yaml`](../src/main/resources/application.yaml).
- **Fix:** if a template name in config doesn't match a template deployed to docassembly, the call fails. If a field name in the payload doesn't match the template, the PDF comes out blank or wrong. Generation is synchronous, so a slow render can time out the CCD callback, and a retry may succeed. Templates are in [rdo-docmosis](https://github.com/hmcts/rdo-docmosis). Template or docassembly outages go to the document generation team.

### A citizen can't find their case

- **Symptoms:** the citizen sees "case not found" or "not authorised", or the dashboard won't load.
- **Flow:** `POST /cica-lookup` in sptribs-frontend calls `GET /cases/cica/{ccdReference}/access` on this API. A 200 leads to postcode verification, a 404 offers to start a new application, and a 403 goes to the not-authorised page. The dashboard sends the confirmed postcode in the `X-Postcode` header. Without the header, the API returns 403.
- **Where to look:** the frontend events (`cica_lookup_succeeded` and the failure events, linked by attempt ID and dashboard journey ID) in App Insights. Then this API's logs for `CicaRoleService`.
- **Fix:**
  1. Check that the citizen entered the CCD case reference and that the postcode matches the one held on the case.
  2. `CicaRoleService.ensureCreatorRole` grants `[CREATOR]` through CCD data store's case-assignment API (`addCaseUserRoles`, `getUserRoles`), using the system-update user. It is called from the `/cases/cica/...` services and again after a 404 from CDAM. `[CREATOR]` is a CCD case-user role, not an AM role assignment, so check case users in CCD.
  3. If the system-update user can't get a token, check the Key Vault secrets `idam-systemupdate-username` and `idam-systemupdate-password`, then escalate to the IDAM team.

### Stuck or missing Work Allocation tasks

- **Symptoms:** no task appears after an event, or a task never completes or cancels.
- **How events reach WA:** when an event marked for publishing is submitted, the decentralised runtime inserts a row into the outbox table `ccd.message_queue_candidates` (`message_type = 'CASE_EVENT'`) in the same transaction that saves the case. A scheduler in this service (the SDK's `CcdCaseEventScheduler`) sends pending rows to the `ccd-case-events-<env>` topic. It runs every 10 seconds (`SPTRIBS_SERVICEBUS_SCHEDULE`), sends batches of 50, sets `published` on each row it sends and deletes published rows after 7 days. It runs only when `spring.jms.servicebus.enabled` and `ccd.servicebus.scheduler-enabled` are both true (env `SPRING_JMS_SERVICEBUS_ENABLED` and `SPTRIBS_SERVICEBUS_SCHEDULER_ENABLED`, both `true` by default). The connection string is the secret `ccd-case-events-sendonly-connection-string`.
- **Things to know:** a failed send is not reported to the CCD callback. The row stays unpublished and the next run retries it, so a broker outage delays tasks rather than losing them. A bad connection string or missing topic stops the API from starting, because `CcdServiceBusConnectionValidator` checks the topic at start-up.
- **Where to look:**
  1. Query the outbox in the `ccd` schema of the `sptribs` database. Connection details are in the `sptribs-<env>` Key Vault as `sptribs-POSTGRES-HOST`, `-PORT`, `-DATABASE`, `-USER` and `-PASS`. For one case:

     ```sql
     SELECT id, message_type, time_stamp, published
       FROM ccd.message_queue_candidates
      WHERE reference = 1234567890123456
      ORDER BY id DESC;
     ```

     For everything still waiting to be sent:

     ```sql
     SELECT reference, count(*) AS pending, min(time_stamp) AS oldest
       FROM ccd.message_queue_candidates
      WHERE published IS NULL
      GROUP BY reference
      ORDER BY oldest;
     ```

     No row for the event means it wasn't marked for publishing. A row with `published` null that is more than a minute old means sends are failing. A row with `published` set means the message reached Service Bus.
  2. Look for `Failed to publish message_queue_candidates` and `consecutive message_queue_candidates sends failed` in the logs.
  3. Confirm that neither setting is overridden to `false` for the environment in cnp-flux-config (`apps/sptribs/sptribs-case-api/<env>.yaml`).
  4. Check the DMN rule that should match. The DMNs are in [`src/main/resources/dmn/`](../src/main/resources/dmn). A rule only matches when the case data carries the input it needs, for example the referral type.
- **Fix:** correct the DMN or the case data, then trigger a new event. If the event was published but WA didn't act, escalate to the WA team. If a task exists but nobody can see it, check the users' role assignments, then go to the AM team. See [Work Allocation](work-allocation.md).

### A cron job not running

- **Symptoms:** draft cases aren't cleared, or (in AAT or demo) bundles or hearing outcomes aren't triggered.
- **Where to look:** `kubectl get cronjobs,jobs -n sptribs`, then the logs of the most recent job pod. Each cron is a HelmRelease using the `sptribs-cron` chart, which wraps the HMCTS `job` chart. It runs this service's image with `TASK_NAME` set and reads the `sptribs` Key Vault secrets.
- **CronJob names:** the `job` chart names the CronJob `<releaseName>-job`. For example, the HelmRelease `sptribs-cron-clear-inactive-dss-draft-cases` creates `sptribs-cron-clear-inactive-dss-draft-cases-job`.
- **Active cron cluster:** each environment has two clusters, `00` and `01`. The `job` chart suspends the CronJob on any cluster where `global.activeCronCluster` isn't `true`, and wherever the values set `suspend: true`. A CronJob showing `SUSPEND True` on one cluster is expected. Check the other cluster before treating it as broken.
- **Which crons run where:** see [Scheduled tasks](scheduled-tasks.md). Prod runs only `sptribs-cron-clear-inactive-dss-draft-cases`.
- **Fix:** check whether the CronJob is suspended or missing from the environment's kustomization. To run one now (this also works when it is suspended):

  ```bash
  JOB=manual-clear-dss-$(date +%s)
  kubectl create job --from=cronjob/sptribs-cron-clear-inactive-dss-draft-cases-job "$JOB" -n sptribs
  kubectl logs -n sptribs -f "job/$JOB"
  ```

  A `CcdConflictException` in the WA trigger tasks means another update to a case was running at the same time. The task logs that it is stopping and ends the run. Cases it didn't reach are picked up by the next run, because their flag is still unset. A `CcdManagementException` on one case is logged and the task moves on to the next case.

## Fixing case data

Case data is fixed with system events, run as a `TASK_NAME` task and deployed as a cron in cnp-flux-config.

1. Write a `Runnable` task bean under `systemupdate/schedule/`, or a `Migration` that [`SystemMigrateCasesTask`](../src/main/java/uk/gov/hmcts/sptribs/systemupdate/schedule/migration/SystemMigrateCasesTask.java) runs in priority order (0 runs first). It should search for the affected cases and submit a system event for each. Use `idamService.retrieveSystemUpdateUserDetails()` and `authTokenGenerator.generate()`, as the existing tasks do.
2. Gate it behind a feature flag and a single test case reference, as [`SystemCleanDeletedDocumentsTask`](../src/main/java/uk/gov/hmcts/sptribs/systemupdate/schedule/cleandata/SystemCleanDeletedDocumentsTask.java) does with `feature.clean-deleted-documents-task.enabled` and `feature.clean-deleted-documents-task.caseReference`.
3. Test it locally with `TASK_NAME=<TaskClassName> ./gradlew bootRun`. [`ScheduledTaskRunner`](../src/main/java/uk/gov/hmcts/sptribs/systemupdate/service/ScheduledTaskRunner.java) lower-cases the first letter to find the bean.
4. Add a `sptribs-cron-<name>` directory under `apps/sptribs/` in cnp-flux-config. Reference it from the environment kustomization, AAT first. For a one-off run, set `suspend: true` and start it by hand with `kubectl create job --from=cronjob/sptribs-cron-<name>-job`.
5. After the run, remove it from the kustomization so it doesn't run again.

See [Scheduled tasks](scheduled-tasks.md) for the full recipe.

## Verify

- `curl -s http://sptribs-case-api-<env>.service.core-compute-<env>.internal/health` returns `"status":"UP"` (VPN required).
- In `sptribs-appinsights`, the exception rate is back below the alert threshold, and `kubectl get jobs -n sptribs` shows the latest cron job as `Complete`.

## Escalation routes

| Problem area | Escalate to |
|---|---|
| CCD data store, definition import, CDAM, Elasticsearch indexing, case-assignment API | CCD platform team |
| Task creation, DMN deployment, task visibility | Work Allocation team |
| Role assignments and access profiles | Access Management team |
| Login, system-update user tokens, OAuth clients | IDAM team |
| AKS, Flux, Key Vault access, networking, Service Bus namespace | Platform Operations |
| Bundling and stitching, Docmosis rendering | Evidence Management and document generation teams |

Include the environment, case reference, event ID, the App Insights operation ID and the time.

## See also

- [Architecture](architecture.md)
- [Release and deploy](release-and-deploy.md)
- [Scheduled tasks](scheduled-tasks.md)
- [Data and persistence](data-and-persistence.md)
