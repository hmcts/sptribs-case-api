package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueFinalDecisionSelectTemplate;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.caseworker.model.NoticeOption;
import uk.gov.hmcts.sptribs.ciccase.model.*;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.caseworker.event.CaseworkerIssueFinalDecision.CASEWORKER_ISSUE_FINAL_DECISION;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerIssueFinalDecisionTest {

    @Mock
    IssueFinalDecisionSelectTemplate issueFinalDecisionSelectTemplate;

    @InjectMocks
    private CaseworkerIssueFinalDecision issueFinalDecision;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        issueFinalDecision.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_ISSUE_FINAL_DECISION);
    }

    @Test
    void shouldSuccessfullyIssueFinalDecision() {
        //Given
        final CaseData caseData = caseData();
        final CaseIssueFinalDecision finalDecision = new CaseIssueFinalDecision();
        finalDecision.setFinalDecisionNotice(NoticeOption.CREATE_FROM_TEMPLATE);
        finalDecision.setFinalDecisionTemplate(FinalDecisionTemplate.QUANTUM);
        caseData.setCaseIssueFinalDecision(finalDecision);

        //Then
        assertThat(caseData.getCaseIssueFinalDecision().getFinalDecisionTemplate().getId()).isEqualTo("SPT_CIC2_Quantum.docx");
        assertThat(caseData.getCaseIssueFinalDecision().getFinalDecisionTemplate().getLabel()).isEqualTo("Quantum");
    }

    @Test
    void shouldSuccessfullyAddFinalDecisionRecipients() {
        //Given
        final CaseData caseData = caseData();
        final CicCase cicCase = caseData.getCicCase();
        Set<FinalDecisionRecipientSubjectCIC> subjectCICSet = new HashSet<>();
        subjectCICSet.add(FinalDecisionRecipientSubjectCIC.SUBJECT);
        cicCase.setRecipientSubjectCIC(subjectCICSet);
        Set<FinalDecisionRecipientRepresentativeCIC> representativeCICSet = new HashSet<>();
        representativeCICSet.add(FinalDecisionRecipientRepresentativeCIC.REPRESENTATIVE);
        cicCase.setRecipientRepresentativeCIC(representativeCICSet);
        Set<FinalDecisionRecipientRespondentCIC> respondentCICSet = new HashSet<>();
        respondentCICSet.add(FinalDecisionRecipientRespondentCIC.RESPONDENT);
        cicCase.setRecipientRespondentCIC(respondentCICSet);

        //Then
        assertThat(caseData.getCicCase().getRecipientSubjectCIC().contains(FinalDecisionRecipientSubjectCIC.SUBJECT));
        assertThat(caseData.getCicCase().getRecipientRepresentativeCIC().contains(FinalDecisionRecipientRepresentativeCIC.REPRESENTATIVE));
        assertThat(caseData.getCicCase().getRecipientRespondentCIC().contains(FinalDecisionRecipientRespondentCIC.RESPONDENT));
    }
}
