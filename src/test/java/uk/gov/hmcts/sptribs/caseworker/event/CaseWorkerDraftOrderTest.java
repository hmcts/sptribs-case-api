package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.event.page.PreviewDraftOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.caseworker.event.CaseWorkerCreateDraftOrder.CASEWORKER_CREATE_DRAFT_ORDER;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseWorkerDraftOrderTest {
    @InjectMocks
    private CaseWorkerCreateDraftOrder caseWorkerDraftOrder;

    @InjectMocks
    private PreviewDraftOrder previewDraftOrder;

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

        SubmittedCallbackResponse draftCreatedResponse = caseWorkerDraftOrder.draftCreated(updatedCaseDetails, beforeDetails);
        //  Then
        assertThat(draftCreatedResponse).isNotNull();

    }



    @Test
    void shouldSuccessfullyReviewCase() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final DraftOrderCIC draftOrderCIC = new DraftOrderCIC();
        draftOrderCIC.setOrderTemplate(OrderTemplate.GENERALDIRECTIONS);
        draftOrderCIC.setMainContentForGeneralDirections("content");
        caseData.setDraftOrderCIC(draftOrderCIC);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            previewDraftOrder.aboutToSubmit(updatedCaseDetails, beforeDetails);

        //  Then
        assertThat(response).isNotNull();

    }


}

