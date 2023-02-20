package uk.gov.hmcts.sptribs.common.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderContentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class EditDraftOrderTest {

    @InjectMocks
    private EditDraftOrder editDraftOrder;

    @Test
    void shouldSetDraftOrderContentFromSelectedOrder() {
        // Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final DynamicListElement element = DynamicListElement.builder()
            .code(UUID.randomUUID())
            .build();
        final DynamicList dynamicList = DynamicList.builder()
            .value(element)
            .build();
        final List<ListValue<DraftOrderCIC>> draftOrderCICList = new ArrayList<>();
        DraftOrderContentCIC contentCIC = DraftOrderContentCIC.builder()
            .mainContent("content")
            .orderSignature("signature")
            .orderTemplate(OrderTemplate.CIC6_GENERAL_DIRECTIONS)
            .build();
        DraftOrderCIC orderCIC = DraftOrderCIC.builder()
            .code(element.getCode().toString())
            .draftOrderContentCIC(contentCIC)
            .build();
        ListValue<DraftOrderCIC> listValue = ListValue.<DraftOrderCIC>builder()
            .value(orderCIC)
            .build();
        draftOrderCICList.add(listValue);

        final CicCase cicCase = CicCase.builder()
            .draftOrderDynamicList(dynamicList)
            .draftOrderCICList(draftOrderCICList)
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        // When
        editDraftOrder.midEvent(caseDetails, caseDetails);

        // Then
        assertThat(caseDetails.getData().getDraftOrderContentCIC().getMainContent()).isEqualTo("content");
        assertThat(caseDetails.getData().getDraftOrderContentCIC().getOrderSignature()).isEqualTo("signature");
        assertThat(caseDetails.getData().getDraftOrderContentCIC().getOrderTemplate()).isEqualTo(OrderTemplate.CIC6_GENERAL_DIRECTIONS);
    }
}
