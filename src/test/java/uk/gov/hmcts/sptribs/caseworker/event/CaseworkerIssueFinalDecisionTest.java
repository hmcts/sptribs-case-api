package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueFinalDecisionSelectTemplate;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.caseworker.model.NoticeOption;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.FinalDecisionTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_ISSUE_FINAL_DECISION;

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
    void shouldShowCorrectMessageWhenSubmitted() {
        //Given
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        final CicCase cicCase = CicCase.builder().notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .build();
        final CaseData caseData = caseData();
        caseData.setCicCase(cicCase);
        details.setData(caseData);

        //When
        SubmittedCallbackResponse response = issueFinalDecision.submitted(details, beforeDetails);

        //Then
        assertThat(response.getConfirmationHeader())
            .contains("Respondent");
    }

    @Test
    void shouldCloseCase() {
        //Given
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final CaseData caseData = caseData();
        details.setData(caseData);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = issueFinalDecision.aboutToSubmit(details, beforeDetails);

        //Then
        assertThat(response.getState())
            .isEqualTo(CaseClosed);
    }
}
