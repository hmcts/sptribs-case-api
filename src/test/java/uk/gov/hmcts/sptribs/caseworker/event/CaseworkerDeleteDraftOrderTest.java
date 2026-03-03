package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
import uk.gov.hmcts.sptribs.caseworker.event.page.ShowDraftOrders;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderContentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_DELETE_DRAFT_ORDER;

@ExtendWith(MockitoExtension.class)
class CaseworkerDeleteDraftOrderTest {
    @InjectMocks
    private CaseworkerDeleteDraftOrder caseworkerDeleteDraftOrder;

    @InjectMocks
    private ShowDraftOrders showDraftOrders;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseworkerDeleteDraftOrder.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_DELETE_DRAFT_ORDER);
    }

    @Test
    void shouldRemoveDraftOrderSuccessfully() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        final Document document = Document.builder()
            .url("test/documents/a57d1138-1f8d-4aeb-b5ad-3681aba68747")
            .filename("Order--[test]--24-02-2026 15:47:25.pdf")
            .binaryUrl("test")
            .categoryId("TD")
            .build();

        final DraftOrderContentCIC content = DraftOrderContentCIC.builder()
            .orderTemplate(OrderTemplate.CIC3_RULE_27)
            .mainContent("test")
            .orderSignature("test")
            .build();

        final DraftOrderCIC draftOrder = DraftOrderCIC.builder()
            .templateGeneratedDocument(document)
            .draftOrderContentCIC(content)
            .build();

        ListValue<DraftOrderCIC> lv1 = ListValue.<DraftOrderCIC>builder()
            .id("1")
            .value(draftOrder)
            .build();

        DynamicListElement dynamicListElement = new DynamicListElement(UUID.randomUUID(), "test");

        CicCase cicCase = CicCase.builder()
            .draftOrderCICList(new ArrayList<>(List.of(lv1)))
            .draftOrderDynamicList(DynamicList.builder().value(dynamicListElement)
                .listItems(new ArrayList<>())
                .build())
            .build();

        caseData.setCicCase(cicCase);

        beforeDetails.setData(caseData);

        final CaseData newData = caseData();

        CicCase cicCaseNew = CicCase.builder()
            .draftOrderCICList(new ArrayList<>())
            .draftOrderDynamicList(DynamicList.builder()
                .listItems(new ArrayList<>())
                .build())
            .build();

        newData.setCicCase(cicCaseNew);

        updatedCaseDetails.setData(newData);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> midResponse =
            showDraftOrders.midEvent(updatedCaseDetails, beforeDetails);
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerDeleteDraftOrder.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse deleteDraftOrderResponse = caseworkerDeleteDraftOrder.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(midResponse).isNotNull();
        assertThat(response).isNotNull();
        assertThat(deleteDraftOrderResponse).isNotNull();
        assertThat(response.getData().getCicCase().getDraftOrderDynamicList().getListItems()).isNull();
    }
}
