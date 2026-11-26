# Roles and access

Access to the `CriminalInjuriesCompensation` case type is declared in Java in this repository with the ccd-config-generator SDK, and CCD enforces it. The service itself does not check roles on callbacks. This page covers the role enums, the access-profile mapping, how grants are declared on events, states and fields, and how citizens and the respondent (CICA) get access to a case.

In summary:

- [`UserRole`](../src/main/java/uk/gov/hmcts/sptribs/ciccase/model/UserRole.java) lists IDAM roles, a case role and access-profile names: `ST_CIC_CASEWORKER`, `ST_CIC_SENIOR_CASEWORKER`, `ST_CIC_HEARING_CENTRE_ADMIN`, `ST_CIC_HEARING_CENTRE_TEAM_LEADER`, `ST_CIC_SENIOR_JUDGE`, `ST_CIC_JUDGE`, `ST_CIC_RESPONDENT`, `ST_CIC_WA_CONFIG_USER`, plus `SUPER_USER`, `SYSTEM_UPDATE`, `CITIZEN`, `CREATOR` and others.
- [`CICAccessProfile`](../src/main/java/uk/gov/hmcts/sptribs/ciccase/model/accessprofile/CICAccessProfile.java) maps IDAM roles and AM organisational roles to CCD access profiles such as `CIC_CASEWORKER`, `CIC_RESPONDENT` and `NON_RESPONDENT_PROFILE`. It uses a second enum, `UserRolesForAccessProfiles`.
- Citizen events grant `CITIZEN` and `CREATOR`. `CicaRoleService.ensureCreatorRole()` gives the citizen the `[CREATOR]` case role on their case through CCD's case-users API.
- CICA, the respondent, gets in through the AM organisational role `cica`, which `CICAccessProfile` maps to `CIC_RESPONDENT`. The respondent does not see the notes and correspondence tabs.

## Layers of access

Access to a CIC case passes through four layers. Each layer is owned by a different component.

| Layer | What it is | Where it is declared | Who enforces it |
|---|---|---|---|
| IDAM role | Role on the user's IDAM account, for example `caseworker-st_cic-caseworker` or `citizen` | IDAM user set-up. Mirrored in `UserRole` | IDAM issues it. CCD and the citizen apps read it |
| AM role assignment | Organisational or case role held in the Role Assignment Service (`RAS_*` names in code) | AM. Role names referenced in `UserRolesForAccessProfiles` | AM stores it. CCD reads it |
| Case role | Role on one case, for example `[CREATOR]` | `UserRole.CREATOR`. Assigned at runtime by `CicaRoleService` | CCD case-user assignment |
| Access profile | CCD-level grant used in event, state, field and tab authorisations | `CICAccessProfile` | CCD |

This service does not call AM's Role Assignment Service and has no AM client. `ROLE_ASSIGNMENT_URL` appears only in environment set-up: the `cftlibTest` task in [`build.gradle`](../build.gradle) and the preview Helm values, where it points the in-cluster CFT components at AM.

The one role write the service makes is the `[CREATOR]` case role. `CicaRoleService` adds it with `uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi` (`addCaseUserRoles`, `getUserRoles`). That is CCD data-store's case-users API, not an AM call.

## The `UserRole` enum

`UserRole` is the role type parameter in the event, state, field and tab config beans (`CCDConfig<CaseData, State, UserRole>`). It implements the SDK's `HasRole`. Each value carries a role string (the `@JsonValue`) and a case-type permission string.

| Group | Value | Role string | Permission |
|---|---|---|---|
| Generic | `SUPER_USER` | `caseworker-sptribs-superuser` | `CRU` |
| Generic | `SYSTEM_UPDATE` | `caseworker-sptribs-systemupdate` | `CRUD` |
| Generic | `CASEWORKER` | `caseworker` | `CRU` |
| Case role | `CREATOR` | `[CREATOR]` | `CRU` |
| Older CIC roles | `DISTRICT_JUDGE_CIC` | `caseworker-sptribs-cic-districtjudge` | `CRU` |
| Older CIC roles | `RESPONDENT_CIC` | `caseworker-sptribs-cic-respondent` | `CRU` |
| CIC staff | `ST_CIC_CASEWORKER` | `caseworker-st_cic-caseworker` | `CRU` |
| CIC staff | `ST_CIC_SENIOR_CASEWORKER` | `caseworker-st_cic-senior-caseworker` | `CRU` |
| CIC staff | `ST_CIC_HEARING_CENTRE_ADMIN` | `caseworker-st_cic-hearing-centre-admin` | `CRU` |
| CIC staff | `ST_CIC_HEARING_CENTRE_TEAM_LEADER` | `caseworker-st_cic-hearing-centre-team-leader` | `CRU` |
| CIC judiciary | `ST_CIC_SENIOR_JUDGE` | `caseworker-st_cic-senior-judge` | `CRU` |
| CIC judiciary | `ST_CIC_JUDGE` | `caseworker-st_cic-judge` | `CRU` |
| Respondent | `ST_CIC_RESPONDENT` | `caseworker-st_cic-respondent` | `CRU` |
| Access profile | `NON_RESPONDENT_USER` | `non-respondent-user` | `R` |
| Case flags | `AC_CASE_FLAGS_ADMIN` | `caseflags-admin` | `CRU` |
| Case flags | `AC_CASE_FLAGS_VIEWER` | `caseflags-viewer` | `R` |
| Citizen | `CITIZEN` | `citizen` | `CRU` |
| Global search | `GS_PROFILE` | `GS_profile` | `R` |
| Work Allocation configuration | `ST_CIC_WA_CONFIG_USER` | `caseworker-wa-task-configuration` | `CRU` |
| RAS validation | `RAS_CASEWORKER_VALIDATION` | `caseworker-ras-validation` | `R` |

`ST_CIC_WA_CONFIG_USER` is the Work Allocation task-configuration user. It is granted `CRU` on several events that publish to Camunda, such as `caseworker-case-built`, `citizen-cic-submit-dss-application` and the hearing-bundle and hearing-outcome system triggers. See [work-allocation.md](work-allocation.md).

## Access profiles: `CICAccessProfile`

`CICAccessProfile` is a `CCDConfig<CriminalInjuriesCompensationData, State, UserRolesForAccessProfiles>` bean. It calls `configBuilder.caseRoleToAccessProfile(...)` 54 times, mapping each role to a list of CCD access profiles. The mapped roles are IDAM roles (prefixed `idam:`), the `[CREATOR]` case role, and AM roles for legal operations, CTSC, admin, judiciary and CICA. Representative rows:

| Role | Access profiles |
|---|---|
| `ST_CIC_CASEWORKER` | `CIC_CASEWORKER`, `AC_CASEFLAGS_ADMIN`, `NON_RESPONDENT_PROFILE` |
| `ST_CIC_SENIOR_JUDGE` | `CIC_SENIOR_JUDGE`, `AC_CASEFLAGS_VIEWER`, `NON_RESPONDENT_PROFILE` |
| `ST_CIC_RESPONDENT` | `CIC_RESPONDENT` only |
| `RAS_ST_CICA` (AM role) | `CIC_RESPONDENT`, `AC_CASEFLAGS_ADMIN` |

The role names on both sides of the mapping live in [`UserRolesForAccessProfiles`](../src/main/java/uk/gov/hmcts/sptribs/ciccase/model/UserRolesForAccessProfiles.java). It is a separate enum that also implements `HasRole`. Its constants fall into three groups:

- IDAM roles, with an `idam:` prefix, for example `ST_CIC_CASEWORKER("idam:caseworker-st_cic-caseworker")` and `CITIZEN_CIC("idam:citizen")`. These are the left-hand side of the mapping.
- Access profiles, without the prefix, for example `CIC_CASEWORKER("caseworker-st_cic-caseworker")`, `NON_RESPONDENT_PROFILE("non-respondent-user")` and `AC_CASEFLAGS_ADMIN("caseflags-admin")`. These are the right-hand side.
- AM role names (`RAS_*`), also on the left-hand side:

| Constant | Role name | Permission |
|---|---|---|
| `RAS_HMCTS_STAFF` | `hmcts-staff` | `R` |
| `RAS_HMCTS_CTSC` | `hmcts-ctsc` | `CRU` |
| `RAS_HMCTS_ADMIN` | `hmcts-admin` | `CRU` |
| `RAS_HMCTS_JUDICIARY` | `hmcts-judiciary` | `CRU` |
| `RAS_ST_TRIBUNAL_CASEWORKER` | `tribunal-caseworker` | `CRU` |
| `RAS_ST_CICA` | `cica` | `CRU` |

The access-profile strings are the same strings that `UserRole` uses for its staff, judiciary and flag values. That is what links the two enums. An access profile such as `caseworker-st_cic-caseworker`, granted in `CICAccessProfile`, is the role that grants on `UserRole.ST_CIC_CASEWORKER` refer to.

The flag constants are spelt differently in the two enums (`AC_CASE_FLAGS_ADMIN` in `UserRole`, `AC_CASEFLAGS_ADMIN` in `UserRolesForAccessProfiles`), but both resolve to `caseflags-admin`, and likewise for `caseflags-viewer`.

`NON_RESPONDENT_PROFILE` keeps the respondent out of internal content. The **notes** and **correspondence** tabs in [`CaseTypeTab`](../src/main/java/uk/gov/hmcts/sptribs/ciccase/tab/CaseTypeTab.java) are shown only to `NON_RESPONDENT_USER` (`non-respondent-user`), and `NonRespondentAccess` grants that role read access to fields. CICA users with `CIC_RESPONDENT` do not get that profile.

## Declaring authorisations in config-generator code

On master, most CIC config beans implement `CCDConfig<CaseData, State, UserRole>`. The case-type bean `CriminalInjuriesCompensation` and `CICAccessProfile` are declared on `CriminalInjuriesCompensationData`, a subclass of `CaseData` with no extra members. Authorisations are declared in three places.

**Events.** Each event's `configure(ConfigBuilder)` calls `.grant(...)` with a constant from [`Permissions`](../src/main/java/uk/gov/hmcts/sptribs/ciccase/model/access/Permissions.java). The common constants are `CREATE_READ_UPDATE` (`CRU`), `CREATE_READ_UPDATE_DELETE` (`CRUD`), `CREATE_READ` (`CR`), `READ` (`R`) and `READ_UPDATE` (`RU`). There are also `READ_UPDATE_DELETE`, `DELETE`, `UPDATE_DELETE` and `CREATE_UPDATE`. `grantHistoryOnly(...)` lets a role see the event in case history without triggering it. For example, from [`CaseworkerCaseBuilt`](../src/main/java/uk/gov/hmcts/sptribs/caseworker/event/CaseworkerCaseBuilt.java):

```java
configBuilder
    .event(CASEWORKER_CASE_BUILT)
    .forStateTransition(Submitted, CaseManagement)
    .name("Case: Build case")
    .submittedCallback(this::submitted)
    .grant(CREATE_READ_UPDATE, SUPER_USER,
        ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
        ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, ST_CIC_WA_CONFIG_USER)
    .grantHistoryOnly(ST_CIC_JUDGE)
    .publishToCamunda();
```

The grants for every event are in [events-reference.md](events-reference.md).

**States.** Each `State` value carries a `@CCD` annotation that lists access classes from [`ciccase/model/access/`](../src/main/java/uk/gov/hmcts/sptribs/ciccase/model/access/):

| Access class | Grants | Used on |
|---|---|---|
| `DefaultStateAccess` | `CREATOR`: `R` | `Completed`, `Rejected`, `Sent`, `Withdrawn` |
| `DefaultStateAccessExcludingCAA` | `CREATOR`: `R`; `SYSTEM_UPDATE`: `CRU` | Every other state |
| `CaseworkerRASValidationAccess` | `RAS_CASEWORKER_VALIDATION`: `R` | Most states (not `DeathOfAppellant` or `DSS_Expired`) |
| `GlobalSearchAccess` | `GS_PROFILE`: `R`, plus `UD` for most roles | Most states (not `DeathOfAppellant` or `DSS_Expired`) |
| `CaseworkerWithCAAAccess` | `ST_CIC_JUDGE` and `ST_CIC_RESPONDENT`: `CRU` | `ReadyToList` |
| `SolicitorAccess` | `CASEWORKER`: `CRU` | `Draft` |

"CAA" in these class names means case access administrator. The names follow a pattern shared with other CCD services. This service has no CAA role, so read each class by the grants in the table. `DefaultStateAccessExcludingCAA` differs from `DefaultStateAccess` by adding `SYSTEM_UPDATE`.

**Fields and tabs.** Field-level access classes include `CaseFlagsAccess` (`AC_CASE_FLAGS_ADMIN`: `CRU`, `AC_CASE_FLAGS_VIEWER`: `R`), `CaseLinksDefaultAccess` (linked cases) and `NonRespondentAccess`. Tab visibility is set per role in `CaseTypeTab`.

System events in `systemupdate/event/` grant `CREATE_READ_UPDATE_DELETE` to `SYSTEM_UPDATE`. A few also grant `SUPER_USER` (for example `SystemCreateTestCase` and `SystemMigrateCaseDocumentsToDocTable`), and the hearing-bundle and hearing-outcome triggers grant `CRU` to `ST_CIC_WA_CONFIG_USER`. They run as the system-update user, whose credentials come from Key Vault (see [configuration.md](configuration.md)).

## Citizen access

Citizens sign in through IDAM with the `citizen` role. They use [sptribs-frontend](https://github.com/hmcts/sptribs-frontend) (DSS application) or [sptribs-dss-update-case-web](https://github.com/hmcts/sptribs-dss-update-case-web) (DSS update case).

### Event grants

The citizen events are in [`citizen/event/`](../src/main/java/uk/gov/hmcts/sptribs/citizen/event/):

| Event ID | Class | States | Grants |
|---|---|---|---|
| `citizen-cic-create-dss-application` | `CicCreateCaseEvent` | Initial state `Draft`; `aboutToSubmit` sets `DSS_Draft` | `CRU`: `CITIZEN`, `CREATOR` |
| `citizen-cic-update-dss-application` | `CicUpdateCaseEvent` | `Draft`, `Submitted`, `DSS_Draft` | `CRUD`: `CITIZEN`, `CREATOR` |
| `citizen-cic-submit-dss-application` | `CicSubmitCaseEvent` | `DSS_Draft` to `DSS_Submitted` | `CRUD`: `CITIZEN`; `CRU`: `SYSTEM_UPDATE`, `CREATOR`, `ST_CIC_WA_CONFIG_USER`; history only for staff and judges |
| `citizen-cic-dss-update-case` | `CicDssUpdateCaseEvent` | Every state except `DSS_Expired` | `CRUD`: `CITIZEN`, `CREATOR`; `CRU`: `ST_CIC_WA_CONFIG_USER`; history only for staff, judges and `SYSTEM_UPDATE` |

### The `[CREATOR]` case role and `CicaRoleService`

`CREATOR` is the `[CREATOR]` case role. It gives the citizen access to the case they started. [`CicaRoleService.ensureCreatorRole()`](../src/main/java/uk/gov/hmcts/sptribs/ciccase/service/CicaRoleService.java) takes a case reference and the user's token, then:

1. Gets the citizen's IDAM user ID from the token through `IdamService`. If there is no user ID it throws `UnauthorisedCaseAccessException`.
2. Gets a system-update user token from `IdamService` and an S2S token from `AuthTokenGenerator`.
3. Calls `hasCreatorRole()`, which reads the case's user roles with `caseAssignmentApi.getUserRoles()`. If the role is already there, it stops.
4. Otherwise it builds a `CaseAssignmentUserRoleWithOrganisation` with `caseRole = "[CREATOR]"` and calls `caseAssignmentApi.addCaseUserRoles()` with the system-update token.
5. If the call fails but the role is now present, for example because another request added it at the same time, it does not throw.
6. It logs `event=citizen_creator_role_ensured outcome=success`.

Callers check case membership before calling it. The assigned role does not replace the permission checks that later run with the citizen's token.

`ensureCreatorRole()` is called from `CicaCaseService`, `CicaCaseUpdateService` and `DocumentDownloadService`. These back the `/cases/cica/...` endpoints in [`CicaCaseController`](../src/main/java/uk/gov/hmcts/sptribs/controllers/CicaCaseController.java), including `POST /cases/cica/{ccdReference}/role-assignments`. `DocumentDownloadService` calls it again after a 404 from CDAM, then retries. `CicCreateCaseEvent` does not call it.

### DSS update-case checks

`sptribs-dss-update-case-web` adds its own steps before a citizen can submit `citizen-cic-dss-update-case`:

- Its OIDC middleware requires the `citizen` role on every route except sign-in, sign-out and the static information pages.
- The case finder calls `GET /cases/cica/{caseId}/access` on this service to check that the case exists and that the user may access it.
- The subject-details step calls `POST /cases/cica/{caseId}/subject-verifications` with the subject's full name and date of birth. On success the app records the verified case and user in the session. POSTs to the step are rate-limited per user and case.
- The upload and check-your-answers pages require a verified case that matches the signed-in user.
- Check-your-answers verifies the subject again, then posts to `/cases/cica/{caseId}/updates`.

## CICA (respondent) access

CICA is the respondent on every CIC case. CICA users get access through the AM role `cica` (`RAS_ST_CICA`), which `CICAccessProfile` maps to `CIC_RESPONDENT` plus `AC_CASEFLAGS_ADMIN`. The IDAM-role route, `idam:caseworker-st_cic-respondent` (`ST_CIC_RESPONDENT`), maps to `CIC_RESPONDENT` only. Neither route grants `NON_RESPONDENT_PROFILE`, so respondents do not see the notes or correspondence tabs or the fields guarded by `NonRespondentAccess`.

AM's organisational role mapping service creates the `cica` assignment, not this service. The Drools rule `st_cic_cica_org_role` in [`stcic-other-mapping.drl`](https://github.com/hmcts/am-org-role-mapping-service/blob/master/src/main/resources/validationrules/stcic/stcic-other-mapping.drl) fires for a caseworker staff profile with role ID `17` and service code `BBA2` that is not suspended, while the `ST_CIC_WA_1_0` feature flag is on. It creates an `ORGANISATION` role, category `OTHER_GOV_DEPT`, grant type `STANDARD`, with `jurisdiction = ST_CIC` and the user's `primaryLocation`. The role is defined in [`role_stcic.json`](https://github.com/hmcts/am-role-assignment-service/blob/master/src/main/resources/roleconfig/role_stcic.json). So a CICA user gets the role once they are set up in staff reference data with that role and service.

## Organisational roles used in tests

[sptribs-e2etests](https://github.com/hmcts/sptribs-e2etests) holds one credential pair for each test persona, loaded from the `sptribs-<env>` Key Vault. Its 12 Work Allocation personas show the organisational roles that task routing expects:

| Persona | Key Vault secret name prefix |
|---|---|
| `waCaseWorker` | `wa-caseworker-` |
| `waSeniorCaseworker` | `wa-seniorCaseworker-` |
| `waHearingCentreAdmin` | `wa-hearingCentreAdmin-` |
| `waHearingCentreTeamLead` | `wa-hearingCentreTeamLead-` |
| `waRegionalHearingCentreAdmin` | `wa-regionalHearingCentreAdmin-` |
| `waRegionalHearingCentreTeamLead` | `wa-regionalHearingCentreTeamLead-` |
| `waCTSCAdmin` | `wa-ctscAdmin-` |
| `waCTSCTeamLead` | `wa-ctscTeamLead-` |
| `waRespondent` | `wa-respondent-` |
| `waPresidentOfTribunal` | `wa-presidentOfTribunal-` |
| `waTribunalJudgeSalaried` | `wa-tribunalJudgeSalaried-` |
| `waTribunalJudgeFeePaid` | `wa-tribunalJudgeFeePaid-` |

Each prefix has a `-username` and a `-password` secret. The other personas are `citizen`, `respondent`, `superUser` and `demoCitizen`.

Task permissions (who can claim or complete a task) are set in [`wa-task-permissions-st_cic-criminalinjuriescompensation.dmn`](../src/main/resources/dmn/wa-task-permissions-st_cic-criminalinjuriescompensation.dmn). See [work-allocation.md](work-allocation.md).

## See also

- [case-lifecycle.md](case-lifecycle.md): which roles can trigger each event and see each state
- [events-reference.md](events-reference.md): the grants on every event
- [work-allocation.md](work-allocation.md): task DMNs and the roles that claim and complete tasks
- [adding-a-case-type.md](adding-a-case-type.md): why each config declares its case-data class
