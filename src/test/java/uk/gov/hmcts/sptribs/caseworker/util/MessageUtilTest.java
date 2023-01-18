package uk.gov.hmcts.sptribs.caseworker.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;

import java.util.HashSet;
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
    void shouldSuccessfullyGenerateEmailMessage() {
        //Given
        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .email(TEST_SUBJECT_EMAIL)
            .contactPreferenceType(ContactPreferenceType.EMAIL)
            .respondantEmail(TEST_CASEWORKER_USER_EMAIL)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .representativeContactDetailsPreference(ContactPreferenceType.EMAIL)
            .representativeEmailAddress(TEST_SOLICITOR_EMAIL)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .build();

        //When
        String result = MessageUtil.getEmailMessage(cicCase);

        //Then
        assertThat(result).contains(SubjectCIC.SUBJECT.getLabel());
    }

    @Test
    void shouldSuccessfullyGenerateEmailMessageWithParties() {
        //Given
        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .email(TEST_SUBJECT_EMAIL)
            .contactPreferenceType(ContactPreferenceType.EMAIL)
            .respondantEmail(TEST_CASEWORKER_USER_EMAIL)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .representativeEmailAddress(TEST_SOLICITOR_EMAIL)
            .representativeContactDetailsPreference(ContactPreferenceType.EMAIL)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .build();
        Set<NotificationParties> parties = new HashSet<>();
        parties.add(NotificationParties.RESPONDENT);
        parties.add(NotificationParties.REPRESENTATIVE);
        parties.add(NotificationParties.SUBJECT);

        //When
        String result = MessageUtil.getEmailMessage(cicCase, parties);

        //Then
        assertThat(result).contains(SubjectCIC.SUBJECT.getLabel());
    }


    @Test
    void shouldBeNullWithoutContactPreferenceType() {
        //Given
        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .contactPreferenceType(ContactPreferenceType.POST)
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .build();
        Set<NotificationParties> parties = new HashSet<>();
        parties.add(NotificationParties.SUBJECT);

        //When
        String resultEmail = MessageUtil.getEmailMessage(cicCase);

        //Then
        assertThat(resultEmail).isNull();
    }


    @Test
    void shouldBeNullWithoutContactPreferenceTypeRepresentative() {
        //Given
        final CicCase cicCase = CicCase.builder()
            .representativeFullName(TEST_FIRST_NAME)
            .representativeContactDetailsPreference(ContactPreferenceType.POST)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .build();
        Set<NotificationParties> parties = new HashSet<>();
        parties.add(NotificationParties.REPRESENTATIVE);

        //When
        String resultEmail = MessageUtil.getEmailMessage(cicCase);

        //Then
        assertThat(resultEmail).isNull();
    }

    @Test
    void shouldBeNull() {
        //Given
        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .build();

        //When
        String resultEmail = MessageUtil.getEmailMessage(cicCase);
        String resultPost = MessageUtil.getPostMessage(cicCase);

        //Then
        assertThat(resultEmail).isNull();
        assertThat(resultPost).isNull();
    }

    @Test
    void shouldSuccessfullyGeneratePostMessage() {
        //Given
        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .address(SUBJECT_ADDRESS)
            .contactPreferenceType(ContactPreferenceType.POST)
            .respondantEmail(TEST_CASEWORKER_USER_EMAIL)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .representativeAddress(SOLICITOR_ADDRESS)
            .representativeContactDetailsPreference(ContactPreferenceType.POST)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .build();

        //When
        String result = MessageUtil.getPostMessage(cicCase);

        //Then
        assertThat(result).contains(SubjectCIC.SUBJECT.getLabel());
    }

    @Test
    void shouldSuccessfullyGeneratePostMessageWithParties() {
        //Given
        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .address(SUBJECT_ADDRESS)
            .contactPreferenceType(ContactPreferenceType.POST)
            .respondantEmail(TEST_CASEWORKER_USER_EMAIL)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .representativeAddress(SOLICITOR_ADDRESS)
            .representativeContactDetailsPreference(ContactPreferenceType.POST)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .build();
        Set<NotificationParties> parties = new HashSet<>();
        parties.add(NotificationParties.SUBJECT);
        parties.add(NotificationParties.RESPONDENT);
        parties.add(NotificationParties.REPRESENTATIVE);
        //When
        String result = MessageUtil.getPostMessage(cicCase, parties);

        //Then
        assertThat(result).contains(SubjectCIC.SUBJECT.getLabel());
    }

    @Test
    void shouldSuccessfullyGenerateWholeMessageWithPostAndEmail() {
        //Given
        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .address(SUBJECT_ADDRESS)
            .contactPreferenceType(ContactPreferenceType.POST)
            .respondantEmail(TEST_CASEWORKER_USER_EMAIL)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .representativeContactDetailsPreference(ContactPreferenceType.EMAIL)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .build();
        //When
        String result = MessageUtil.generateWholeMessage(cicCase, "header", "footer");

        //Then
        assertThat(result).contains("header");
        assertThat(result).contains("footer");
        assertThat(result).contains(SubjectCIC.SUBJECT.getLabel());
    }

    @Test
    void shouldSuccessfullyGenerateWholeMessageWithPost() {
        //Given
        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .address(SUBJECT_ADDRESS)
            .contactPreferenceType(ContactPreferenceType.POST)
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .build();
        //When
        String result = MessageUtil.generateWholeMessage(cicCase, "header", "footer");

        //Then
        assertThat(result).contains("header");
        assertThat(result).contains("footer");
        assertThat(result).contains(SubjectCIC.SUBJECT.getLabel());
    }

    @Test
    void shouldSuccessfullyGenerateWholeMessageWithEmail() {
        //Given
        final CicCase cicCase = CicCase.builder()
            .respondantEmail(TEST_CASEWORKER_USER_EMAIL)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .representativeContactDetailsPreference(ContactPreferenceType.EMAIL)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .build();

        //When
        String result = MessageUtil.generateWholeMessage(cicCase, "header", "footer");

        //Then
        assertThat(result).contains("header");
        assertThat(result).contains("footer");
        assertThat(result).contains(RespondentCIC.RESPONDENT.getLabel());
    }

    @Test
    void shouldSuccessfullyGenerateIssueDecisionMessage() {
        //Given
        final CicCase cicCase = CicCase.builder()
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .representativeEmailAddress(TEST_SOLICITOR_EMAIL)
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .build();

        //When
        String result = MessageUtil.generateIssueDecisionMessage(cicCase);

        //Then
        assertThat(result).contains("# Decision notice issued");
    }

    @Test
    void shouldSuccessfullyGenerateSimpleHearingMessage() {
        //Given
        Set<NotificationParties> parties = new HashSet<>();
        parties.add(NotificationParties.SUBJECT);
        parties.add(NotificationParties.RESPONDENT);
        parties.add(NotificationParties.REPRESENTATIVE);

        //When
        String result = MessageUtil.generateSimpleMessage(parties);

        //Then
        assertThat(result).contains("Subject");
    }
}
