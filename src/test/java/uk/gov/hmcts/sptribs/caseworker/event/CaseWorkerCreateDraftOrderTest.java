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
import uk.gov.hmcts.sptribs.caseworker.service.OrderService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.event.page.DraftOrderMainContentPage;
import uk.gov.hmcts.sptribs.common.event.page.PreviewDraftOrder;
import uk.gov.hmcts.sptribs.document.CaseDataDocumentService;
import uk.gov.hmcts.sptribs.document.content.PreviewDraftOrderTemplateContent;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_CREATE_DRAFT_ORDER;

@ExtendWith(MockitoExtension.class)
class CaseWorkerCreateDraftOrderTest {

    @InjectMocks
    private CaseWorkerCreateDraftOrder caseWorkerDraftOrder;
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

    @Mock
    private OrderService orderService;

    @Test
    void shouldAddConfigurationToConfigBuilder() {

        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseWorkerDraftOrder.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_CREATE_DRAFT_ORDER);
    }

    @Test
    void shouldSuccessfullySaveDraftOrder() {

        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseWorkerDraftOrder.aboutToSubmit(updatedCaseDetails, beforeDetails);
        assertThat(response).isNotNull();


        SubmittedCallbackResponse draftCreatedResponse = caseWorkerDraftOrder.draftCreated(updatedCaseDetails, beforeDetails);
        //  Then
        assertThat(draftCreatedResponse).isNotNull();

    }


    @Test
    void shouldSuccessfullySaveDraftOrderWithCurrentDateAndTime() {

        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseWorkerDraftOrder.aboutToSubmit(updatedCaseDetails, beforeDetails);
        assertThat(response).isNotNull();
        SubmittedCallbackResponse draftCreatedResponse = caseWorkerDraftOrder.draftCreated(updatedCaseDetails, beforeDetails);
        //  Then
        assertThat(draftCreatedResponse).isNotNull();

    }

    @Test
    void shouldSuccessfullyShowPreviewOrderWithTemplate() {

        CicCase cicCase = CicCase.builder().orderTemplate(OrderTemplate.CIC6_GENERAL_DIRECTIONS).build();

        //Given
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);

        caseDetails.setData(caseData);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = caseWorkerDraftOrder.midEvent(caseDetails, caseDetails);

        //Then
        assertThat(response.getErrors()).isNull();
    }

}

