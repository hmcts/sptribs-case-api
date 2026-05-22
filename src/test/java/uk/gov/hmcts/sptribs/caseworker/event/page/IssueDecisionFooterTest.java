package uk.gov.hmcts.sptribs.caseworker.event.page;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.DecisionTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.document.CaseDataDocumentService;
import uk.gov.hmcts.sptribs.document.content.DecisionTemplateContent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssueDecisionFooterTest {

    @InjectMocks
    private IssueDecisionFooter issueDecisionFooter;

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private DecisionTemplateContent decisionTemplateContent;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Test
    void shouldRenderDocumentWithoutError() {
        //Given
        final CaseIssueDecision caseIssueDecision = new CaseIssueDecision();
        caseIssueDecision.setIssueDecisionTemplate(DecisionTemplate.ELIGIBILITY);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder()
                .caseIssueDecision(caseIssueDecision)
                .build();
        caseDetails.setData(caseData);
        Document document = new Document();
        when(caseDataDocumentService.renderDocument(
                anyMap(),
                any(),
                eq(DecisionTemplate.ELIGIBILITY.getId()),
                eq(LanguagePreference.ENGLISH), any(), any()))
                .thenReturn(document);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = issueDecisionFooter.midEvent(caseDetails, caseDetails);

        //Then
        assertThat(response.getErrors()).isNull();
        assertThat(caseIssueDecision.getIssueDecisionDraft()).isEqualTo(document);
    }
}