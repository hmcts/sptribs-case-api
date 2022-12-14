package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ManageSelectOrdersTest {

    @InjectMocks
    private ManageSelectOrders manageSelectOrders;

    @Test
    void shouldReturnErrorsIfNoNotificationPartySelected() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        final Order order = Order.builder()
            .dueDateList(null)
            .draftOrder(new DraftOrderCIC())
            .build();
        ListValue<Order> listValue = new ListValue<>();
        listValue.setValue(order);
        listValue.setId("0");
        final CicCase cicCase = CicCase.builder()
            .orderList(List.of(listValue))
            .orderDynamicList(geOrderList())
            .build();
        caseData.setCicCase(cicCase);
        caseDetails.setData(caseData);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = manageSelectOrders.midEvent(caseDetails, caseDetails);

        //Then
        assertThat(response.getErrors()).hasSize(0);
    }

    private DynamicList geOrderList() {
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label("0")
            .code(UUID.randomUUID())
            .build();
        return DynamicList
            .builder()
            .value(listItem)
            .listItems(List.of(listItem))
            .build();
    }
}
