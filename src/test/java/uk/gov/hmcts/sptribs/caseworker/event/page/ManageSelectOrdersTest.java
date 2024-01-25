package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DateModel;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class ManageSelectOrdersTest {

    @InjectMocks
    private ManageSelectOrders manageSelectOrders;

    @Mock
    private ListValue<DateModel> mockListValueDateModel;

    private final DynamicList dynamicListLabelled = createDynamicList("0");

    private final DynamicList dynamicListUnlabelled = createDynamicList("");

    private final DynamicList dynamicListNullLabel = createDynamicList(null);

    @Test
    void midEventCompletesSuccessfully() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        final Order order = Order.builder()
            .dueDateList(List.of(mockListValueDateModel))
            .draftOrder(new DraftOrderCIC())
            .build();
        final ListValue<Order> listValue = new ListValue<>();
        listValue.setValue(order);
        listValue.setId("0");
        final CicCase cicCase = CicCase.builder()
            .orderList(List.of(listValue))
            .orderDynamicList(dynamicListLabelled)
            .build();
        caseData.setCicCase(cicCase);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = manageSelectOrders.midEvent(caseDetails, caseDetails);
        assertNotNull(cicCase.getOrderDueDates());
        assertThat(cicCase.getOrderDueDates()).isEqualTo(List.of(mockListValueDateModel));
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void midEventReturnsErrorWhenNoSelectedOrder() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        final Order order = Order.builder()
            .dueDateList(List.of(mockListValueDateModel))
            .draftOrder(new DraftOrderCIC())
            .build();
        final ListValue<Order> listValue = new ListValue<>();
        listValue.setValue(order);
        listValue.setId("0");
        final CicCase cicCase = CicCase.builder()
            .orderList(List.of(listValue))
            .orderDynamicList(dynamicListUnlabelled)
            .build();
        caseData.setCicCase(cicCase);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = manageSelectOrders.midEvent(caseDetails, caseDetails);
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains("Please select an order to manage");
    }

    @Test
    void midEventReturnsCorrectErrorWhenOrderListIsNull() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        final CicCase cicCase = CicCase.builder()
            .orderList(null)
            .orderDynamicList(dynamicListUnlabelled)
            .build();
        caseData.setCicCase(cicCase);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = manageSelectOrders.midEvent(caseDetails, caseDetails);
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains("Please select an order to manage");
    }

    @Test
    void midEventReturnsCorrectErrorWhenDynamicOrderLabelIsNull() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        final Order order = Order.builder()
            .dueDateList(List.of(mockListValueDateModel))
            .draftOrder(new DraftOrderCIC())
            .build();
        final ListValue<Order> listValue = new ListValue<>();
        listValue.setValue(order);
        listValue.setId("0");
        final CicCase cicCase = CicCase.builder()
            .orderList(List.of(listValue))
            .orderDynamicList(dynamicListNullLabel)
            .build();
        caseData.setCicCase(cicCase);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = manageSelectOrders.midEvent(caseDetails, caseDetails);
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains("Please select an order to manage");
    }

    @Test
    void midEventReturnsCorrectErrorWhenOrderListIsEmpty() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        final CicCase cicCase = CicCase.builder()
            .orderList(Collections.emptyList())
            .orderDynamicList(dynamicListUnlabelled)
            .build();
        caseData.setCicCase(cicCase);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = manageSelectOrders.midEvent(caseDetails, caseDetails);
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains("Please select an order to manage");
    }

    @Test
    void midEventReturnsNoErrorsForNoMatchingId() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        final Order order = Order.builder()
            .dueDateList(List.of(mockListValueDateModel))
            .draftOrder(new DraftOrderCIC())
            .build();
        final ListValue<Order> listValue = new ListValue<>();
        listValue.setValue(order);
        listValue.setId("1");
        final CicCase cicCase = CicCase.builder()
            .orderList(List.of(listValue))
            .orderDynamicList(dynamicListLabelled)
            .build();
        caseData.setCicCase(cicCase);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = manageSelectOrders.midEvent(caseDetails, caseDetails);
        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getErrors()).isEmpty();
    }

    private DynamicList createDynamicList(String label) {
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label(label)
            .code(UUID.randomUUID())
            .build();
        return DynamicList
            .builder()
            .value(listItem)
            .listItems(List.of(listItem))
            .build();
    }
}
