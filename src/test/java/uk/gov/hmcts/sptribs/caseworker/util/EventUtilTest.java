package uk.gov.hmcts.sptribs.caseworker.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.DecisionTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.ELIGIBILITY_MAIN_CONTENT;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.ME_DMI_MAIN_CONTENT;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.ME_JOINT_MAIN_CONTENT;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.PRO_FORMA_MAIN_CONTENT;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.QUANTUM_MAIN_CONTENT;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.RULE27_MAIN_CONTENT;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.STRIKE_OUT_NOTICE_MAIN_CONTENT;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.STRIKE_OUT_WARNING_MAIN_CONTENT;
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
    void shouldSuccessfullyParseHyphen() {
        //Given
        final String selected = "0-Draft";

        //When
        String result = EventUtil.parseHyphen(selected, 0);

        //Then
        assertThat(result).isEqualTo("0");
    }

    @Test
    void shouldSuccessfullyGetMainContentEligibility() {
        //Given
        final DecisionTemplate decisionTemplate = DecisionTemplate.ELIGIBILITY;

        //When
        String result = EventUtil.getMainContent(decisionTemplate);

        //Then
        assertThat(result).isEqualTo(ELIGIBILITY_MAIN_CONTENT);

    }


    @Test
    void shouldSuccessfullyGetMainContentQuantum() {
        //Given
        final DecisionTemplate decisionTemplate = DecisionTemplate.QUANTUM;

        //When
        String result = EventUtil.getMainContent(decisionTemplate);

        //Then
        assertThat(result).isEqualTo(QUANTUM_MAIN_CONTENT);

    }

    @Test
    void shouldSuccessfullyGetMainContentBlank() {
        //Given
        final DecisionTemplate decisionTemplate = DecisionTemplate.BLANK_DECISION_NOTICE;

        //When
        String result = EventUtil.getMainContent(decisionTemplate);

        //Then
        assertThat(result).isEmpty();

    }

    @Test
    void shouldSuccessfullyGetMainContentRule() {
        //Given
        final DecisionTemplate decisionTemplate = DecisionTemplate.RULE_27;

        //When
        String result = EventUtil.getMainContent(decisionTemplate);

        //Then
        assertThat(result).isEqualTo(RULE27_MAIN_CONTENT);

    }

    @Test
    void shouldSuccessfullyGetMainContentStrikeOutNotice() {
        //Given
        final DecisionTemplate decisionTemplate = DecisionTemplate.STRIKE_OUT_DECISION_NOTICE;

        //When
        String result = EventUtil.getMainContent(decisionTemplate);

        //Then
        assertThat(result).isEqualTo(STRIKE_OUT_NOTICE_MAIN_CONTENT);

    }

    @Test
    void shouldSuccessfullyGetMainContentStrikeOutWarning() {
        //Given
        final DecisionTemplate decisionTemplate = DecisionTemplate.STRIKE_OUT_WARNING;

        //When
        String result = EventUtil.getMainContent(decisionTemplate);

        //Then
        assertThat(result).isEqualTo(STRIKE_OUT_WARNING_MAIN_CONTENT);

    }

    @Test
    void shouldSuccessfullyGetMainContentMedicalEvidenceDmi() {
        //Given
        final DecisionTemplate decisionTemplate = DecisionTemplate.ME_DMI_REPORTS;

        //When
        String result = EventUtil.getMainContent(decisionTemplate);

        //Then
        assertThat(result).isEqualTo(ME_DMI_MAIN_CONTENT);

    }

    @Test
    void shouldSuccessfullyGetMainContentMEJoint() {
        //Given
        final DecisionTemplate decisionTemplate = DecisionTemplate.ME_JOINT_INSTRUCTION;

        //When
        String result = EventUtil.getMainContent(decisionTemplate);

        //Then
        assertThat(result).isEqualTo(ME_JOINT_MAIN_CONTENT);

    }

    @Test
    void shouldSuccessfullyGetMainContentProForma() {
        //Given
        final DecisionTemplate decisionTemplate = DecisionTemplate.PRO_FORMA_SUMMONS;

        //When
        String result = EventUtil.getMainContent(decisionTemplate);

        //Then
        assertThat(result).isEqualTo(PRO_FORMA_MAIN_CONTENT);

    }
}
