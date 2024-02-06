package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.caseworker.util.EventUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.DecisionTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssueDecisionSelectTemplateTest {

    @InjectMocks
    private IssueDecisionSelectTemplate issueDecisionSelectTemplate;

    @Mock
    private CaseIssueDecision caseIssueDecision;

    private final DecisionTemplate decisionTemplate = DecisionTemplate.ELIGIBILITY;

    @Test
    void midEventCorrectlySetsDecisionTemplate() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder()
            .caseIssueDecision(caseIssueDecision)
            .build();

        when(caseIssueDecision.getIssueDecisionTemplate()).thenReturn(decisionTemplate);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = issueDecisionSelectTemplate.midEvent(caseDetails, caseDetails);
        assertThat(response.getData().getDecisionMainContent()).isEqualTo(DocmosisTemplateConstants.ELIGIBILITY_MAIN_CONTENT);
    }

    @Test
    void validateGetMainContentIsCalledOnce() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder()
            .caseIssueDecision(caseIssueDecision)
            .build();

        when(caseIssueDecision.getIssueDecisionTemplate()).thenReturn(decisionTemplate);
        caseDetails.setData(caseData);

        try (MockedStatic<EventUtil> mockedEventUtils = Mockito.mockStatic(EventUtil.class)) {
            mockedEventUtils.when(() -> EventUtil.getMainContent(decisionTemplate))
                .thenReturn(DocmosisTemplateConstants.ELIGIBILITY_MAIN_CONTENT);

            issueDecisionSelectTemplate.midEvent(caseDetails, caseDetails);

            mockedEventUtils.verify(() ->  EventUtil.getMainContent(decisionTemplate), times(1));
        }
    }
}
