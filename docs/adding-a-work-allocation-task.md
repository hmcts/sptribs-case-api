# Adding a work allocation task

On master, a Work Allocation (WA) task is defined entirely by six Camunda DMN files in [`src/main/resources/dmn/`](../src/main/resources/dmn/) (`wa-task-<kind>-st_cic-criminalinjuriescompensation.dmn`). No Java code creates tasks. WA evaluates the DMNs against the CCD case events that this service publishes to the `ccd-case-events-<env>` Service Bus topic. This page is the recipe for adding a task. For background, see [work-allocation.md](work-allocation.md).

In summary:

- A new task needs a row in **initiation**, **types** and **completion**, its task type added to the relevant **configuration** and **permissions** rows, and a check of **cancellation**.
- Only events marked `.publishToCamunda()` are published. If a DMN input is missing from the published data, no rule matches and no error is raised.
- The DMNs have unit tests in [`src/test/java/uk/gov/hmcts/sptribs/dmn/`](../src/test/java/uk/gov/hmcts/sptribs/dmn/), run by `./gradlew test`.
- To test on a PR, add the `pr-values:wa` label. Add `pr-values:wa-ft-tests` as well to run the WA functional tests.

> Draft PR [#2467](https://github.com/hmcts/sptribs-case-api/pull/2467) proposes moving task handling from DMN rules to calls made from the event handlers. It is not merged. This page describes master. If the PR has merged, check this recipe against it.

## 1. Edit the DMNs

Each file is a decision table for `st_cic` / `criminalinjuriescompensation`. The columns below are those on master: the input variable or expression and the output `name`, with the column label in brackets where it differs.

| File | Hit policy | Inputs | Outputs | What a new task needs |
|---|---|---|---|---|
| `wa-task-types-…dmn` | COLLECT | one unnamed input, always empty | `taskTypeId`, `taskTypeName` | One new row with the task type code and display name |
| `wa-task-initiation-…dmn` | COLLECT | `eventId`, `postEventState`, `referralType`, `adminActionRequired`, `firstOrderDueDate` (label "Has Order Due Date") | `taskId`, `name`, `delayUntil`, `workingDaysAllowed`, `processCategories`, `workType`, `roleCategory` | One row per event and state combination that should create the task |
| `wa-task-configuration-…dmn` | RULE ORDER | `caseData`, `taskType` | `name`, `value`, `canReconfigure` | Add the task type to the `taskType` list of the existing rows that apply (for example `description`, `workType`, `roleCategory`, `dueDateIntervalDays`), or add a row for a value specific to the task |
| `wa-task-permissions-…dmn` | RULE ORDER | `taskType`, `case` (label "Case Data") | `caseAccessCategory`, `name`, `value`, `roleCategory`, `authorisations`, `assignmentPriority`, `autoAssignable` | Add the task type to the `taskType` list of each role row that should see, claim or complete it. The first row (empty task type, `task-supervisor`) applies to every task |
| `wa-task-completion-…dmn` | COLLECT | `eventId` (label "Event ID") | `taskType`, `completionMode` | One row per event that completes the task, usually with `completionMode` `"Auto"` |
| `wa-task-cancellation-…dmn` | COLLECT | `fromState`, `event`, `state` (label "To State") | `action`, `warningCode`, `warningText`, `processCategories` | Usually nothing. Rows target process categories, not task types, so give the task a `processCategories` value in its initiation row that the right cancellation rows already cover. The `caseworker-close-the-case` row has no process category, so it cancels every open task |

The configuration and permissions tables are not one row per task. In both, a single `taskType` input cell holds a comma-separated list of task types, such as `"processCaseWithdrawalDirections","processRule27Decision",…`.

Steps:

1. Pick a `taskId` in lowerCamelCase that matches the existing ones, for example `processCaseWithdrawalDirections`.
2. Add the task type to the **types** table.
3. Add the **initiation** row. Leave inputs that don't apply empty (any value). The `referralType`, `adminActionRequired` and `firstOrderDueDate` inputs are FEEL expressions over `additionalData.Data.cicCaseReferralTypeForWA`, `cicCaseAdminActionRequired` and `cicCaseFirstOrderDueDate`. The hit policy is COLLECT, so one event can create several tasks.
4. Add the new task type to the **configuration** and **permissions** rows used by an existing task with the same role category and work type.
5. Add **completion** rows for the events that finish the task. Check the **cancellation** rows against the task's `processCategories`, and check that no existing row would complete or cancel it unexpectedly. The three lifecycle tables are independent of each other.
6. Update the DMN unit tests (see step 3). They assert the exact number of rules in each table, so they fail until you update the count.

The existing initiation rule below is the model to copy. It creates a withdrawal-directions task when a draft order is created for a withdrawal request.

| Input / output | Value |
|---|---|
| `eventId` | `"create-draft-order"` |
| `postEventState` | `"CaseManagement", "ReadyToList"` |
| `referralType` | `"Withdrawal request"` |
| `adminActionRequired` | (empty) |
| `firstOrderDueDate` | (empty) |
| `taskId` | `"processCaseWithdrawalDirections"` |
| `name` | `"Process case withdrawal directions"` |
| `delayUntil` | (empty) |
| `workingDaysAllowed` | `7` |
| `processCategories` | `"Processing"` |
| `workType` | `"routine_work"` |
| `roleCategory` | `"ADMIN"` |

This is the same rule in `wa-task-initiation-st_cic-criminalinjuriescompensation.dmn` (rule `DecisionRule_0in26ix`, the first in the table). Give a new rule and its entries fresh unique ids. Camunda Modeler does this for you.

```xml
<rule id="DecisionRule_0in26ix">
  <inputEntry id="UnaryTests_1mtpy0d">
    <text>"create-draft-order"</text>
  </inputEntry>
  <inputEntry id="UnaryTests_0pzaled">
    <text>"CaseManagement", "ReadyToList"</text>
  </inputEntry>
  <inputEntry id="UnaryTests_1dz8lch">
    <text>"Withdrawal request"</text>
  </inputEntry>
  <inputEntry id="UnaryTests_058t87a">
    <text></text>
  </inputEntry>
  <inputEntry id="UnaryTests_1a7i989">
    <text></text>
  </inputEntry>
  <outputEntry id="LiteralExpression_0yp6gfj">
    <text>"processCaseWithdrawalDirections"</text>
  </outputEntry>
  <outputEntry id="LiteralExpression_11crul1">
    <text>"Process case withdrawal directions"</text>
  </outputEntry>
  <outputEntry id="LiteralExpression_0j4vwvw">
    <text></text>
  </outputEntry>
  <outputEntry id="LiteralExpression_0h32jop">
    <text>7</text>
  </outputEntry>
  <outputEntry id="LiteralExpression_1p20qkn">
    <text>"Processing"</text>
  </outputEntry>
  <outputEntry id="LiteralExpression_0te3ddw">
    <text>"routine_work"</text>
  </outputEntry>
  <outputEntry id="LiteralExpression_19ua141">
    <text>"ADMIN"</text>
  </outputEntry>
</rule>
```

## 2. Check the triggering event and Service Bus publishing

WA only sees what this service publishes, so check the event end to end.

1. **The event exists and ends in the state you used.** The `eventId` must be the CCD event id. `postEventState` is the state the case is in after the event, as declared in the event's config class. See [events-reference.md](events-reference.md).
2. **The event is published.** Publishing is opt-in per event. The event's config class must call `.publishToCamunda()` on its event builder, as `CaseworkerCreateDraftOrder` and 27 other event classes on master do. The generator then writes `Publish = Y` on the event and its fields, and the SDK's decentralised runtime (`MessagePublisher`) skips any event without that flag.
3. **The published data carries every DMN input.** The initiation inputs read `additionalData.Data.<field>`, which holds only the event's published fields. If a field is missing, no rule matches and no error is raised. For example, the `referralType` input reads `cicCaseReferralTypeForWA`. The refer-to-judge and refer-to-legal-officer events set it, and the `create-draft-order` page (`CreateDraftOrder`) lists it as a `NEVER_SHOW` field so that it is on the event's field list. Put any new DMN input on the triggering event in the same way.
4. **Publishing is on.** `caseEventServiceBus = true` in the `ccd { }` block of [`build.gradle`](../build.gradle) makes the service publish case events. The `ccd.servicebus.*` keys in [`application.yaml`](../src/main/resources/application.yaml) set the topic (`CCD_SERVICEBUS_DESTINATION`: `ccd-case-events-<env>` in `values.yaml`, `${SERVICE_NAME}-asb-ccd-case-events` in preview), a scheduler that runs every 10 seconds (`SPTRIBS_SERVICEBUS_SCHEDULE`) and a batch size of 50 (`SPTRIBS_SERVICEBUS_BATCH_SIZE`). Publishing is not inline with the callback, so allow a few seconds before the task appears.
5. **For a time-based task**, the event may need a system trigger. The jobs in [`systemupdate/schedule/wa`](../src/main/java/uk/gov/hmcts/sptribs/systemupdate/schedule/wa/) (for example `SystemTriggerStitchCollateHearingBundleTask`) submit system events that WA rules listen for. Follow the same pattern if no caseworker event fits. See [scheduled-tasks.md](scheduled-tasks.md).

If you are adding a new event as well, see [adding-an-event.md](adding-an-event.md).

## 3. Test the DMNs locally

The DMNs have unit tests that evaluate them in the Camunda DMN engine, with no Camunda server.

- [`DmnDecisionTableBaseUnitTest`](../src/test/java/uk/gov/hmcts/sptribs/DmnDecisionTableBaseUnitTest.java) parses the table named in the `DmnDecisionTable` enum and evaluates it with a map of variables.
- [`src/test/java/uk/gov/hmcts/sptribs/dmn/`](../src/test/java/uk/gov/hmcts/sptribs/dmn/) has one test class per table: `CamundaTaskTypesTest`, `CamundaTaskWaInitiationTest`, `CamundaTaskWaConfigurationTest`, `CamundaTaskWaPermissionTest`, `CamundaTaskCompletionTest` and `CamundaTaskWaCancellationTest`. Helpers are in `src/test/java/uk/gov/hmcts/sptribs/dmnutils/`.
- Each class asserts the table's rule count (63 initiation, 67 types, 45 configuration, 15 permissions, 98 completion and 5 cancellation rules on master), so update the count when you add a rule.

Add parameterised cases for the new task to each affected test class, then run:

```bash
./gradlew test --tests 'uk.gov.hmcts.sptribs.dmn.*'
```

The cftlib stack (`bootWithCCD` / `cftlibTest`) does not include WA, so end-to-end task creation needs a WA environment. The [`bin/wa/`](../bin/wa/) scripts deploy to one:

| Script | Use |
|---|---|
| `bin/wa/import-dmn-diagram.sh <workspace> <environment> <tenant_id> <product>` | Upload every `*.dmn` in `<workspace>/src/main/resources/dmn` to `${CAMUNDA_BASE_URL}/engine-rest/deployment/create` (default: AAT's `camunda-api`), with `deploy-changed-only=true`. The pipeline passes `st_cic` for both tenant and product. It leases an S2S token as `sptribs_case_api` from `S2S_SECRET` (`S2S_SECRET_PROD` when the environment is `prod`) and needs Docker for `oathtool` |
| `bin/wa/deploy-demo-dmn.sh` | Wraps `import-dmn-diagram.sh` for Demo: reads `s2s-case-api-secret` from the `sptribs-demo` vault and targets `camunda-api-demo` |
| `bin/wa/pull-latest-camunda-wa-files.sh <branch>` | Clone [wa-standalone-task-bpmn](https://github.com/hmcts/wa-standalone-task-bpmn) at that branch and copy its `src/main/resources/` into the current directory |
| `bin/wa/import-wa-bpmn-diagram.sh <workspace>` | Upload every `*.bpmn` under `<workspace>/resources` to `${CAMUNDA_BASE_URL}` (default `http://localhost:9404`) |
| `bin/wa/setup-role-assignments.sh` | Create org role mappings for the users in `aat-caseworker-user-ids.json` through the PR's `am-org-role-mapping-service` testing-support endpoint (needs `CHANGE_ID`) |

A PR preview (step 4) is the first shared environment where the DMNs run against real case events.

## 4. Deploy to preview

Preview runs without WA by default, because [`values.preview.template.yaml`](../charts/sptribs-case-api/values.preview.template.yaml) sets `SPRING_JMS_SERVICEBUS_ENABLED: false`.

1. Open a PR against master.
2. Add the `pr-values:wa` label. The PR is then deployed with WA pods, using [`values.wa.preview.template.yaml`](../charts/sptribs-case-api/values.wa.preview.template.yaml), which sets `SPRING_JMS_SERVICEBUS_ENABLED: true`. After `akschartsinstall`, `Jenkinsfile_CNP` uploads the DMNs and the BPMN to the PR's Camunda and runs `setup-role-assignments.sh`.
3. Add the `pr-values:wa-ft-tests` label as well if you want the pipeline to run the WA functional tests. It sets `WA_FUNCTIONAL_TESTS_ENABLED`, and only works together with `pr-values:wa`.
4. Optionally add `enable_keep_helm` so the preview environment stays up after the pipeline finishes, and you can test the task by hand in ExUI.

All three labels are listed in the [README](../README.md).

## 5. Run the WA functional tests

The WA functional tests are in [`src/functionalTest/java/uk/gov/hmcts/sptribs/wa/`](../src/functionalTest/java/uk/gov/hmcts/sptribs/wa/), one class per task (for example `WAProcessCaseWithdrawalDirectionsFT`). They are in the `functionalTest` source set, which the `functional` Gradle task runs (`**/*FT.class`). Each test is annotated `@EnabledIfEnvironmentVariable(named = "WA_FUNCTIONAL_TESTS_ENABLED", matches = "true")`.

A typical test creates and submits a case, runs the caseworker events that lead to the trigger state, sets the DMN inputs in the case data (for example `cicCaseReferralTypeForWA`), then submits the triggering event. It then polls with Awaitility (every 4 seconds, for up to 300 seconds) through the test helper `TaskManagementService`. It searches for tasks of the expected type on the case, and checks that each role returned by the task's role permissions is in the expected list.

The `pr-values:wa-ft-tests` label runs them in the PR pipeline. To run them yourself:

```bash
WA_FUNCTIONAL_TESTS_ENABLED=true ./gradlew functional
```

- `WA_FUNCTIONAL_TESTS_ENABLED=true` turns on the WA tests and raises the number of parallel forks to the number of processors.
- [`application-functional.yaml`](../src/functionalTest/resources/application-functional.yaml) defaults IDAM, S2S, role assignment and CDAM to AAT, so you need the VPN. `TEST_URL` defaults to `http://localhost:4013`, `FT_POSTGRES_HOST` to `localhost`, and the task-management URL to AAT's `wa-task-management-api`. Set these for the environment you are testing.
- The tests use the WA IDAM users from Key Vault: `wa-seniorCaseworker-username` / `-password` and `wa-regionalHearingCentreTeamLead-username` / `-password`. Before it runs, `functional` loads environment variables through `loadEnvFile()` from `.local-env`, or from `.aat-env` if there is no `.local-env`. `functional` does not create `.aat-env` itself. The `loadEnvSecrets` task (which `bootRun` depends on) writes it from the `sptribs-case-api-dot-env` secret in the `sptribs-aat` vault.

Add a functional test for the new task in `src/functionalTest/java/uk/gov/hmcts/sptribs/wa/`, modelled on an existing one. Event payloads are in [`src/functionalTest/resources/wa/`](../src/functionalTest/resources/wa/). See [testing.md](testing.md) for the other test suites.

## 6. Deploy to AAT, prod, demo, perftest and ithc

[`Jenkinsfile_CNP`](../Jenkinsfile_CNP) calls `uploadDmnDiagrams()` (which runs `./wa/import-dmn-diagram.sh ${WORKSPACE} ${env.ENVIRONMENT} st_cic st_cic`) and `uploadBpmnDiagram()` in these places:

1. **AAT**: before `functionalTest:aat` on the master build, against `camunda-api-aat`.
2. **Prod**: after `functionalTest:aat` succeeds, against `camunda-api-prod`, with `S2S_SECRET_PROD` read from the `sptribs-prod` vault.
3. **Preview**: after `akschartsinstall` on a PR with the `pr-values:wa` label (step 4).
4. **Demo, perftest and ithc**: `syncBranchesWithMaster(['demo', 'perftest', 'ithc'])` keeps those branches in step with master, and on those branches the pipeline uploads after `checkout`.

To push DMNs to Demo by hand, for example to test a change before it merges, run `bin/wa/deploy-demo-dmn.sh`. See [release-and-deploy.md](release-and-deploy.md) for the pipeline as a whole.

## Verify

1. On the preview environment (with `pr-values:wa` and `enable_keep_helm`), sign in to ExUI as a WA caseworker. Trigger the event on a case in the right state, wait at least 10 seconds for the Service Bus scheduler, then open **My work** or **Available tasks**. The new task should appear with the name from the initiation row.
2. Trigger the completing or cancelling event and check that the task leaves the list.
3. Run `WA_FUNCTIONAL_TESTS_ENABLED=true ./gradlew functional`, or let the `pr-values:wa-ft-tests` label run it, and check that the WA tests pass.

## See also

- [Work allocation](work-allocation.md): how events reach WA, the six DMNs and the deployment routes
- [Roles and access](roles-and-access.md): the roles that work on tasks
- [`src/main/resources/dmn/`](../src/main/resources/dmn/): the DMN files
- [`bin/wa/`](../bin/wa/): DMN, BPMN and role-assignment scripts
