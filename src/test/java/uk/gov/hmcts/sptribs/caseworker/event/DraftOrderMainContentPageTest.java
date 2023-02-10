package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.event.page.DraftOrderMainContentPage;
import uk.gov.hmcts.sptribs.common.event.page.PreviewDraftOrder;
import uk.gov.hmcts.sptribs.document.CaseDataDocumentService;
import uk.gov.hmcts.sptribs.document.content.PreviewDraftOrderTemplateContent;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class DraftOrderMainContentPageTest {
    @Mock
    private CaseDataDocumentService caseDataDocumentService;
    @Mock
    PreviewDraftOrderTemplateContent previewDraftOrderTemplateContent;
    @InjectMocks
    private DraftOrderMainContentPage draftOrderMainContentPage;
    @Mock
    private PreviewDraftOrder previewOrder;


    @InjectMocks
    private PreviewDraftOrder previewDraftOrder;


    @Test
    void shouldSuccessfullyShowPreviewOrderWithTemplate() {

        CicCase cicCase = new CicCase();
        cicCase.setAnOrderTemplates(OrderTemplate.CIC6_GENERAL_DIRECTIONS);


        //Given
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);

        caseDetails.setData(caseData);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = draftOrderMainContentPage.midEvent(caseDetails, caseDetails);

        //Then
        assertThat(response.getErrors()).isNull();
    }


}

