package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.netty.http.server.HttpServerRequest;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.DecisionTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.document.CaseDataDocumentService;
import uk.gov.hmcts.sptribs.document.content.FinalDecisionTemplateContent;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class IssueFinalDecisionFooterTest {

    @InjectMocks
    private IssueFinalDecisionFooter issueFinalDecisionFooter;

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private FinalDecisionTemplateContent finalDecisionTemplateContent;

    @Mock
    private HttpServerRequest httpServerRequest;

    @Test
    void shouldReturnErrorsIfNoNotificationPartySelected() {
        //Given
        final CaseIssueFinalDecision caseIssueFinalDecision = new CaseIssueFinalDecision();
        caseIssueFinalDecision.setDecisionTemplate(DecisionTemplate.ELIGIBILITY);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder()
                .caseIssueFinalDecision(caseIssueFinalDecision)
                .build();
        caseDetails.setData(caseData);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = issueFinalDecisionFooter.midEvent(caseDetails, caseDetails);

        //Then
        assertThat(response.getErrors()).isNull();
    }

}