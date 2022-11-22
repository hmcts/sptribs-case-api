package uk.gov.hmcts.sptribs.caseworker.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBJECT_ADDRESS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASEWORKER_USER_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SUBJECT_EMAIL;

@ExtendWith(MockitoExtension.class)
public class MessageUtilTest {

    @Test
    void shouldSuccessfullySendOrderWithEmail() {
        //Given
        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .email(TEST_SUBJECT_EMAIL)
            .respondantEmail(TEST_CASEWORKER_USER_EMAIL)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .representativeEmailAddress(TEST_SOLICITOR_EMAIL)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .build();

        //When
        StringBuilder result = MessageUtil.getEmailMessage(cicCase);

        //Then
        assertThat(result).contains(SubjectCIC.SUBJECT.getLabel());
    }

    @Test
    void shouldBeNull() {
        //Given
        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .build();

        //When
        StringBuilder resultEmail = MessageUtil.getEmailMessage(cicCase);
        StringBuilder resultPost = MessageUtil.getPostMessage(cicCase);

        //Then
        assertThat(resultEmail).isNull();
        assertThat(resultPost).isNull();
    }

    @Test
    void shouldSuccessfullySendOrderWithPost() {
        //Given
        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .address(SUBJECT_ADDRESS)
            .respondantEmail(TEST_CASEWORKER_USER_EMAIL)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .representativeAddress(SOLICITOR_ADDRESS)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .build();

        //When
        StringBuilder result = MessageUtil.getPostMessage(cicCase);

        //Then
        assertThat(result).contains(SubjectCIC.SUBJECT.getLabel());
    }

}
