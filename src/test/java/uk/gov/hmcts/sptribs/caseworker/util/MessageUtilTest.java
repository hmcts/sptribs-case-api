package uk.gov.hmcts.sptribs.caseworker.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.caseworker.model.ContactParties;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASEWORKER_USER_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_NAME;

@ExtendWith(MockitoExtension.class)
public class MessageUtilTest {

    @Test
    void shouldSuccessfullyGenerateSimpleHeaderAndFooterMessage() {
        //When
        String result = MessageUtil.generateSimpleMessage("header", "footer");

        //Then
        assertThat(result).contains("header").contains("footer");
    }

    @Test
    void shouldSuccessfullyGenerateSimpleMessageWithFooter() {
        //Given
        final CicCase cicCase = CicCase.builder()
            .respondentEmail(TEST_CASEWORKER_USER_EMAIL)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .representativeContactDetailsPreference(ContactPreferenceType.EMAIL)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .build();

        //When
        String result = MessageUtil.generateSimpleMessage(cicCase, "header", "footer");

        //Then
        assertThat(result).contains("header").contains("footer").contains("Respondent");
    }

    @Test
    void shouldSuccessfullyGenerateIssueDecisionMessageWithCicCase() {
        //Given
        final CicCase cicCase = CicCase.builder()
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .build();

        //When
        String result = MessageUtil.generateSimpleMessage(cicCase);

        //Then
        assertThat(result).contains("Subject");
    }

    @Test
    void shouldSuccessfullyGenerateSimpleHearingMessage() {
        //Given
        Set<NotificationParties> parties = new HashSet<>();
        parties.add(NotificationParties.SUBJECT);
        parties.add(NotificationParties.RESPONDENT);
        parties.add(NotificationParties.REPRESENTATIVE);
        parties.add(NotificationParties.APPLICANT);

        //When
        String result = MessageUtil.generateSimpleMessage(parties);

        //Then
        assertThat(result).contains("Respondent").contains("Representative").contains("Subject").contains("Applicant");
    }

    @Test
    void shouldSuccessfullyGenerateIssueDecisionMessageWithContactParties() {
        //Given
        final ContactParties contactParties = ContactParties.builder()
            .representativeContactParties(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .subjectContactParties(Set.of(SubjectCIC.SUBJECT))
            .respondent(Set.of(RespondentCIC.RESPONDENT))
            .build();
        //When
        String result = MessageUtil.generateSimpleMessage(contactParties);

        //Then
        assertThat(result).contains("Subject");
    }
}
