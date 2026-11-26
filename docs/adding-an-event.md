# Adding an event

This guide adds a CCD event to the `CriminalInjuriesCompensation` case type (jurisdiction `ST_CIC`). An event is a Spring bean that implements `CCDConfig<CaseData, State, UserRole>`. The config generator turns it into CCD definition JSON. Because the case type is decentralised, this service also serves every callback for the event.

In summary:

- Add the event ID to `EventConstants`.
- Put the class in `caseworker/event/`, `citizen/event/` or `systemupdate/event/`, depending on who triggers it.
- Declare the states, name, grants and pages in `configure()`, and wire only the callbacks you need.
- Regenerate with `./gradlew generateCCDConfig`, then add the `enable_ccd_diff` label to the PR to get a definition diff.

## Before you start

The examples use [`CaseworkerCaseBuilt`](../src/main/java/uk/gov/hmcts/sptribs/caseworker/event/CaseworkerCaseBuilt.java) (`caseworker-case-built`). It is a simple caseworker event that moves a case from `Submitted` to `CaseManagement` and has only a `submitted` callback.

Decide who triggers the event. That decides the package and the roles:

| Trigger | Package (under `src/main/java/uk/gov/hmcts/sptribs/`) | Typical grant |
|---|---|---|
| Caseworker or judge in ExUI | `caseworker/event/` | `CREATE_READ_UPDATE` to the `ST_CIC_*` roles |
| Citizen (DSS journeys) | `citizen/event/` | `CREATE_READ_UPDATE` to `CITIZEN` and `CREATOR` |
| System or scheduled task | `systemupdate/event/` | `CREATE_READ_UPDATE_DELETE` to `SYSTEM_UPDATE` |

### Which case-data class to declare

On master, declare `CCDConfig<CaseData, State, UserRole>`, like every existing event. The `CriminalInjuriesCompensationData` class exists on master, but only the case-type bean and `CICAccessProfile` use it.

The multi-case-type groundwork on the `send35-prototype` branch moves every CIC config onto `CriminalInjuriesCompensationData`. Once that work is on master, declare new CIC events as `CCDConfig<CriminalInjuriesCompensationData, State, UserRole>`. The reason is explained in [Adding a case type](adding-a-case-type.md).

## Steps

1. **Add the event ID to `EventConstants`.**
   Open [`EventConstants.java`](../src/main/java/uk/gov/hmcts/sptribs/caseworker/util/EventConstants.java) and add a `public static final String` constant. Follow the existing naming style, for example `CASEWORKER_CASE_BUILT = "caseworker-case-built"`. Citizen DSS event IDs are an exception: `CicCreateCaseEvent` takes `citizen-cic-create-dss-application` from `caseinfo.apps` in `application.yaml`.

2. **Create the event class in the right package.**
   Annotate it with `@Component` so that the generator picks it up, and implement `CCDConfig`. Name it after its siblings: `Caseworker<Action>`, `Cic<Action>Event` or `System<Action>`.

3. **Configure the event in `configure(ConfigBuilder)`.**
   - **States.** Use `forStateTransition(from, to)` for a move, or `forState(...)` / `forStates(...)` when the state doesn't change. CCD rejects the event if the case isn't in a permitted from-state. State constants are in `ciccase/model/State.java`. Groupings such as `POST_SUBMISSION_STATES` and `BUNDLE_STATES` save listing states one by one.
   - **Name and description.** These are the labels in the ExUI event dropdown.
   - **Grants.** Use the constants in [`Permissions.java`](../src/main/java/uk/gov/hmcts/sptribs/ciccase/model/access/Permissions.java) (`CREATE_READ_UPDATE`, `CREATE_READ_UPDATE_DELETE`, `READ` and so on) with roles from `ciccase/model/UserRole.java`. See [Roles and access](roles-and-access.md).
   - **Pages and fields.** Pages are usually built with the [`PageBuilder`](../src/main/java/uk/gov/hmcts/sptribs/common/ccd/PageBuilder.java) wrapper. Larger events keep them in separate classes under `caseworker/event/page/`. Declare each field as mandatory, optional or read-only on its page.
   - **Show conditions.** Add page or field show conditions as CCD condition strings, for example `"LinkedCasesComponentLauncher!=\"\""`.

   This extract is from `CaseworkerCaseBuilt` (imports omitted):

   ```java
   @Component
   @Slf4j
   public class CaseworkerCaseBuilt implements CCDConfig<CaseData, State, UserRole> {

       @Override
       public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
           Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
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

           new PageBuilder(eventBuilder)
               .page("caseBuilt")
               .pageLabel("Case Built")
               .label("LabelCaseBuilt", "");
       }
   ```

   The builder methods are `event`, `forStateTransition` / `forState` / `forStates`, `name`, `grant`, `grantHistoryOnly`, `aboutToStartCallback`, `aboutToSubmitCallback`, `submittedCallback` and `publishToCamunda`. `publishToCamunda()` marks the event for publishing to Work Allocation.

4. **Implement the callbacks you need.** Wire only the ones the event uses.

   | Callback | When it runs | Typical use | Example event |
   |---|---|---|---|
   | `aboutToStart` | Before the first page loads | Pre-populate fields, block with errors | `CaseworkerRecordListing`, `CaseworkerCreateAndSendOrder` |
   | Mid-event (per page) | When a page is submitted | Validate page input, return errors | Page classes under `caseworker/event/page/` |
   | `aboutToSubmit` | After input, before CCD saves | Change case data or state, return errors | `CaseworkerEditCase`, `CaseworkerCloseTheCase` |
   | `submitted` | After CCD has saved | Notifications, external calls, confirmation text (`SubmittedCallbackResponse`) | `CaseworkerCaseBuilt`, `CicCreateCaseEvent` |

   `aboutToStart` and `aboutToSubmit` return `AboutToStartOrSubmitResponse<CaseData, State>`. Put errors in the response to stop the event. Business logic usually lives in an injected service, not in the event class.

5. **Add notifications, if the event needs them.** Notifications are normally sent from the `submitted` callback through the dispatchers in `notification/dispatcher/`. See [Adding a notification](adding-a-notification.md).

6. **Add Work Allocation tasks, if the event needs them.** Case events reach Work Allocation over Azure Service Bus. Task creation, cancellation and completion for the new event ID are configured in the WA DMNs, not in the event class. See [Adding a Work Allocation task](adding-a-work-allocation-task.md).

7. **Write the tests, following the existing patterns.**

   | Kind | Source set | Naming | Run with |
   |---|---|---|---|
   | Unit | `src/test/java` | `<Event>Test` | `./gradlew test` |
   | Integration | `src/integrationTest/java` | `<Event>IT` | `./gradlew integration` |
   | Functional (VPN, runs against AAT) | `src/functionalTest/java` | `<Event>FT` | `./gradlew functional` |

   For `CaseworkerCaseBuilt` the existing tests are:

   - Unit: [`CaseWorkerCaseBuiltTest.java`](../src/test/java/uk/gov/hmcts/sptribs/caseworker/event/CaseWorkerCaseBuiltTest.java)
   - Integration: [`CaseworkerCaseBuiltIT.java`](../src/integrationTest/java/uk/gov/hmcts/sptribs/caseworker/event/CaseworkerCaseBuiltIT.java)
   - Functional: [`CaseworkerCaseBuiltFT.java`](../src/functionalTest/java/uk/gov/hmcts/sptribs/caseworker/CaseworkerCaseBuiltFT.java)

   Unit tests usually assert the generated config (state transition, grants) and each callback's response. Copy the nearest sibling event's tests as a starting point. See [Testing](testing.md).

8. **Regenerate the definitions and review the diff.**
   Run `./gradlew generateCCDConfig`. It starts the app in the `config-gen` profile, with Flyway and JMS disabled and an in-memory H2 database, and writes the definition JSON to `build/definitions/CriminalInjuriesCompensation/`. Check that the new event, its `AuthorisationCaseEvent` rows and any new fields appear.

   Then add the `enable_ccd_diff` label to the PR. [`.github/workflows/ccd-diff.yml`](../.github/workflows/ccd-diff.yml) runs when the label is added, and again on every push while it stays on. It generates the definitions on master and on your branch, compares them with `npx @hmcts/ccd-diff`, and posts a PR comment. The comment holds the start of the HTML report (up to 65,000 characters) and a link to the Actions run, where the full report is an artifact.

9. **Test in preview.**
   Every PR deploys to preview with its own CCD stack and imports the definition. Add `enable_keep_helm` to keep the deployment after the pipeline finishes. If the event creates WA tasks, add `pr-values:wa`, and `pr-values:wa-ft-tests` to run the WA functional tests. Sign in to the preview ExUI as a user with a granted role and run the event on a case in a valid from-state. See [Release and deploy](release-and-deploy.md).

## Verify

- The `enable_ccd_diff` comment lists the new event ID under `CriminalInjuriesCompensation`, with the expected states and authorisations, and no unexpected removals.
- In the preview ExUI, a case in the from-state shows the event in the "Next step" dropdown for a granted role, and not for a role without a grant. Submitting it moves the case to the expected state and adds the event to History.

## See also

- [Events reference](events-reference.md): every event and state
- [Case lifecycle](case-lifecycle.md): how the states connect
- [Adding a notification](adding-a-notification.md)
- [Adding a Work Allocation task](adding-a-work-allocation-task.md)
