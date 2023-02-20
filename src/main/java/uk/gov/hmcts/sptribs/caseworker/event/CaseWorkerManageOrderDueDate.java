package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.AmendOrderDueDates;
import uk.gov.hmcts.sptribs.caseworker.event.page.ManageSelectOrders;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_AMEND_DUE_DATE;
import static uk.gov.hmcts.sptribs.caseworker.util.EventUtil.getId;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseStayed;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;


@Component
@Slf4j
public class CaseWorkerManageOrderDueDate implements CCDConfig<CaseData, State, UserRole> {

    private static final CcdPageConfiguration manageSelectOrderTemplates = new ManageSelectOrders();
    private static final CcdPageConfiguration amendOrderDueDates = new AmendOrderDueDates();

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(
            configBuilder
                .event(CASEWORKER_AMEND_DUE_DATE)
                .forStates(CaseManagement, AwaitingHearing, AwaitingOutcome, CaseClosed, CaseStayed)
                .name("Orders: Manage due date")
                .description("Orders: Manage due date")
                .showSummary()
                .aboutToStartCallback(this::aboutToStart)
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::orderDatesManaged)
                .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
                .grantHistoryOnly(SOLICITOR));

        manageSelectOrderTemplates.addTo(pageBuilder);
        amendOrderDueDates.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        var caseData = details.getData();
        DynamicList draftOrderDynamicList = caseData.getCicCase().getDraftOrderDynamicList();
        caseData.getCicCase().setOrderDynamicList(draftOrderDynamicList);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        var caseData = details.getData();
        var cicCase = caseData.getCicCase();
        String selectedDraft = caseData.getCicCase().getOrderDynamicList().getValue().getLabel();
        String id = getId(selectedDraft);
        var orderList = caseData.getCicCase().getOrderList();
        Order order = new Order();
        for (int i = 0; i < orderList.size(); i++) {
            if (null != id && id.equals(orderList.get(i).getId())) {
                order = orderList.get(i).getValue();
                order.setDueDateList(cicCase.getOrderDueDates());
                orderList.get(i).setValue(order);
                break;
            }
        }
        caseData.getCicCase().setOrderList(orderList);
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .state(details.getState())
            .data(caseData)
            .build();

    }

    public SubmittedCallbackResponse orderDatesManaged(CaseDetails<CaseData, State> details,
                                                       CaseDetails<CaseData, State> beforeDetails) {

        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Due dates amended.")
            .build();
    }
}
