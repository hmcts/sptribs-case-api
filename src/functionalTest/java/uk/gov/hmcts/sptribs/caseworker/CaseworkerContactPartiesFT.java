package uk.gov.hmcts.sptribs.caseworker;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.notification.model.Party;
import uk.gov.hmcts.sptribs.notification.persistence.CorrespondenceEntity;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CONTACT_PARTIES;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseworkerContactPartiesFT extends FunctionalTestSuite {

    private static final String ABOUT_TO_START_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-contact-parties-about-to-start.json";
    private static final String ABOUT_TO_START_RESPONSE =
        "classpath:responses/response-caseworker-contact-parties-about-to-start.json";

    private static final String SUBMITTED_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-contact-parties-submitted.json";

    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    @Test
    public void shouldPrepareContactPartiesDocumentListInAboutToStartCallback() throws Exception {

        final Map<String, Object> caseData = caseData(ABOUT_TO_START_REQUEST);

        final Response response = triggerCallback(caseData, CASEWORKER_CONTACT_PARTIES, ABOUT_TO_START_URL, false);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(ABOUT_TO_START_RESPONSE)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("recipientSelections")
    public void shouldSendNotificationsInSubmittedCallback(String scenario, Set<Party> selectedParties,
                                                            String expectedRecipients) throws Exception {
        final Map<String, Object> caseData = caseData(SUBMITTED_REQUEST);
        caseData.put("cicCaseNotifyPartySubject", selectedParties.contains(Party.SUBJECT) ? List.of("SubjectCIC") : List.of());
        caseData.put("cicCaseNotifyPartyApplicant", selectedParties.contains(Party.APPLICANT) ? List.of("ApplicantCIC") : List.of());
        caseData.put("cicCaseNotifyPartyRepresentative",
            selectedParties.contains(Party.REPRESENTATIVE) ? List.of("RepresentativeCIC") : List.of());

        final Response response = triggerCallback(caseData, CASEWORKER_CONTACT_PARTIES, SUBMITTED_URL, false);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isEqualTo("# Message sent \\n## A notification has been sent to: " + expectedRecipients);

        long testCaseRef = Long.parseLong(caseData.get("hyphenatedCaseRef").toString().replace("-", ""));

        List<CorrespondenceEntity> correspondenceEntities = caseCorrespondencesFTDataManager.getCorrespondenceEntities(testCaseRef);
        assertThat(correspondenceEntities).hasSize(selectedParties.size());
        assertThat(correspondenceEntities.stream().map(CorrespondenceEntity::getReceivingParty).toList())
            .containsExactlyInAnyOrderElementsOf(selectedParties);

        Map<Party, String> emailAddresses = Map.of(
            Party.SUBJECT, "test@email.com",
            Party.APPLICANT, "AutoTestApplicant@mail.com",
            Party.REPRESENTATIVE, "AutoTestRepresentative@mail.com");

        for (CorrespondenceEntity correspondence : correspondenceEntities) {
            assertThat(correspondence.getId()).isNotNull();
            assertThat(correspondence.getCaseReferenceNumber()).isEqualTo(testCaseRef);
            assertThat(correspondence.getEventType()).startsWith("CONTACT_PARTIES_EMAIL");
            assertThat(correspondence.getSentOn()).isNotNull();
            assertThat(correspondence.getSentFrom()).isNotNull();
            assertThat(correspondence.getSentTo()).isEqualTo(emailAddresses.get(correspondence.getReceivingParty()));
            assertThat(correspondence.getCorrespondenceType()).isEqualTo("Email");
            assertThat(correspondence.getDocumentUrl()).isNotNull();
            assertThat(correspondence.getDocumentFilename()).isNotNull();
            assertThat(correspondence.getDocumentBinaryUrl()).isNotNull();
            assertThat(correspondenceDocumentFTDataManager.getCorrespondenceDocuments(correspondence.getId())).isEmpty();
        }
    }

    private static Stream<Arguments> recipientSelections() {
        return Stream.of(
            Arguments.of("subject", Set.of(Party.SUBJECT), "Subject"),
            Arguments.of("applicant", Set.of(Party.APPLICANT), "Applicant"),
            Arguments.of("representative", Set.of(Party.REPRESENTATIVE), "Representative"),
            Arguments.of("subject and applicant", Set.of(Party.SUBJECT, Party.APPLICANT), "Subject, Applicant"),
            Arguments.of("subject and representative", Set.of(Party.SUBJECT, Party.REPRESENTATIVE), "Subject, Representative"),
            Arguments.of("applicant and representative", Set.of(Party.APPLICANT, Party.REPRESENTATIVE), "Representative, Applicant"),
            Arguments.of("all three", Set.of(Party.SUBJECT, Party.APPLICANT, Party.REPRESENTATIVE),
                "Subject, Representative, Applicant")
        );
    }
}
