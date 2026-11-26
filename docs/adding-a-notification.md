# Adding a notification

A new notification needs four things: a GOV.UK Notify template, a template ID in `application.yaml`, a `TemplateName` constant, and a dispatcher class that implements `PartiesNotification`. The dispatcher is called from the event's `submitted()` callback. `NotificationServiceCIC` sends the message and records it in `case_correspondences` with a PDF copy in CDAM, so you don't write correspondence code yourself. Read [notifications.md](notifications.md) first. It covers the dispatcher pattern, the channel rules and the failure behaviour that this recipe relies on.

Paths below are relative to `src/main/java/uk/gov/hmcts/sptribs/` unless they start with `src/`.

## 1. Create the templates in GOV.UK Notify

1. In the sptribs Notify service, create an English template for each channel you need: an email template, a letter template, or both.
2. If the notification follows a citizen DSS submission and must support Welsh, create a Welsh version as well (see step 4).
3. If the email should link to the citizen dashboard, create a dashboard variant as well.
4. Name the personalisation fields in each template. The dispatcher must supply every field the template uses (step 5).
5. Copy each template ID. You add them to config in the next step.

The Notify API key comes from the Key Vault secret `uk-gov-notify-api-key`. The Helm chart exposes it as `UK_GOV_NOTIFY_API_KEY_1`, `application.yaml` maps that to `uk.gov.notify.api.sptribskey`, and `NotificationsConfiguration` reads that property to build the `NotificationClient` bean. Use the Notify service that this key belongs to.

## 2. Add the template IDs to `application.yaml`

Add one entry per template under `uk.gov.notify.email.templatesCIC` in [`application.yaml`](../src/main/resources/application.yaml). The key is the `TemplateName` constant name and the value is the Notify template ID. `EmailTemplatesConfigCIC` binds the map, and `NotificationServiceCIC` looks the ID up with `template.name()`.

| Suffix | Use for | Existing example |
|---|---|---|
| *(none)* or `_EMAIL` | English email | `APPLICATION_RECEIVED`, `DECISION_ISSUED_EMAIL` |
| `_POST` | Letter | `CASE_ISSUED_CITIZEN_POST`, `CASE_LINKED_POST` |
| `_NEW_CD` | Email with a citizen-dashboard link | `APPLICATION_RECEIVED_NEW_CD` |
| `_CY` | Welsh version, added last | `APPLICATION_RECEIVED_CY`, `DECISION_ISSUED_EMAIL_NEW_CD_CY` |

The shape, with placeholders in place of the IDs:

```yaml
uk:
  gov:
    notify:
      email:
        templatesCIC:
          MY_NEW_EVENT_EMAIL: <notify-template-id>
          MY_NEW_EVENT_POST: <notify-template-id>
```

The IDs are literal values, the same in every environment. Neither the Helm chart nor the Flux config overrides them. Only the API key differs per environment. Nothing checks the IDs against Notify at start-up, so a wrong or missing ID shows up only when a send fails at runtime.

## 3. Add the `TemplateName` constants

Add a constant to the [`TemplateName`](../src/main/java/uk/gov/hmcts/sptribs/notification/TemplateName.java) enum for every key you added in step 2. The names must match the YAML keys exactly, because the YAML map is keyed by these names.

## 4. Write the dispatcher

Create a `@Component` class in [`notification/dispatcher/`](../src/main/java/uk/gov/hmcts/sptribs/notification/dispatcher/) that implements [`PartiesNotification`](../src/main/java/uk/gov/hmcts/sptribs/notification/PartiesNotification.java). Name it after the event, following existing classes such as `CaseLinkedNotification`, `CaseIssuedNotification` and `ApplicationReceivedNotification`. Inject `NotificationServiceCIC` and `NotificationHelper` through the constructor.

1. `PartiesNotification` has a no-op default for every method, so override only the parties that should receive the notification. The basic signature is `void sendToSubject(CaseData caseData, String caseNumber)`, and likewise `sendToApplicant`, `sendToRepresentative`, `sendToRespondent` and `sendToTribunal`. Each also has an overload `String sendToX(CaseData, String caseNumber, Map<String, String> uploadedDocuments)` for document attachments (used by `ContactPartiesNotification`). The subject and representative have a `DssCaseData` overload for citizen DSS flows.
2. Choose the channel from the party's contact preference on `CicCase`:

   | Party | Preference field | Email address | Postal address |
   |---|---|---|---|
   | Subject | `contactPreferenceType` | `getEmail()` | `getAddress()` |
   | Applicant | `applicantContactDetailsPreference` | `getApplicantEmailAddress()` | `getApplicantAddress()` |
   | Representative | `representativeContactDetailsPreference` | `getRepresentativeEmailAddress()` | `getRepresentativeAddress()` |
   | Respondent | none, always emailed | `getRespondentEmail()` | none |

3. For `EMAIL`, call `NotificationServiceCIC.sendEmail(NotificationRequest, caseReferenceNumber, Party)`. Most dispatchers pass `null` as the party. Pass a `Party` value if you want `receiving_party` filled on the correspondence row.
4. Otherwise, call `NotificationServiceCIC.sendLetter(NotificationRequest, caseReferenceNumber)`.
5. Pick the `TemplateName` variant: `_POST` for letters, and `_NEW_CD` when `feature.citizen-dashboard.enabled` is true. The dispatchers that support it inject the flag with `@Value` and choose in a `getTemplateName()` method.
6. Follow the existing channel rule: email when the preference is `EMAIL`, a letter otherwise. A party with no preference set therefore gets a letter. The email address isn't checked first, and there is no fallback from email to post. Email-only dispatchers (such as `ApplicationReceivedNotification`) send nothing when the preference isn't `EMAIL`.
7. Store the returned `NotificationResponse` on the case, as the existing dispatchers do (`setSubjectNotifyList`, `setAppNotificationResponse`, `setRepNotificationResponse`, `setResNotificationResponse`).

Welsh: only `DssApplicationReceivedNotification` selects a `_CY` template, from `DssCaseData.languagePreference`, which is set only at DSS submission. A caseworker-event dispatcher has no language source on `CaseData`, so it sends English.

An extract from [`CaseLinkedNotification`](../src/main/java/uk/gov/hmcts/sptribs/notification/dispatcher/CaseLinkedNotification.java):

```java
@Override
public void sendToSubject(final CaseData caseData, final String caseNumber) {
    final CicCase cicCase = caseData.getCicCase();
    final Map<String, Object> templateVars = notificationHelper.getSubjectCommonVars(caseNumber, caseData);

    final NotificationResponse notificationResponse;
    if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
        notificationResponse = sendEmailNotification(cicCase.getEmail(), templateVars, caseNumber);
    } else {
        notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVars);
        notificationResponse = sendLetterNotification(templateVars, caseNumber);
    }

    cicCase.setSubjectNotifyList(notificationResponse);
}

private NotificationResponse sendEmailNotification(final String destinationAddress,
                                                   final Map<String, Object> templateVars,
                                                   String caseReferenceNumber) {
    final NotificationRequest request = notificationHelper.buildEmailNotificationRequest(
        destinationAddress,
        templateVars,
        TemplateName.CASE_LINKED_EMAIL);
    return notificationService.sendEmail(request, caseReferenceNumber, null);
}
```

## 5. Build the template variables with `NotificationHelper`

Use [`NotificationHelper`](../src/main/java/uk/gov/hmcts/sptribs/notification/NotificationHelper.java) to build the personalisation map and the request instead of building them by hand.

- `getSubjectCommonVars`, `getApplicantCommonVars`, `getRepresentativeCommonVars`, `getRespondentCommonVars` and `getTribunalCommonVars` (all `(String caseNumber, CaseData caseData)`) return a mutable map with `TribunalName`, `CicCaseNumber`, `CicCaseSubjectFullName`, `hasCicaNumber`, `cicaRefNumber` and that party's `ContactName`.
- For letters, `addAddressTemplateVars(AddressGlobalUK, Map)` copies address lines 1 to 3, post town, county, country and postcode into `address_line_1` to `address_line_7`. Notify needs these to address the letter, and `sendLetter` throws if they are all empty.
- `buildEmailNotificationRequest(destinationAddress, templateVars, TemplateName)` and `buildLetterNotificationRequest(templateVars, TemplateName)` build the `NotificationRequest`. A second `buildEmailNotificationRequest` overload takes attachment flags and uploaded documents.
- Add the variables that are specific to your event to the map. Constant names for keys live in [`common/CommonConstants.java`](../src/main/java/uk/gov/hmcts/sptribs/common/CommonConstants.java).
- On the `_NEW_CD` path, put the dashboard link under `DashboardLink` (`CommonConstants.DASHBOARD_KEY`), read from `sptribs-frontend.dashboard-url`, only when `feature.citizen-dashboard.enabled` (env `CITIZEN_DASHBOARD_ENABLED`) is true.
- The `uk.gov.notify.email.templateVars` values in `application.yaml` (`signInProfessionalUsersUrl`, respondent and tribunal name, organisation and email) are bound into `EmailTemplatesConfigCIC`, but `NotificationHelper` doesn't add them. If your template needs one, add it yourself.

Every personalisation field in the Notify template must be in the map. If one is missing, the send fails.

## 6. Call the dispatcher from the event's `submitted()` callback

In the event class under `caseworker/event/`, inject the dispatcher and call its `sendTo*` methods from `submitted()`, registered with `.submittedCallback(this::submitted)`. That callback runs after CCD has saved the case. `CaseworkerLinkCase`, `CaseworkerContactParties` and `CaseworkerSendOrder` all work this way. See [adding-an-event.md](adding-an-event.md) for event classes in general.

- Don't send from `aboutToSubmit()`. The case isn't saved yet at that point, and a later validation error would leave a notification sent for a change that never happened.
- Call only the parties that apply to the event, usually from the caseworker's selections. `CaseworkerLinkCase` checks `cicCase.getSubjectCIC()`, `getApplicantCIC()` and `getRepresentativeCIC()` before each call.
- `NotificationServiceCIC` logs a failed send and rethrows it as `NotificationException`. Wrap the dispatcher calls in a `try` in `submitted()`, catch the exception, and return a `SubmittedCallbackResponse` whose confirmation header says the notification failed and should be resent, as `CaseworkerLinkCase` does. The CCD event still completes. With one `try` around all parties, a failure stops the parties after it.

## 7. Check the correspondence is recorded

You don't write to `case_correspondences` yourself. [`NotificationServiceCIC`](../src/main/java/uk/gov/hmcts/sptribs/notification/NotificationServiceCIC.java) does it after Notify accepts each send:

- After an email, `saveEmailCorrespondence()` saves a row with `correspondence_type` `Email`.
- After a letter, `saveLetterCorrespondence()` saves a row with `correspondence_type` `Letter`, from the Notify `SendLetterResponse`.
- Both render a PDF copy of the message through the PDF service client, upload it to CDAM, and store its URLs on the row.

The row's `event_type` is the template name, so the `TemplateName` you pick in step 4 is what appears in the correspondence record. `receiving_party` is the `Party` you passed to `sendEmail`, and is always null for letters. The table is created by the Flyway migrations in [`src/main/resources/db/migration/`](../src/main/resources/db/migration/) and accessed through `CorrespondenceRepository`. The case's Correspondence tab reads it through `CicCaseView`. A new notification doesn't need a migration.

If a send fails, no row is written and nothing retries it.

## 8. Write tests

Dispatcher tests live in [`src/test/java/uk/gov/hmcts/sptribs/notification/dispatcher/`](../src/test/java/uk/gov/hmcts/sptribs/notification/dispatcher/), named `<Dispatcher>Test` (for example `CaseLinkedNotificationTest`). They use `@ExtendWith(MockitoExtension.class)`, `@Mock` for `NotificationServiceCIC` and `NotificationHelper`, and `@InjectMocks` for the dispatcher.

1. Add a test class for the dispatcher and cover each party with:
   - `EMAIL` preference: `sendEmail` is called with the expected request and `Party` (or `null`)
   - `POST` preference: `sendLetter` is called with the `_POST` template
   - citizen dashboard on and off, if you use `_NEW_CD`: the right variant is chosen and `DashboardLink` is in the map
2. Extend the event's unit test (in `src/test/java/uk/gov/hmcts/sptribs/caseworker/event/`, for example `CaseworkerLinkCaseTest`) to check that `submitted()` calls the dispatcher for the right parties and returns the failure header when it throws.
3. Check by hand that every new `TemplateName` constant has a matching key under `templatesCIC`. `NotificationServiceCicIT` builds its own template map, so no test covers this.

See [testing.md](testing.md) for the test suites.

## Verify

1. Run the dispatcher and event tests:

   ```bash
   ./gradlew test --tests '*MyNewEventNotificationTest'
   ```

2. Run the service locally with `./gradlew bootWithCCD` (needs `az login`). See [running-locally.md](running-locally.md). The task loads environment variables from `.local-env` if present, otherwise `.aat-env`, which `loadEnvSecrets` pulls from the `sptribs-case-api-dot-env` secret in the `sptribs-aat` Key Vault. If that environment doesn't set `UK_GOV_NOTIFY_API_KEY_1`, the key falls back to `dummy` and every send fails with a `NotificationException` in the log. Trigger the event in ExUI on a test case, then check that:
   - the message appears in the Notify dashboard for that service
   - a new row exists in `case_correspondences` with the expected `event_type` and `correspondence_type`, for example `SELECT event_type, correspondence_type, receiving_party FROM case_correspondences WHERE case_reference_number = <16-digit ref>;`

## See also

- [Notifications and correspondence](notifications.md): the dispatcher pattern, the correspondence table, Welsh, and failure behaviour
- [Documents and bundles](documents-and-bundles.md): documents and CDAM, where correspondence PDFs are stored
- [`notification/dispatcher/`](../src/main/java/uk/gov/hmcts/sptribs/notification/dispatcher/): existing dispatchers to copy
