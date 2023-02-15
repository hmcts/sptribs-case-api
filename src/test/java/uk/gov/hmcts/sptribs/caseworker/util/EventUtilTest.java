package uk.gov.hmcts.sptribs.caseworker.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBJECT_ADDRESS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASEWORKER_USER_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_NAME;

@ExtendWith(MockitoExtension.class)
public class EventUtilTest {

    @Test
    void shouldSuccessfullyGetId() {
        //Given
        final String selected = "0-Draft";

        //When
        String result = EventUtil.getId(selected);

        //Then
        assertThat(result).isNotNull();
    }

    @Test
    void shouldGetRecipients() {
        //Given
        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .address(SUBJECT_ADDRESS)
            .respondentEmail(TEST_CASEWORKER_USER_EMAIL)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .representativeAddress(SOLICITOR_ADDRESS)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .notifyPartyApplicant(Set.of(ApplicantCIC.APPLICANT_CIC))
            .build();

        //When
        String result = EventUtil.getRecipients(cicCase);
        //Then
        assertThat(result).contains(SubjectCIC.SUBJECT.getLabel());
    }

    @Test
    void shouldGetNotificationParties() {
        //Given
        final CicCase cicCase = CicCase.builder()
            .representativeCIC(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .subjectCIC(Set.of(SubjectCIC.SUBJECT))
            .build();

        //When
        Set<NotificationParties> result = EventUtil.getNotificationParties(cicCase);

        //Then
        assertThat(result).hasSize(2);
    }

    @Test
    void shouldBeNull() {
        //Given
        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .build();

        //When
        String result = EventUtil.getRecipients(cicCase);

        //Then
        assertThat(result).isNull();
    }


    @Test
    void shouldSuccessfullyParseHyhen() {
        //Given
        final String selected = "0-Draft";

        //When
        String result = EventUtil.parseHyphen(selected,0);

        //Then
        assertThat(result).isEqualTo("0");
    }
}
