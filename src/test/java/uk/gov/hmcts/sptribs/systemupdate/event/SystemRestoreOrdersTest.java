package uk.gov.hmcts.sptribs.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.repositories.exception.CaseEventRepositoryException;
import uk.gov.hmcts.sptribs.common.service.OrdersListRestoreService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemRestoreOrders.SYSTEM_RESTORE_ORDERS;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class SystemRestoreOrdersTest {

    @InjectMocks
    private SystemRestoreOrders systemRestoreOrders;

    @Mock
    private OrdersListRestoreService ordersListRestoreService;

    private static final LocalDate START_FROM_DATE = LocalDate.of(2026, 2, 24);

    private static final LocalDate END_TO_DATE = LocalDate.of(2026, 3, 5);

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        systemRestoreOrders.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_RESTORE_ORDERS);
    }

    @Test
    void shouldCallRestoreServiceWithCorrectArguments() {
        CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder()
                .orderList(List.of())
                .build())
            .build();

        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(12345L);
        caseDetails.setData(caseData);

        systemRestoreOrders.aboutToSubmit(caseDetails, null);

        verify(ordersListRestoreService).restoreOrdersList(
            eq(12345L),
            eq(caseData),
            eq(START_FROM_DATE),
            eq(END_TO_DATE)
        );
    }

    @Test
    void shouldReturnMutatedCaseDataAfterRestore() {
        ListValue<Order> restoredOrder = ListValue.<Order>builder()
            .id("order-1")
            .value(Order.builder().build())
            .build();

        CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder()
                .orderList(new ArrayList<>())
                .build())
            .build();

        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(12345L);
        caseDetails.setData(caseData);

        // simulate service mutating caseData directly
        doAnswer(invocation -> {
            CaseData data = invocation.getArgument(1);
            data.getCicCase().setOrderList(List.of(restoredOrder));
            return null;
        }).when(ordersListRestoreService).restoreOrdersList(any(), any(), any(), any());

        AboutToStartOrSubmitResponse<CaseData, State> response =
            systemRestoreOrders.aboutToSubmit(caseDetails, null);

        assertThat(response.getData()).isEqualTo(caseData);
        assertThat(response.getData().getCicCase().getOrderList())
            .hasSize(1)
            .extracting(ListValue::getId)
            .containsExactly("order-1");
    }

    @Test
    void shouldReturnUnchangedCaseDataWhenNoOrdersRestored() {
        ListValue<Order> existingOrder = ListValue.<Order>builder()
            .id("order-existing")
            .value(Order.builder().build())
            .build();

        CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder()
                .orderList(new ArrayList<>(List.of(existingOrder)))
                .build())
            .build();

        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(12345L);
        caseDetails.setData(caseData);

        // service does nothing - no orders to restore
        doNothing().when(ordersListRestoreService)
            .restoreOrdersList(any(), any(), any(), any());

        AboutToStartOrSubmitResponse<CaseData, State> response =
            systemRestoreOrders.aboutToSubmit(caseDetails, null);

        assertThat(response.getData().getCicCase().getOrderList())
            .hasSize(1)
            .extracting(ListValue::getId)
            .containsExactly("order-existing");
    }

    @Test
    void shouldPropagateExceptionWhenServiceThrows() {
        CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder().build())
            .build();

        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(12345L);
        caseDetails.setData(caseData);

        doThrow(new CaseEventRepositoryException("DB error", new RuntimeException()))
            .when(ordersListRestoreService)
            .restoreOrdersList(any(), any(), any(), any());

        assertThatThrownBy(() -> systemRestoreOrders.aboutToSubmit(caseDetails, null))
            .isInstanceOf(CaseEventRepositoryException.class)
            .hasMessageContaining("DB error");
    }
}
