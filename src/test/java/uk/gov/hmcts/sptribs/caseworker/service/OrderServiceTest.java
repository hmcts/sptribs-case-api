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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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
        assertThat(regionList.getListItems()).isNotNull();
        assertThat(regionList.getListItems().getFirst()).isNotNull();
        assertThat(regionList.getListItems().getFirst().getLabel()).contains(OrderTemplate.CIC7_ME_DMI_REPORTS.name());
    }

    @Test
    void getOrderDynamicListReturnNullForEmptyOrderList() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        final CicCase cicCase = CicCase.builder().orderList(Collections.emptyList()).build();
        caseData.setCicCase(cicCase);
        details.setData(caseData);

        final DynamicList regionList = orderService.getOrderDynamicList(details);

        assertThat(regionList).isNull();
    }

    @Test
    void getOrderDynamicListReturnNullWhenOrderListIsNull() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        final CicCase cicCase = CicCase.builder().build();
        caseData.setCicCase(cicCase);
        details.setData(caseData);

        final DynamicList regionList = orderService.getOrderDynamicList(details);

        assertThat(regionList).isNull();
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
        order.setId("12345");
        final CicCase cicCase = CicCase.builder().orderList(List.of(order)).build();
        caseData.setCicCase(cicCase);
        details.setData(caseData);

        final DynamicList regionList = orderService.getOrderDynamicList(details);

        assertThat(regionList).isNotNull();
        assertThat(regionList.getListItems()).isNotNull();
        assertThat(regionList.getListItems().getFirst()).isNotNull();
        assertThat(regionList.getListItems().getFirst().getLabel()).contains("12345");

    }

    @Test
    void shouldGenerateOrderFile() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(12345L);
        final CaseData caseData = CaseData.builder().build();
        final CicCase cicCase = CicCase.builder().fullName("Jane Smith").build();
        caseData.setCicCase(cicCase);
        final DraftOrderContentCIC orderContentCIC =
            DraftOrderContentCIC.builder().orderTemplate(OrderTemplate.CIC6_GENERAL_DIRECTIONS).build();
        caseData.setDraftOrderContentCIC(orderContentCIC);
        details.setData(caseData);

        final Document expectedDocument = Document.builder().filename("testDraft.pdf").build();
        when(caseDataDocumentService.renderDocument(anyMap(), anyLong(), anyString(), any(), anyString(), any()))
            .thenReturn(expectedDocument);
        final CaseData result = orderService.generateOrderFile(caseData, details.getId(), "24-01-2024");

        assertThat(result).isNotNull();
        assertThat(result.getCicCase()).isNotNull();
        assertThat(result.getCicCase().getOrderTemplateIssued()).isNotNull();
        assertThat(result.getCicCase().getOrderTemplateIssued()).isEqualTo(expectedDocument);
    }


}
