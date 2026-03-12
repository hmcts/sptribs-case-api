package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.DateModel;
import uk.gov.hmcts.sptribs.caseworker.model.DueDateOptions;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.caseworker.service.OrderService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.access.Permissions;
import uk.gov.hmcts.sptribs.taskmanagement.TaskManagementService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.followUpNoncomplianceOfDirections;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_AMEND_DUE_DATE;


@ExtendWith(MockitoExtension.class)
class CaseWorkerManageOrderDueDatesTest {

    @Mock
    private OrderService orderService;

    @Mock
    private TaskManagementService taskManagementService;

    private final Clock fixedClock = Clock.fixed(
        LocalDate.of(2026, 7, 15)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant(),
        ZoneId.systemDefault()
    );

    private CaseWorkerManageOrderDueDate caseWorkerManageOrderDueDate;

    @BeforeEach
    void setUp() {
        caseWorkerManageOrderDueDate =
            new CaseWorkerManageOrderDueDate(orderService, fixedClock);
    }


    @Test
    void shouldAddPublishToCamundaWhenWAIsEnabled() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseWorkerManageOrderDueDate.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_AMEND_DUE_DATE);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::getGrants)
                .extracting(map -> map.containsKey(ST_CIC_WA_CONFIG_USER))
                .contains(true);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::getGrants)
                .extracting(map -> map.get(ST_CIC_WA_CONFIG_USER))
                .contains(Permissions.CREATE_READ_UPDATE);
    }

    @Test
    void shouldSuccessfullySaveDraftOrder() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final Order order = new Order();
        ListValue<Order> listValue = new ListValue<>();
        listValue.setValue(order);
        listValue.setId("0");
        final CicCase cicCase = CicCase.builder()
            .orderList(List.of(listValue))
            .orderDynamicList(getOrderList())
            .build();
        caseData.setCicCase(cicCase);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseWorkerManageOrderDueDate.aboutToSubmit(updatedCaseDetails, beforeDetails);

        SubmittedCallbackResponse draftCreatedResponse =
            caseWorkerManageOrderDueDate.submitted(updatedCaseDetails, beforeDetails);
        //Then
        assertThat(draftCreatedResponse).isNotNull();
        assertThat(response).isNotNull();
        verify(taskManagementService).enqueueCompletionTasks(List.of(followUpNoncomplianceOfDirections), TEST_CASE_ID);

    }

    @Test
    void whenAboutToSubmit_thenShouldSuccessfullyUpdateDueDateWithNewRadioListOption() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        List<ListValue<DateModel>> dueDatesListValues = new ArrayList<>();

        buildDueDateList(dueDatesListValues);

        final Order order = new Order();
        ListValue<Order> listValue = new ListValue<>();
        listValue.setValue(order);
        listValue.setId("0");
        final CicCase cicCase = CicCase.builder()
            .orderList(List.of(listValue))
            .orderDynamicList(getOrderList())
            .build();
        caseData.setCicCase(cicCase);
        caseData.setOrderDueDates(dueDatesListValues);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseWorkerManageOrderDueDate.aboutToSubmit(updatedCaseDetails, beforeDetails);

        // Then
        assertThat(response).isNotNull();

        List<ListValue<DateModel>> actualOrderDueDates = response.getData().getCicCase()
            .getOrderList().getFirst().getValue().getDueDateList();
        assertEquals(2, actualOrderDueDates.size(), "assert list correct size");

        DateModel first = actualOrderDueDates.get(0).getValue();
        DateModel second = actualOrderDueDates.get(1).getValue();

        assertEquals(LocalDate.now(fixedClock).plusDays(21), first.getDueDate(), "assert due date has been updated");
        assertNull(first.getUpdatedDueDate(), "assert updated is set to null");

        assertEquals(LocalDate.of(2026, 8, 5), second.getDueDate(), "assert due date has been updated");
        assertNull(second.getUpdatedDueDate(), "assert updated is set to null");

        assertEquals(LocalDate.now(fixedClock).plusDays(21),
            response.getData().getCicCase().getFirstOrderDueDate(),
            "checking the first order due date set is actualy the earliest");

    }

    private void buildDueDateList(List<ListValue<DateModel>> dueDatesListValues) {

        dueDatesListValues.addAll(List.of(
            listValue("1", DateModel.builder()
                .dueDate(LocalDate.of(2026, 5, 5))
                .dueDateOptions(DueDateOptions.DAY_COUNT_21)
                .build()),

            listValue("2", DateModel.builder()
                .dueDate(LocalDate.of(2026, 5, 5))
                .dueDateOptions(DueDateOptions.OTHER)
                .updatedDueDate(LocalDate.of(2026, 8, 5))
                .build())
        ));
    }

    @Test
    void shouldRunAboutToStart() {
        //Given
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder().build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        updatedCaseDetails.setData(caseData);
        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = caseWorkerManageOrderDueDate.aboutToStart(updatedCaseDetails);

        //Then
        assertThat(response).isNotNull();
    }

    private DynamicList getOrderList() {
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

    private ListValue<DateModel> listValue(String id, DateModel model) {
        return ListValue.<DateModel>builder()
            .id(id)
            .value(model)
            .build();
    }


}
