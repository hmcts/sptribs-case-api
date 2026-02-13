package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.AmendOrderDueDates;
import uk.gov.hmcts.sptribs.caseworker.event.page.ManageSelectOrders;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.caseworker.service.OrderService;
import uk.gov.hmcts.sptribs.ciccase.CicCaseFieldsUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_AMEND_DUE_DATE;
import static uk.gov.hmcts.sptribs.caseworker.util.EventUtil.getId;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseStayed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.ReadyToList;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;


@Component
@Slf4j
public class CaseWorkerManageOrderDueDate implements CCDConfig<CaseData, State, UserRole> {

    private static final CcdPageConfiguration manageSelectOrderTemplates = new ManageSelectOrders();
    private static final CcdPageConfiguration amendOrderDueDates = new AmendOrderDueDates();

    @Autowired
    private OrderService orderService;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
            configBuilder
                .event(CASEWORKER_AMEND_DUE_DATE)
                .forStates(CaseManagement, ReadyToList, AwaitingHearing, CaseClosed, CaseStayed)
                .name("Orders: Manage due date")
                .description("Orders: Manage due date")
                .showSummary()
                .aboutToStartCallback(this::aboutToStart)
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::submitted)
                .grant(CREATE_READ_UPDATE, SUPER_USER,
                    ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                    ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, ST_CIC_WA_CONFIG_USER)
                .grantHistoryOnly(ST_CIC_JUDGE)
                .publishToCamunda();

        PageBuilder pageBuilder = new PageBuilder(eventBuilder);
        manageSelectOrderTemplates.addTo(pageBuilder);
        amendOrderDueDates.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        final CaseData caseData = details.getData();
        DynamicList orderDynamicList = orderService.getOrderDynamicList(details);
        caseData.getCicCase().setOrderDynamicList(orderDynamicList);
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = details.getData();
        final CicCase cicCase = caseData.getCicCase();
        final String selectedOrder = caseData.getCicCase().getOrderDynamicList().getValue().getLabel();
        final String id = getId(selectedOrder);
        final List<ListValue<Order>> orderList = caseData.getCicCase().getOrderList();
        for (ListValue<Order> orderListValue : orderList) {
            if (id != null && id.equals(orderListValue.getId())) {
                Order order = orderListValue.getValue();
                order.setDueDateList(cicCase.getOrderDueDates());
                orderListValue.setValue(order);
                break;
            }
        }
        caseData.getCicCase().setOrderList(orderList);
        cicCase.setOrderDueDates(new ArrayList<>());
        cicCase.setFirstOrderDueDate(CicCaseFieldsUtil.calculateFirstDueDate(caseData.getCicCase().getOrderList()));
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .state(details.getState())
            .data(caseData)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                                       CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Due dates amended.")
            .build();
    }
}
