package uk.gov.hmcts.sptribs.caseworker.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.caseworker.model.ContactParties;
import uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.TribunalCIC;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.caseworker.util.MessageUtil.generateSimpleErrorMessage;
import static uk.gov.hmcts.sptribs.caseworker.util.MessageUtil.generateSimpleMessage;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASEWORKER_USER_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_NAME;

@ExtendWith(MockitoExtension.class)
public class MessageUtilTest {

    @Test
    void shouldSuccessfullyGenerateSimpleHeaderAndFooterMessage() {
        String result = generateSimpleMessage("header", "footer");
        assertThat(result).contains("header").contains("footer");
    }

    @Test
    void shouldSuccessfullyGenerateSimpleMessageWithFooter() {
        final CicCase cicCase = CicCase.builder()
            .respondentEmail(TEST_CASEWORKER_USER_EMAIL)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .representativeContactDetailsPreference(ContactPreferenceType.EMAIL)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .build();

        String result = generateSimpleMessage(cicCase, "header", "footer");

        assertThat(result).contains("header").contains("footer").contains("Respondent");
    }

    @Test
    void shouldSuccessfullyGenerateIssueDecisionMessageWithCicCase() {
        final CicCase cicCase = CicCase.builder()
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .build();

        String result = generateSimpleMessage(cicCase);

        assertThat(result).contains("Subject");
    }

    @Test
    void shouldSuccessfullyGenerateSimpleHearingMessage() {
        Set<NotificationParties> parties = new HashSet<>();
        parties.add(NotificationParties.SUBJECT);
        parties.add(NotificationParties.RESPONDENT);
        parties.add(NotificationParties.REPRESENTATIVE);
        parties.add(NotificationParties.APPLICANT);

        String result = generateSimpleMessage(parties);

        assertThat(result).contains("Respondent").contains("Representative").contains("Subject").contains("Applicant");
    }

    @Test
    void shouldSuccessfullyGenerateIssueDecisionMessageWithContactParties() {
        final ContactParties contactParties = ContactParties.builder()
            .representativeContactParties(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .subjectContactParties(Set.of(SubjectCIC.SUBJECT))
            .respondent(Set.of(RespondentCIC.RESPONDENT))
            .build();

        String result = generateSimpleMessage(contactParties);

        assertThat(result).contains("Subject");
    }

    @Test
    void shouldGenerateSimpleErrorMessageWithPartiesWhoCouldNotBeNotified() {
        final List<String> errors = List.of("Subject", "Applicant", "Representative", "Respondent");

        String result = generateSimpleErrorMessage(errors);

        assertThat(result)
            .isEqualTo("A notification could not be sent to: Subject, Applicant, Representative, Respondent");
    }

    @Test
    void shouldGenerateSimpleErrorMessageWithoutPartiesIfErrorsIsEmptyList() {
        final List<String> errors = emptyList();

        String result = generateSimpleErrorMessage(errors);

        assertThat(result)
            .isEqualTo("A notification could not be sent to");
    }

    @Test
    void shouldGenerateMessageInRecipientOrder() {
        Set<NotificationParties> parties = Set.of(
            NotificationParties.SUBJECT,
            NotificationParties.RESPONDENT,
            NotificationParties.REPRESENTATIVE,
            NotificationParties.APPLICANT
        );

        String result = generateSimpleMessage(parties);

        assertThat(result)
            .isEqualTo("A notification has been sent to: Subject, Respondent, Representative, Applicant");
    }

    @Test
    void shouldSuccessfullyGenerateIssueDecisionMessageWithAllParties() {
        final CicCase cicCase = CicCase.builder()
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyApplicant(Set.of(ApplicantCIC.APPLICANT_CIC))
            .build();

        String result = generateSimpleMessage(cicCase);

        assertThat(result)
            .isEqualTo("A notification has been sent to: Subject, Respondent, Representative, Applicant");
    }

    @Test
    void shouldSuccessfullyGenerateMessageWithSingleParty() {
        Set<NotificationParties> parties = Set.of(NotificationParties.SUBJECT);

        String result = generateSimpleMessage(parties);

        assertThat(result)
            .isEqualTo("A notification has been sent to: Subject");
    }

    @Test
    void shouldSuccessfullyGenerateMessageWithAllContactParties() {
        final ContactParties contactParties = ContactParties.builder()
            .subjectContactParties(Set.of(SubjectCIC.SUBJECT))
            .applicantContactParties(Set.of(ApplicantCIC.APPLICANT_CIC))
            .respondent(Set.of(RespondentCIC.RESPONDENT))
            .representativeContactParties(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .tribunal(Set.of(TribunalCIC.TRIBUNAL))
            .build();

        String result = generateSimpleMessage(contactParties);

        assertThat(result)
            .isEqualTo("A notification has been sent to: Subject, Applicant, Respondent, Representative, Tribunal");
    }

    @Test
    void shouldHandleDocumentExceptionWithFilename() {
        final Document doc = Document.builder().filename("test.pdf").build();

        String result = MessageUtil.handleDocumentException(doc, "Failed to save");

        assertThat(result).contains("test.pdf");
    }

    @Test
    void shouldHandleDocumentExceptionWithoutFilename() {
        final Document doc = Document.builder().filename(null).build();

        String result = MessageUtil.handleDocumentException(doc, "Failed to save");

        assertThat(result).contains("Error saving document with no filename");
    }

    @Test
    void shouldHandleDocumentExceptionWithEmptyFilename() {
        final Document doc = Document.builder().filename("").build();

        String result = MessageUtil.handleDocumentException(doc, "Failed to save");

        assertThat(result).contains("Error saving document with no filename");
    }

    @Test
    void shouldSuccessfullyGenerateSimpleMessageWithoutFooter() {
        final CicCase cicCase = CicCase.builder()
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .build();

        String result = generateSimpleMessage(cicCase, "header", null);

        assertThat(result)
            .isEqualTo("# header \n## A notification has been sent to: Subject");
    }

    @Test
    void shouldSuccessfullyGenerateSimpleMessageWithOnlyApplicant() {
        final CicCase cicCase = CicCase.builder()
            .notifyPartyApplicant(Set.of(ApplicantCIC.APPLICANT_CIC))
            .build();

        String result = generateSimpleMessage(cicCase);

        assertThat(result)
            .isEqualTo("A notification has been sent to: Applicant");
    }

    @Test
    void shouldSuccessfullyGenerateMessageWithOnlyTribunalContactParty() {
        final ContactParties contactParties = ContactParties.builder()
            .tribunal(Set.of(TribunalCIC.TRIBUNAL))
            .build();

        String result = generateSimpleMessage(contactParties);

        assertThat(result)
            .isEqualTo("A notification has been sent to: Tribunal");
    }

    @Test
    void shouldSuccessfullyGenerateMessageWithoutSubject() {
        Set<NotificationParties> parties = Set.of(NotificationParties.RESPONDENT);

        String result = generateSimpleMessage(parties);

        assertThat(result)
            .isEqualTo("A notification has been sent to: Respondent");
    }
}
