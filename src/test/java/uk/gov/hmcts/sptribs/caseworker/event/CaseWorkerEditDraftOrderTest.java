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
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderContentCIC;
import uk.gov.hmcts.sptribs.caseworker.service.OrderService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_EDIT_DRAFT_ORDER;


@ExtendWith(MockitoExtension.class)
class CaseWorkerEditDraftOrderTest {
    @InjectMocks
    private CaseWorkerEditDraftOrder caseWorkerEditDraftOrder;

    @Mock
    private OrderService orderService;

    @Test
    void shouldAddConfigurationToConfigBuilder() {

        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseWorkerEditDraftOrder.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_EDIT_DRAFT_ORDER);
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
        caseData.setDraftOrderContentCIC(DraftOrderContentCIC.builder().orderTemplate(OrderTemplate.CIC6_GENERAL_DIRECTIONS).build());
        DynamicListElement element = DynamicListElement.builder().code(UUID.randomUUID()).label("label").build();
        List<DynamicListElement> elements = new ArrayList<>();
        elements.add(element);
        caseData.getCicCase().setDraftOrderDynamicList(DynamicList.builder().value(element).listItems(elements).build());
        List<ListValue<DraftOrderCIC>> cicList = new ArrayList<>();
        cicList.add(ListValue.<DraftOrderCIC>builder().value(
            DraftOrderCIC.builder().build()
        ).build());
        caseData.getCicCase().setDraftOrderCICList(cicList);
        caseData.getCicCase().setOrderTemplateIssued(new Document());

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseWorkerEditDraftOrder.aboutToSubmit(updatedCaseDetails, beforeDetails);
        assertThat(response).isNotNull();
        DraftOrderCIC updatedDraftOrder = response.getData()
            .getCicCase().getDraftOrderCICList().get(0).getValue();
        assertThat(updatedDraftOrder.getDraftOrderContentCIC().getOrderTemplate()).isEqualTo(OrderTemplate.CIC6_GENERAL_DIRECTIONS);
        assertThat(updatedDraftOrder.getTemplateGeneratedDocument()).isNotNull();

        SubmittedCallbackResponse draftCreatedResponse = caseWorkerEditDraftOrder.draftUpdated(updatedCaseDetails, beforeDetails);
        //  Then
        assertThat(draftCreatedResponse).isNotNull();
    }

}

