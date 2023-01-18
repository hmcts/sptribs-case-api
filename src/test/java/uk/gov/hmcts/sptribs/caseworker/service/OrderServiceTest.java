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
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

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
    void shouldPopulateDraftOrderDynamicList() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        ListValue<DraftOrderCIC> draftOrderCIC = new ListValue<>();
        DraftOrderCIC orderCIC = DraftOrderCIC.builder().anOrderTemplate(OrderTemplate.CIC2_QUANTUM).build();
        draftOrderCIC.setValue(orderCIC);
        CicCase cicCase = CicCase.builder().draftOrderCICList(List.of(draftOrderCIC)).build();
        caseData.setCicCase(cicCase);
        details.setData(caseData);
        //When

        DynamicList regionList = orderService.getDraftOrderDynamicList(details);

        //Then
        assertThat(regionList).isNotNull();
    }

    @Test
    void shouldPopulateDraftOrderDynamicListNull() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        details.setData(caseData);
        //When

        DynamicList regionList = orderService.getDraftOrderDynamicList(details);

        //Then
        assertThat(regionList).isNull();
    }

    @Test
    void shouldCreateTemplateWithCurrentDateAndTime() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        final CicCase cicase = CicCase.builder().build();

        caseData.setCicCase(cicase);
        details.setData(caseData);

        Set<OrderTemplate> orderTemplates = new HashSet<>();
        orderTemplates.add(OrderTemplate.CIC2_QUANTUM);
        cicase.setAnOrderTemplates(orderTemplates);
        //When

        DynamicList orderTemplateList = orderService.getDraftOrderTemplatesDynamicList(details);

        //Then
        assertThat(orderTemplateList).isNotNull();
    }


}
