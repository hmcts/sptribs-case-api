# Your first week on sptribs-case-api

## TL;DR

- A five-day guided path for a developer joining Special Tribunals: get access, run the case API and the citizen app, make one small definition change, and open a PR.
- Day 2 and Day 3 are hands-on: `./gradlew bootWithCCD` in `sptribs-case-api` (XUI Manage Cases on `http://localhost:3000`, signing in through AAT IDAM, so the VPN is needed), then `yarn start:dev` in `sptribs-frontend` (`http://localhost:3001`).
- Day 4 adds a read-only field to the `caseworker-case-built` event page, regenerates the definition with `./gradlew generateCCDConfig`, and runs `./gradlew test` and `./gradlew integration`.
- Day 5 opens a PR with the `enable_ccd_diff` label and checks the change in the PR's preview environment. A merge to `master` goes to AAT and then to prod with no manual approval step.
- Each day links to the guide that holds the full commands. This page only shows the commands you need to follow along.

## Before you start

This tutorial follows one path: CIC (`CriminalInjuriesCompensation`, jurisdiction `ST_CIC`) on `master` of `sptribs-case-api`. The SEND35 (`StSend35`) and WPAF1 (`StWpaf1`) case types are on prototype branches of `sptribs-case-api`, not on `master`. Leave them until after this week.

Have these installed before Day 2: Java 21, Node and Yarn, Docker and the Azure CLI. The [Running locally](running-locally.md#prerequisites) prerequisites table explains why each is needed.

If a term on this page is new to you, look it up in the [Glossary](glossary.md).

## Day 1: access and orientation

Today you get the access that the rest of the week needs, and build a mental model of the service.

1. **GitHub.** Make sure your GitHub account is a member of the `hmcts` organisation and that you can clone `hmcts/sptribs-case-api`, `hmcts/sptribs-frontend`, `hmcts/sptribs-dss-update-case-web` and `hmcts/sptribs-e2etests`.
2. **Azure and Key Vault.** Ask to join the **DTS Special Tribunals** AAD group. [`tf-key-vault.tf`](https://github.com/hmcts/sptribs-shared-infrastructure/blob/master/tf-key-vault.tf) in `sptribs-shared-infrastructure` sets it as the `product_group_name` of the `sptribs-<env>` Key Vaults, which is how the group gets access to them. The local tasks read secrets from `sptribs-aat`, and from `sptribs-preview` for `loadLocalEnvSecrets`. You don't need access to `s2s-aat`: the same Terraform copies the S2S secret the apps use, `s2s-case-api-secret`, into `sptribs-<env>`.
   Then sign in and check the subscription:

   ```bash
   az login
   az account show
   az keyvault secret list --vault-name sptribs-aat --query "[].name" -o tsv | head
   ```

   The last command lists secret names only. If it fails with "forbidden", your group membership hasn't taken effect yet.
3. **VPN.** Everything in AAT (`*.service.core-compute-aat.internal`, AAT IDAM, Key Vault) needs the F5 VPN. Connect to it before you go further, and check that an AAT hostname resolves.
4. **Jenkins.** Sign in to the CFT Jenkins (`https://build.hmcts.net`) and open the `HMCTS_j_to_z` folder, which holds the sptribs pipelines (the case-api `Jenkinsfile_CNP` triggers `HMCTS_j_to_z/sptribs-e2etests/master`). Find `sptribs-case-api`, `sptribs-frontend` and `sptribs-e2etests`. Look at the most recent `master` build of each, and at the weekday nightly runs (`H 8 * * 1-5`).
5. **Slack.** Join `#special-tribunals-builds`. The case-api and e2etests pipelines (PR, `master` and nightly) and the infrastructure pipeline post there.
6. **Read.** Read [Architecture](architecture.md), then [Case lifecycle](case-lifecycle.md). While you read, note the answers to these questions:
   - Which repo owns the case definition and the callbacks? (`sptribs-case-api`)
   - Why does CCD call `sptribs-case-api` for every `CriminalInjuriesCompensation` event? (It is registered as decentralised.)
   - Which apps do citizens use, and which app do caseworkers and judges use?

### What you've learned

- Which accounts and groups you need, and how to check Key Vault access without reading a secret value.
- That AAT is behind the VPN, and where build results are posted.
- The shape of the service: one Spring Boot case API, two Node citizen apps, ExUI for staff, and CCD in the middle.

## Day 2: run the case API locally and move a case through events

Today you run the whole CFT stack on your machine and act as a caseworker. On `master`, `bootWithCCD` signs in through AAT IDAM and S2S, so connect the VPN and run `az login` first. Full detail is in [Running locally, Option A](running-locally.md#option-a-full-stack-with-cftlib).

1. Start Docker, then start the stack:

   ```bash
   cd sptribs-case-api
   ./gradlew bootWithCCD
   ```

   `bootWithCCD` starts CCD, Elasticsearch, Postgres and XUI in the same JVM as the case API. It depends on `loadEnvSecrets`, which writes `.aat-env` from the `sptribs-aat` vault. (`loadLocalEnvSecrets` writes `.local-env` from `sptribs-preview`, and `.local-env` wins whenever it exists.) On start-up `CftLibConfig` generates the definitions into `build/definitions/` and imports `build/definitions/CriminalInjuriesCompensation`. The first run takes a while.
2. Check the API is up:

   ```bash
   curl http://localhost:4013/health
   ```

3. Open XUI Manage Cases at `http://localhost:3000` (port 3003 is XUI Manage Org) and sign in with an AAT IDAM account. [`CftLibConfig`](../src/cftlib/java/uk/gov/hmcts/sptribs/cftlib/CftLibConfig.java) gives a test caseworker and a test solicitor a CCD user profile for `ST_CIC`, and [`cftlib-am-role-assignments.json`](../src/main/resources/cftlib-am-role-assignments.json) gives the test caseworker its role assignments, so sign in as that caseworker. cftlib doesn't create IDAM users in AAT mode, so the account must exist in AAT IDAM with roles such as `caseworker-st_cic-caseworker`. If it doesn't, ask your team how to get an AAT test caseworker account.
4. Create a case: choose **Create case**, then jurisdiction `ST_CIC`, case type `CriminalInjuriesCompensation` and the **Create Case** event (`caseworker-create-case`, in `common/event/CreateCase.java`), and fill in the pages. The event's initial state is `Draft`, but its `aboutToSubmit` callback runs `SubmissionService`, whose `SetStateAfterSubmission` task sets the state to `Submitted`. So the new case lands in `Submitted`.
5. Open the case and use **Next step** to run a few events, checking the **History** tab after each:
   - **Case: Build case** (`caseworker-case-built`) moves the case from `Submitted` to `CaseManagement`.
   - **Hearings: Create listing** (`caseworker-record-listing`) runs from `CaseManagement`, `ReadyToList` or `AwaitingOutcome`, and moves the case to `AwaitingHearing`.
6. Find the code behind what you just did. Open `caseworker/event/CaseworkerCaseBuilt.java` and the matching constant in `caseworker/util/EventConstants.java`. Look up both states in `ciccase/model/State.java`. Notice that the state transition, the event name and the role grants are all Java.

### What you've learned

- How to run the full stack locally with cftlib, and where XUI and the API listen.
- That every event you click in XUI is a Spring bean in `sptribs-case-api`, and that CCD calls back to it on port 4013.
- How to read an event class: from-state, to-state, grants and callbacks.

## Day 3: run sptribs-frontend and submit a DSS application

Today you take the citizen's side: submit an application in `sptribs-frontend` against your local stack, and find the case in your local XUI. Full detail is in [Running locally, the citizen apps](running-locally.md#the-citizen-apps) and the [`sptribs-frontend` README](https://github.com/hmcts/sptribs-frontend#readme).

1. Leave `bootWithCCD` running from Day 2. In a second terminal, install and start the frontend:

   ```bash
   cd sptribs-frontend
   yarn install
   yarn start:dev
   ```

2. You don't need to export anything. In development mode, `src/main/modules/properties-volume/index.ts` reads `idam-ui-secret`, `s2s-case-api-secret`, `postcode-lookup-token` and `specialTribunals-cic-pcq-token` from `sptribs-aat` with `az`, so `az login` is enough. `config/development.yaml` already points the app at your cftlib stack: the case API on `http://localhost:4013`, the CCD data store on `http://localhost:4452` and CDAM on `http://localhost:4455`. (To use AAT instead, you would set `SPTRIBS_CASE_API_URL`, `CCD_API_URL` and `CASE_DOCUMENT_API_URL`.)
3. Open `http://localhost:3001`. The dev server is plain HTTP on port 3001. Sign-in redirects to AAT IDAM. Sign in as an AAT user with the `citizen` role. Your local `bootWithCCD` also authenticates against AAT IDAM, so the local CCD accepts the citizen's token and the case is created in your local stack.
4. Work through the application. The page order is defined in `src/main/steps/edge-case/edgecaseSequence.ts`. As you go, notice what happens in CCD:
   - Submitting the subject's contact details page, while there is no case yet, fires `citizen-cic-create-dss-application` (see `getEventName` in `src/main/app/controller/PostController.ts`). `CicCreateCaseEvent` creates the case in `DSS_Draft`.
   - The contact details, CICA reference number, CICA decision date and representation pages fire `citizen-cic-update-dss-application`.
   - Submitting the check your answers page fires `citizen-cic-submit-dss-application`, which moves the case from `DSS_Draft` to `DSS_Submitted`.
5. Note the HMCTS number from the confirmation page. In your local XUI at `http://localhost:3000`, sign in as a caseworker and search for it. Open the **History** tab and match each entry to a step in the journey.

### What you've learned

- How the citizen app creates and updates a case through CCD events, not by writing data directly.
- That citizen DSS events live in `citizen/event/` in `sptribs-case-api`, with their IDs under `caseinfo.apps` in `application.yaml`.
- How a citizen's case becomes visible to caseworkers in XUI.

## Day 4: add a field to an existing caseworker event page

Today you make the smallest useful change: show the subject's full name on the page of the `caseworker-case-built` event you ran on Day 2. Read [Adding an event](adding-an-event.md) first. Today you only use its steps 3, 7 and 8.

1. Create a branch from an up-to-date `master`:

   ```bash
   cd sptribs-case-api
   git fetch origin && git checkout -b <your-initials>/practice-field origin/master
   ```

2. Open `caseworker/event/CaseworkerCaseBuilt.java`. Its single page, `caseBuilt`, is built inline with `PageBuilder`. Larger events keep their pages in separate classes under `caseworker/event/page/`, with the same builder calls. For example, `ReinstateReasonSelect` adds fields from the `CicCase` complex like this:

   ```java
   // src/main/java/uk/gov/hmcts/sptribs/caseworker/event/page/ReinstateReasonSelect.java
   pageBuilder
       .page("reinstateReason")
       .pageLabel("Reason for reinstatement")
       .label("LabelReinstateCaseReason", "")
       .complex(CaseData::getCicCase)
       .mandatory(CicCase::getReinstateReason)
       .optional(CicCase::getReinstateAdditionalDetail)
       .done();
   ```

3. Add the subject's full name to the `caseBuilt` page as a read-only field. `fullName` is an existing `CicCase` field (label "Subject's full name"). `CaseData` unwraps `CicCase` with the prefix `cicCase`, so the CCD field ID is `cicCaseFullName`. Change the page to:

   ```java
   new PageBuilder(eventBuilder)
       .page("caseBuilt")
       .pageLabel("Case Built")
       .label("LabelCaseBuilt", "")
       .complex(CaseData::getCicCase)
       .readonly(CicCase::getFullName)
       .done();
   ```

   Add `import uk.gov.hmcts.sptribs.ciccase.model.CicCase;` to the imports.
4. Regenerate the definition and check your field is there:

   ```bash
   ./gradlew generateCCDConfig
   grep -rl cicCaseFullName build/definitions/CriminalInjuriesCompensation
   ```

   The task starts the app in the `config-gen` profile, with Flyway and JMS turned off and an in-memory H2 database in place of Postgres. It writes the definition JSON to `build/definitions/CriminalInjuriesCompensation/`. (`build.gradle` first sets `ccd.configDir` to `ccd-definitions/definitions`, but a later `ccd { configDir = file('build/definitions') }` block overrides it.) Check that `cicCaseFullName` now appears among the `caseworker-case-built` event's fields.
5. Restart `bootWithCCD`. `CftLibConfig` regenerates and re-imports the definition on start-up. Create a new case, because your Day 2 case is no longer in `Submitted`. Run **Case: Build case** on it and check the subject's name appears on the page.
6. Run the tests that CI runs on every PR:

   ```bash
   ./gradlew test
   ./gradlew integration
   ```

   `integration` uses testcontainers Postgres, so Docker must be running. If it fails with a Docker API version error, see [the testcontainers fix](running-locally.md#testcontainers-fails-with-a-docker-api-version-error). The tests for this event are `src/test/java/uk/gov/hmcts/sptribs/caseworker/event/CaseWorkerCaseBuiltTest.java` (note the capital `W`) and `src/integrationTest/java/uk/gov/hmcts/sptribs/caseworker/event/CaseworkerCaseBuiltIT.java`. If a test asserts the page's fields, update it.

### What you've learned

- That the CCD definition is generated from Java. You never edit definition JSON or spreadsheets by hand.
- How to regenerate the definition and check the output before you open a PR.
- Which test tasks CI runs (`check` and `integration` in `.github/workflows/ci.yml`), and how to run them locally.

## Day 5: open a PR and test it in preview

Today you put the change through the same route as any real change, and then read about what happens after the merge.

1. Push the branch and open a PR against `master`.
2. Add the **`enable_ccd_diff`** label. `.github/workflows/ccd-diff.yml` runs `generateCCDConfig` on both your branch and `master`, compares the two `build/definitions/CriminalInjuriesCompensation` outputs, and posts a diff summary as a PR comment. Check that the comment shows your field and nothing else.
3. Add **`enable_keep_helm`** so the preview deployment stays up after the pipeline finishes. You can also add **`enable-e2e-tests`**: `Jenkinsfile_CNP` then triggers the `sptribs-e2etests` master job against the PR's XUI before the preview functional tests, and a failure there fails the build.
4. Wait for Jenkins to deploy the PR to preview. A case-api preview gets its own CCD stack. The chart's definition importer is off (`values.preview.template.yaml`); the pipeline's High Level Data Setup stage imports your generated definition into the PR's definition store instead. Sign in to the preview ExUI as a caseworker, run the event and check your field.
5. Read [Release and deploy](release-and-deploy.md) to see what happens after a merge. The `master` build deploys to AAT, runs the smoke and functional tests, imports the prod definition and publishes a `prod-` image tag. Flux image automation then rolls that tag out to prod and the other environments. There is no manual approval step, so a merge that passes AAT reaches prod. Read the [Support runbook](support-runbook.md) for monitoring, alerts and common incidents.
6. This was a practice change. Close the PR without merging, unless your team has agreed to take it.

### What you've learned

- The PR labels that drive preview on `sptribs-case-api`, and how to review a definition change from the diff comment.
- That every case-api PR gets its own CCD in preview, so you can test definition changes before anyone merges them.
- How a merged change reaches AAT and then prod with no manual gate, and where to look when the live service needs help.

## After this week

- [Case lifecycle](case-lifecycle.md) and [Events reference](events-reference.md): every state and event, and who can run each one.
- [Adding an event](adding-an-event.md): the full recipe, including callbacks, notifications and WA tasks.
- [Testing](testing.md#browser-end-to-end-tests-sptribs-e2etests): the unit, integration and functional suites, and the Playwright suite in [`sptribs-e2etests`](https://github.com/hmcts/sptribs-e2etests).
- [Adding a case type](adding-a-case-type.md): how SEND35, WPAF1 and future jurisdictions sit alongside CIC.

## See also

- [Running locally](running-locally.md): every local option, and the known problems
- [Release and deploy](release-and-deploy.md): PR labels, pipelines and Flux promotion
- [Glossary](glossary.md): the terms used on this page
- [`CaseworkerCaseBuilt.java`](../src/main/java/uk/gov/hmcts/sptribs/caseworker/event/CaseworkerCaseBuilt.java): the simple event used on Day 2
