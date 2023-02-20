package uk.gov.hmcts.sptribs.caseworker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderContentCIC;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.document.CaseDataDocumentService;
import uk.gov.hmcts.sptribs.document.content.PreviewDraftOrderTemplateContent;

import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

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

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    void shouldPopulateOrderDynamicList() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        ListValue<Order> order = new ListValue<>();
        Order orderCIC = Order.builder().build();
        order.setValue(orderCIC);
        CicCase cicCase = CicCase.builder().orderList(List.of(order)).build();
        caseData.setCicCase(cicCase);
        details.setData(caseData);
        //When

        DynamicList regionList = orderService.getOrderDynamicList(details);

        //Then
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
        //When
        CaseData result = orderService.generateOrderFile(caseData, details.getId());

        //Then
        assertThat(result).isNotNull();

    }


}
