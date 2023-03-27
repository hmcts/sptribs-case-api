package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.SendOrderAddDraftOrder;
import uk.gov.hmcts.sptribs.caseworker.event.page.SendOrderNotifyParties;
import uk.gov.hmcts.sptribs.caseworker.event.page.SendOrderOrderDueDates;
import uk.gov.hmcts.sptribs.caseworker.event.page.SendOrderOrderIssuingSelect;
import uk.gov.hmcts.sptribs.caseworker.event.page.SendOrderSendReminder;
import uk.gov.hmcts.sptribs.caseworker.event.page.SendOrderUploadOrder;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.caseworker.model.OrderIssuingType;
import uk.gov.hmcts.sptribs.caseworker.util.MessageUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.notification.NewOrderIssuedNotification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_SEND_ORDER;
import static uk.gov.hmcts.sptribs.caseworker.util.EventUtil.getRecipients;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseStayed;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.DISTRICT_JUDGE_CIC;
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
    private NewOrderIssuedNotification newOrderIssuedNotification;

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
            .forStates(CaseManagement, AwaitingHearing, CaseClosed, CaseStayed)
            .name("Orders: Send order")
            .description("Orders: Send order")
            .showSummary()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::sent)
            .grant(CREATE_READ_UPDATE_DELETE,DISTRICT_JUDGE_CIC,COURT_ADMIN_CIC,SUPER_USER)
            .grantHistoryOnly(SOLICITOR));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        var caseData = details.getData();
        DraftOrderCIC selectedDraftOrder = null;
        String selectedDynamicDraft = null;
        var order = Order.builder()
            .uploadedFile(caseData.getCicCase().getOrderFile())
            .dueDateList(caseData.getCicCase().getOrderDueDates())
            .parties(getRecipients(caseData.getCicCase()))
            .orderSentDate(LocalDate.now())
            .reminderDay(caseData.getCicCase().getOrderReminderDays()).build();
        if (null != caseData.getCicCase().getOrderIssuingType() && null != caseData.getCicCase().getDraftOrderDynamicList()
            && caseData.getCicCase().getOrderIssuingType().equals(OrderIssuingType.ISSUE_AND_SEND_AN_EXISTING_DRAFT)) {
            selectedDynamicDraft = caseData.getCicCase().getDraftOrderDynamicList().getValue().getLabel();
            for (ListValue<DraftOrderCIC> draftOrderCICListValue : caseData.getCicCase().getDraftOrderCICList()) {
                if (selectedDynamicDraft
                    .contains(draftOrderCICListValue.getValue().getDraftOrderContentCIC().getOrderTemplate().getLabel())) {
                    selectedDraftOrder = draftOrderCICListValue.getValue();
                    order.setDraftOrder(selectedDraftOrder);
                }
            }
        }

        updateLastSelectedOrder(caseData.getCicCase(), order);

        if (CollectionUtils.isEmpty(caseData.getCicCase().getOrderList())) {
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
        if (null != selectedDraftOrder) {
            DynamicList dynamicList = caseData.getCicCase().getDraftOrderDynamicList();
            List<DynamicListElement> newElements = new ArrayList<>();
            for (DynamicListElement element : dynamicList.getListItems()) {
                if (!element.getLabel().equals(selectedDynamicDraft)) {
                    newElements.add(element);
                }
            }
            caseData.getCicCase().setDraftOrderDynamicList(DynamicList
                .builder()
                .listItems(newElements)
                .build());
            List<ListValue<DraftOrderCIC>> draftList = new ArrayList<>();
            AtomicInteger listValueIndex = new AtomicInteger(0);
            for (ListValue<DraftOrderCIC> draftValue : caseData.getCicCase().getDraftOrderCICList()) {
                if (!draftValue.getValue().getDraftOrderContentCIC().equals(selectedDraftOrder.getDraftOrderContentCIC())) {
                    var listValue = ListValue
                        .<DraftOrderCIC>builder()
                        .value(draftValue.getValue())
                        .build();

                    draftList.add(0, listValue); // always add new note as first element so that it is displayed on top

                    draftList.forEach(
                        caseNoteListValue -> caseNoteListValue.setId(String.valueOf(listValueIndex.incrementAndGet())));
                }
            }
            caseData.getCicCase().setDraftOrderCICList(draftList);
        }
        caseData.getCicCase().setOrderDueDates(new ArrayList<>());
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(details.getState())
            .build();
    }

    public SubmittedCallbackResponse sent(CaseDetails<CaseData, State> details,
                                          CaseDetails<CaseData, State> beforeDetails) {
        var cicCase = details.getData().getCicCase();
        sendOrderNotification(details.getData().getHyphenatedCaseRef(), details.getData());

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Order sent %n## %s",
                MessageUtil.generateSimpleMessage(cicCase)))
            .build();
    }

    private void updateLastSelectedOrder(CicCase cicCase, Order order) {
        if (null != order.getDraftOrder()) {
            cicCase.setLastSelectedOrder(order.getDraftOrder().getTemplateGeneratedDocument());
        } else if (null != order.getUploadedFile()
            && !CollectionUtils.isEmpty(order.getUploadedFile())) {
            cicCase.setLastSelectedOrder(order.getUploadedFile().get(0).getValue().getDocumentLink());
        }
    }

    private void sendOrderNotification(String caseNumber, CaseData caseData) {
        if (!CollectionUtils.isEmpty(caseData.getCicCase().getNotifyPartySubject())) {
            newOrderIssuedNotification.sendToSubject(caseData, caseNumber);
        }

        if (!CollectionUtils.isEmpty(caseData.getCicCase().getNotifyPartyRepresentative())) {
            newOrderIssuedNotification.sendToRespondent(caseData, caseNumber);
        }

        if (!CollectionUtils.isEmpty(caseData.getCicCase().getNotifyPartyRespondent())) {
            newOrderIssuedNotification.sendToRepresentative(caseData, caseNumber);
        }

        //Once Notification is sent, nullify the last selected order
        caseData.getCicCase().setLastSelectedOrder(null);
    }

}
