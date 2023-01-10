package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.SendOrderAddDraftOrder;
import uk.gov.hmcts.sptribs.caseworker.event.page.SendOrderNotifyParties;
import uk.gov.hmcts.sptribs.caseworker.event.page.SendOrderOrderDueDates;
import uk.gov.hmcts.sptribs.caseworker.event.page.SendOrderOrderIssuingSelect;
import uk.gov.hmcts.sptribs.caseworker.event.page.SendOrderSendReminder;
import uk.gov.hmcts.sptribs.caseworker.event.page.SendOrderUploadOrder;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.caseworker.service.OrderService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_SEND_ORDER;
import static uk.gov.hmcts.sptribs.caseworker.util.EventUtil.getId;
import static uk.gov.hmcts.sptribs.caseworker.util.EventUtil.getRecipients;
import static uk.gov.hmcts.sptribs.caseworker.util.MessageUtil.getEmailMessage;
import static uk.gov.hmcts.sptribs.caseworker.util.MessageUtil.getPostMessage;
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
public class CaseworkerSendOrder implements CCDConfig<CaseData, State, UserRole> {
    private static final CcdPageConfiguration orderIssuingSelect = new SendOrderOrderIssuingSelect();
    private static final CcdPageConfiguration uploadOrder = new SendOrderUploadOrder();
    private static final CcdPageConfiguration draftOrder = new SendOrderAddDraftOrder();
    private static final CcdPageConfiguration orderDueDates = new SendOrderOrderDueDates();
    private static final CcdPageConfiguration notifyParties = new SendOrderNotifyParties();
    private static final CcdPageConfiguration sendReminder = new SendOrderSendReminder();

    @Autowired
    private OrderService orderService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        var pageBuilder = send(configBuilder);
        orderIssuingSelect.addTo(pageBuilder);
        draftOrder.addTo(pageBuilder);
        uploadOrder.addTo(pageBuilder);
        orderDueDates.addTo(pageBuilder);
        notifyParties.addTo(pageBuilder);
        sendReminder.addTo(pageBuilder);
    }

    public PageBuilder send(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(CASEWORKER_SEND_ORDER)
            .forStates(CaseManagement,
                AwaitingHearing,
                AwaitingOutcome,
                CaseClosed,
                CaseStayed)
            .name("Send order")
            .description("Send order")
            .showEventNotes()
            .showSummary()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::sent)
            .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
            .grantHistoryOnly(SOLICITOR));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        var caseData = details.getData();
        DynamicList draftList = orderService.getDraftOrderDynamicList(details);
        caseData.getCicCase().setDraftList(draftList);

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

        var order = Order.builder()
            .uploadedFile(cicCase.getOrderFile())
            .dueDateList(cicCase.getOrderDueDates())
            .parties(getRecipients(cicCase))
            .reminderDay(cicCase.getOrderReminderDays()).build();
        String selectedDraft = caseData.getCicCase().getDraftList().getValue().getLabel();
        String id = getId(selectedDraft);
        var draftList = caseData.getCicCase().getDraftOrderCICList();
        for (int i = 0; i < draftList.size(); i++) {
            if (null != id && Integer.parseInt(id) == i) {
                order.setDraftOrder(draftList.get(i).getValue());
            }
        }
        if (isEmpty(caseData.getCicCase().getOrderList())) {
            List<ListValue<Order>> listValues = new ArrayList<>();

            var listValue = ListValue
                .<Order>builder()
                .id("1")
                .value(order)
                .build();

            listValues.add(listValue);

            caseData.getCicCase().setOrderList(listValues);
        } else {
            AtomicInteger listValueIndex = new AtomicInteger(0);
            var listValue = ListValue
                .<Order>builder()
                .value(order)
                .build();

            caseData.getCicCase().getOrderList().add(0, listValue); // always add new note as first element so that it is displayed on top

            caseData.getCicCase().getOrderList().forEach(
                caseNoteListValue -> caseNoteListValue.setId(String.valueOf(listValueIndex.incrementAndGet())));

        }
        caseData.getCicCase().setOrderIssuingType(null);
        caseData.getCicCase().setOrderFile(null);
        caseData.getCicCase().setOrderReminderYesOrNo(null);
        caseData.getCicCase().setOrderReminderDays(null);
        caseData.getCicCase().setOrderDueDates(null);
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(details.getState())
            .build();
    }

    public SubmittedCallbackResponse sent(CaseDetails<CaseData, State> details,
                                          CaseDetails<CaseData, State> beforeDetails) {
        var cicCase = details.getData().getCicCase();
        final StringBuilder emailMessage = getEmailMessage(cicCase);

        StringBuilder postMessage = getPostMessage(cicCase);
        String message = "";
        if (null != postMessage && null != emailMessage) {
            message = format("# Order sent  %n"
                + " %s  %n  %s", emailMessage.substring(0, emailMessage.length() - 2), postMessage.substring(0, postMessage.length() - 2));
        } else if (null != emailMessage) {
            message = format("# Order sent %n ## "
                + " %s ", emailMessage.substring(0, emailMessage.length() - 2));

        } else if (null != postMessage) {
            message = format("# Order sent %n ## "
                + " %s ", postMessage.substring(0, postMessage.length() - 2));
        }

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(message)
            .build();
    }

}
