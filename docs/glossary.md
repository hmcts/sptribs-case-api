# Glossary

## aboutToSubmit
A CCD callback. CCD calls the case API with it after the user submits an event but before the event is recorded. It is the service's chance to validate data, change the case state, run side effects or build follow-up content. In this service, nearly every event has an `aboutToSubmit` that runs business logic such as setting the case state or generating orders. See [Architecture](architecture.md).

## Access profile
A CCD-level grant used to enforce field, tab, state and event authorisations. Access profiles are declared in `CICAccessProfile`, which maps IDAM roles and AM organisational roles to CCD read and write permissions. See [Roles and access](roles-and-access.md).

## CDAM
Case Document Access Management. The service (`ccd-case-document-am-api`) that gates document storage and retrieval for CCD. This service uploads and downloads documents through CDAM. See [Documents and bundles](documents-and-bundles.md).

## CIC
Criminal Injuries Compensation. The case type this service manages (CCD case type `CriminalInjuriesCompensation`, jurisdiction `ST_CIC`). Citizens appeal compensation decisions to the First-tier Tribunal. See [Architecture](architecture.md).

## CICA
Criminal Injuries Compensation Authority. The respondent on every CIC case. CICA users are assigned the AM role `cica` (`RAS_ST_CICA`), which grants them the `CIC_RESPONDENT` access profile. See [Roles and access](roles-and-access.md).

## CicCase
The main nested object in `CaseData` for the CIC case type. It holds the subject, representative, hearing and outcome data. `CaseData` unwraps it with the prefix `cicCase`, so the `fullName` field becomes the CCD field `cicCaseFullName`. See [Data and persistence](data-and-persistence.md).

## [CREATOR]
The CCD case role that gives a citizen access to the case they started. `CicaRoleService.ensureCreatorRole()` adds it through CCD's case-users API after the case is created. See [Roles and access](roles-and-access.md).

## CriminalInjuriesCompensationData
The case-data wrapper class for the CIC case type. On `master`, only the case-type bean and `CICAccessProfile` use it; most other CIC configuration is still declared on `CaseData`. On the `send35-prototype` and `wpaf1-prototype` branches, every CIC config is declared on this class, and each new case type has its own wrapper. See [Adding a case type](adding-a-case-type.md).

## Decentralised CCD
An architecture where a service owns a CCD case type, its definition and its callbacks, and stores the case data itself rather than in CCD's own database. CCD routes every callback for that case type to the service over HTTP. `decentralised = true` in `build.gradle` turns this on. This service is decentralised, so CCD sends every `CriminalInjuriesCompensation` callback to port 4013. See [Architecture](architecture.md#what-decentralised-ccd-means-here).

## DMN
Decision Model and Notation. XML files that encode rules as decision tables. This service holds DMN files in `src/main/resources/dmn/` that Work Allocation uses to decide which tasks to create, cancel or complete. See [Work allocation](work-allocation.md).

## DSS
The prefix used for the citizen journeys and their states. Citizens submit a case through `sptribs-frontend` and add documents to it later through `sptribs-dss-update-case-web`. DSS states (`DSS_Draft`, `DSS_Submitted`, `DSS_Expired`) are separate from the tribunal states that caseworkers use. `DSS` is not expanded in the code and appears only in app, state and event names. See [Case lifecycle](case-lifecycle.md).

## Flux clusters 00 and 01
Each Flux environment has two AKS clusters, `00` and `01`. Both build on a shared `apps/sptribs/<env>/base` overlay in `cnp-flux-config`, which applies the environment's Helm value overrides. See [Release and deploy](release-and-deploy.md).

## High Level Data Setup
The Jenkins pipeline stage that imports the generated CCD definition and the CCD roles it needs into an environment's definition store. It runs `HighLevelDataSetupApp` through the `highLevelDataSetup` Gradle task. In preview, it imports the PR's definition into the PR's own CCD. See [Release and deploy](release-and-deploy.md#4-ccd-definition-import).

## HMCTS service code
A four-character code assigned to every HMCTS service. CIC's service code is `BBA2`. It lives in `CcdServiceCode` and is set as the `hmctsid` supplementary data in `application.yaml`. See [Configuration](configuration.md).

## Outbox
A table pattern for reliable message publishing. The case API inserts published events into `ccd.message_queue_candidates` in the same transaction as the case update, then a scheduler sends the rows to Service Bus. If the publish fails, the row stays in the outbox and is retried. See [Work allocation](work-allocation.md#the-outbox).

## Post-event state
The state of a case after an event completes. Some events declare their to-state in configuration; others set it in the `aboutToSubmit` callback. The Work Allocation DMN rules use the post-event state to decide which tasks to create. See [Case lifecycle](case-lifecycle.md).

## Preview
A short-lived Kubernetes deployment that Jenkins creates for each PR. A case API preview gets its own CCD stack, its own database and its own hostnames for the case API and citizen apps. See [Release and deploy](release-and-deploy.md#1-raise-the-pr-and-use-preview).

## Publish to Camunda
A flag on events that marks them for publication to Work Allocation. `publishToCamunda()` on an event config tells the CCD SDK to insert a row into the outbox when the event is submitted. Only events with this flag reach Work Allocation. See [Work allocation](work-allocation.md).

## ST_CIC
The CCD jurisdiction ID for Special Tribunals Criminal Injuries Compensation. All case types on `master` sit in this jurisdiction. It is also the prefix of the IDAM roles (`caseworker-st_cic-caseworker`, `caseworker-st_cic-judge` and so on). See [Roles and access](roles-and-access.md).

## State transition
The movement of a case from one state to another through an event. For example, `citizen-cic-submit-dss-application` moves a case from `DSS_Draft` to `DSS_Submitted`. Events declare their from-states and to-state, and CCD rejects an event if the case is not in one of its from-states. See [Case lifecycle](case-lifecycle.md).

## TASK_NAME
An environment variable that selects which scheduled task a Kubernetes CronJob runs. When a pod starts with, for example, `TASK_NAME=SystemClearInactiveDssDraftCasesTask`, the case API's `ScheduledTaskRunner` looks up the bean with that name and calls `run()`. See [Scheduled tasks](scheduled-tasks.md).

## UserRole
An enum in the case API that lists the IDAM roles, case roles and access-profile names used in the CIC case type. Each value carries a CCD permission level. `UserRole` is the role type parameter in event, state, field and tab configuration. See [Roles and access](roles-and-access.md).

## Work Allocation
The HMCTS task-management and work-distribution platform. This service publishes case events to a shared Azure Service Bus topic, which Work Allocation consumes. Work Allocation evaluates the DMN rules to decide which tasks to create, and caseworkers and judges claim and complete those tasks in ExUI. See [Work allocation](work-allocation.md).

## See also

- [Your first week](first-week.md): a guided path through the service
- [Architecture](architecture.md): how the components and integrations fit together
- [Roles and access](roles-and-access.md): the access layers and role configuration
