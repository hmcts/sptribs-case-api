package uk.gov.hmcts.sptribs.caseworker.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderContentCIC;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.document.CaseDataDocumentService;
import uk.gov.hmcts.sptribs.document.content.PreviewDraftOrderTemplateContent;
import uk.gov.hmcts.sptribs.document.model.CICDocument;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private PreviewDraftOrderTemplateContent previewDraftOrderTemplateContent;

    @Mock
    private HttpServletRequest request;

    @Test
    void shouldPopulateOrderDynamicList() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        final ListValue<Order> order = new ListValue<>();
        final DraftOrderContentCIC contentCIC = DraftOrderContentCIC.builder().orderTemplate(OrderTemplate.CIC7_ME_DMI_REPORTS).build();
        final DraftOrderCIC draftOrderCIC = DraftOrderCIC.builder().draftOrderContentCIC(contentCIC).build();
        final Order orderCIC = Order.builder().draftOrder(draftOrderCIC).build();
        order.setValue(orderCIC);
        final CicCase cicCase = CicCase.builder().orderList(List.of(order)).build();
        caseData.setCicCase(cicCase);
        details.setData(caseData);

        final DynamicList regionList = orderService.getOrderDynamicList(details);

        assertThat(regionList).isNotNull();
    }

    @Test
    void shouldPopulateOrderDynamicListWithUploadFile() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        final ListValue<Order> order = new ListValue<>();

        final UUID uuid = UUID.randomUUID();
        final CICDocument document = CICDocument.builder()
            .documentLink(Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build())
            .documentEmailContent("content")
            .build();
        final ListValue<CICDocument> documentListValue = new ListValue<>();
        documentListValue.setValue(document);
        final Order orderCIC = Order.builder().uploadedFile(List.of(documentListValue)).build();
        order.setValue(orderCIC);
        final CicCase cicCase = CicCase.builder().orderList(List.of(order)).build();
        caseData.setCicCase(cicCase);
        details.setData(caseData);

        final DynamicList regionList = orderService.getOrderDynamicList(details);

        assertThat(regionList).isNotNull();
    }

    @Test
    void shouldGenerateOrderFile() {
        //Given
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        DraftOrderContentCIC orderContentCIC = DraftOrderContentCIC.builder().orderTemplate(OrderTemplate.CIC6_GENERAL_DIRECTIONS).build();
        caseData.setDraftOrderContentCIC(orderContentCIC);
        details.setData(caseData);

        CaseData result = orderService.generateOrderFile(caseData, details.getId(), "");

        assertThat(result).isNotNull();
    }


}
