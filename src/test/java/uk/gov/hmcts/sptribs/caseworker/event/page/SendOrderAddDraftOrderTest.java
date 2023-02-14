package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import java.util.List;
import java.util.UUID;


@ExtendWith(MockitoExtension.class)
class SendOrderAddDraftOrderTest {

    @InjectMocks
    private SendOrderAddDraftOrder sendOrderAddDraftOrder;

    @Test
    void shouldReturnErrorsIfNoNotificationPartySelected() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        ListValue<DraftOrderCIC> order = new ListValue<>();
        final CicCase cicCase = CicCase.builder()
            .draftOrderCICList(List.of(order))
            .draftOrderDynamicList(getDraftList())
            .build();
        caseData.setCicCase(cicCase);
        caseDetails.setData(caseData);


    }

    private DynamicList getDraftList() {
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
