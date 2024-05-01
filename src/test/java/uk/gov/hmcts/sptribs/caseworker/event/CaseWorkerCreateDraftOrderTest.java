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
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderContentCIC;
import uk.gov.hmcts.sptribs.caseworker.service.OrderService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

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
        final CicCase cicCase = CicCase.builder()
            .orderTemplateIssued(Document.builder().filename("a--b--02-02-2002 11:11:11.pdf").build()).build();
        final CaseData caseData = caseData();
        caseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        caseData.setDraftOrderContentCIC(DraftOrderContentCIC.builder()
            .orderTemplate(OrderTemplate.CIC6_GENERAL_DIRECTIONS).build());

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseWorkerDraftOrder.aboutToSubmit(updatedCaseDetails, beforeDetails);
        assertThat(response).isNotNull();

        SubmittedCallbackResponse draftCreatedResponse = caseWorkerDraftOrder.submitted(updatedCaseDetails, beforeDetails);
        //  Then
        assertThat(draftCreatedResponse).isNotNull();

    }

    @Test
    void shouldSuccessfullySaveDraftOrderWithCurrentDateAndTime() {

        //Given
        final CicCase cicCase = CicCase.builder()
            .orderTemplateIssued(Document.builder().filename("a--b--02-02-2002 11:11:11.pdf").build()).build();
        final CaseData caseData = caseData();
        caseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        caseData.setDraftOrderContentCIC(DraftOrderContentCIC.builder().orderTemplate(OrderTemplate.CIC6_GENERAL_DIRECTIONS).build());
        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseWorkerDraftOrder.aboutToSubmit(updatedCaseDetails, beforeDetails);
        assertThat(response).isNotNull();
        SubmittedCallbackResponse draftCreatedResponse = caseWorkerDraftOrder.submitted(updatedCaseDetails, beforeDetails);
        //  Then
        assertThat(draftCreatedResponse).isNotNull();

    }

    @Test
    void shouldSuccessfullyShowPreviewOrderWithTemplate() {

        //Given
        DraftOrderContentCIC orderContentCIC = DraftOrderContentCIC.builder()
            .orderTemplate(OrderTemplate.CIC6_GENERAL_DIRECTIONS).build();

        final CicCase cicCase = CicCase.builder()
            .orderTemplateIssued(Document.builder().filename("a--b--02-02-2002 11:11:11.pdf").build()).build();
        final CaseData caseData = CaseData.builder()
            .draftOrderContentCIC(orderContentCIC)
            .build();
        caseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);

        caseDetails.setData(caseData);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = caseWorkerDraftOrder.midEvent(caseDetails, caseDetails);

        //Then
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldSuccessfullySave2DraftOrder() {

        //Given
        final CicCase cicCase = CicCase.builder()
            .orderTemplateIssued(Document.builder().filename("a--b--02-02-2002 11:11:11.pdf").build()).build();
        final CaseData caseData = caseData();
        caseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final DraftOrderContentCIC orderContentCIC = DraftOrderContentCIC.builder()
            .orderTemplate(OrderTemplate.CIC7_ME_DMI_REPORTS).build();
        caseData.setDraftOrderContentCIC(orderContentCIC);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseWorkerDraftOrder.aboutToSubmit(updatedCaseDetails, beforeDetails);
        assertThat(response).isNotNull();

        //When
        SubmittedCallbackResponse draftCreatedResponse = caseWorkerDraftOrder.submitted(updatedCaseDetails, beforeDetails);

        //  Then
        assertThat(draftCreatedResponse).isNotNull();

        final CicCase cicCase2 = CicCase.builder()
            .orderTemplateIssued(Document.builder().filename("a--b--02-02-2002 11:11:11.pdf").build()).build();
        caseData.setCicCase(cicCase2);
        updatedCaseDetails.setData(caseData);
        caseData.setDraftOrderContentCIC(orderContentCIC);
        AboutToStartOrSubmitResponse<CaseData, State> response2 =
            caseWorkerDraftOrder.aboutToSubmit(updatedCaseDetails, beforeDetails);
        //  Then
        assertThat(response2).isNotNull();
    }
}

