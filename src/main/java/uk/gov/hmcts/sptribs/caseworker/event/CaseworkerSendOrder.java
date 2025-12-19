package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
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
import uk.gov.hmcts.sptribs.caseworker.util.DynamicListUtil;
import uk.gov.hmcts.sptribs.caseworker.util.MessageUtil;
import uk.gov.hmcts.sptribs.caseworker.util.SendOrderUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.notification.dispatcher.NewOrderIssuedNotification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_SEND_ORDER;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.COLON;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.DOUBLE_HYPHEN;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.DRAFT;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.SENT;
import static uk.gov.hmcts.sptribs.caseworker.util.EventUtil.getRecipients;
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
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.updateCategoryToDocument;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaseworkerSendOrder implements CCDConfig<CaseData, State, UserRole> {
    private static final CcdPageConfiguration orderIssuingSelect = new SendOrderOrderIssuingSelect();
    private static final CcdPageConfiguration uploadOrder = new SendOrderUploadOrder();
    private static final CcdPageConfiguration draftOrder = new SendOrderAddDraftOrder();
    private static final CcdPageConfiguration orderDueDates = new SendOrderOrderDueDates();
    private static final CcdPageConfiguration notifyParties = new SendOrderNotifyParties();
    private static final CcdPageConfiguration sendReminder = new SendOrderSendReminder();

    private static final int ORDER_TIMESTAMP_WITH_EXTENSION = 2; //dd-MM-yyyy HH:mm:ss.pdf

    private final NewOrderIssuedNotification newOrderIssuedNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = send(configBuilder);
        orderIssuingSelect.addTo(pageBuilder);
        draftOrder.addTo(pageBuilder);
        uploadOrder.addTo(pageBuilder);
        orderDueDates.addTo(pageBuilder);
        notifyParties.addTo(pageBuilder);
        sendReminder.addTo(pageBuilder);
    }

    public PageBuilder send(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
                configBuilder
                    .event(CASEWORKER_SEND_ORDER)
                    .forStates(CaseManagement, ReadyToList, AwaitingHearing, CaseClosed, CaseStayed)
                    .name("Orders: Send order")
                    .description("Orders: Send order")
                    .showSummary()
                    .aboutToStartCallback(this::aboutToStart)
                    .aboutToSubmitCallback(this::aboutToSubmit)
                    .submittedCallback(this::submitted)
                    .grant(CREATE_READ_UPDATE,
                        ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                        ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, ST_CIC_JUDGE, ST_CIC_WA_CONFIG_USER)
                    .publishToCamunda();

        return new PageBuilder(eventBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> caseDetails) {
        CaseData data = caseDetails.getData();
        CicCase cicCase = data.getCicCase();

        DynamicList availableOptions = DynamicListUtil.createDynamicListFromEnumSet(
            EnumSet.of(OrderIssuingType.ISSUE_AND_SEND_AN_EXISTING_DRAFT, OrderIssuingType.UPLOAD_A_NEW_ORDER_FROM_YOUR_COMPUTER),
            OrderIssuingType::getLabel,
            cicCase.getOrderIssuingType());

        cicCase.setOrderIssuingDynamicRadioList(availableOptions);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        final CaseData caseData = details.getData();
        if (caseData.getCicCase().getOrderFile() != null) {
            updateCategoryToDocument(caseData.getCicCase().getOrderFile(), DocumentType.TRIBUNAL_DIRECTION.getCategory());
        }

        DraftOrderCIC selectedDraftOrder = null;
        String selectedDynamicDraft = null;
        final Order order = Order.builder()
            .uploadedFile(caseData.getCicCase().getOrderFile())
            .dueDateList(caseData.getCicCase().getOrderDueDates())
            .parties(getRecipients(caseData.getCicCase()))
            .orderSentDate(LocalDate.now())
            .reminderDay(caseData.getCicCase().getOrderReminderDays()).build();

        if (caseData.getCicCase().getOrderIssuingType() != null && caseData.getCicCase().getDraftOrderDynamicList() != null
            && caseData.getCicCase().getOrderIssuingType().equals(OrderIssuingType.ISSUE_AND_SEND_AN_EXISTING_DRAFT)) {

            selectedDynamicDraft = caseData.getCicCase().getDraftOrderDynamicList().getValue().getLabel();
            for (ListValue<DraftOrderCIC> draftOrderCICValue : caseData.getCicCase().getDraftOrderCICList()) {
                String[] splitFilename = draftOrderCICValue.getValue().getTemplateGeneratedDocument().getFilename().split(DOUBLE_HYPHEN);
                String timestamp = splitFilename[ORDER_TIMESTAMP_WITH_EXTENSION].replace(".pdf", "").trim();
                if (selectedDynamicDraft.contains(draftOrderCICValue.getValue().getDraftOrderContentCIC().getOrderTemplate().getLabel())
                    && selectedDynamicDraft.contains(timestamp)) {
                    selectedDraftOrder = draftOrderCICValue.getValue();
                    String fileName = selectedDraftOrder.getTemplateGeneratedDocument().getFilename().replace(DRAFT + COLON, "");
                    selectedDraftOrder.getTemplateGeneratedDocument().setFilename(SENT + COLON + fileName);
                    order.setDraftOrder(selectedDraftOrder);
                }
            }
        }

        SendOrderUtil.updateCicCaseOrderList(caseData, order);

        caseData.getCicCase().setOrderIssuingType(null);
        caseData.getCicCase().setOrderFile(null);
        caseData.getCicCase().setOrderReminderYesOrNo(null);
        caseData.getCicCase().setOrderReminderDays(null);

        if (selectedDraftOrder != null) {
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
                if (!draftValue.getValue().equals(selectedDraftOrder)) {
                    ListValue<DraftOrderCIC> listValue = ListValue
                        .<DraftOrderCIC>builder()
                        .value(draftValue.getValue())
                        .build();

                    draftList.addFirst(listValue); // always add new order as first element so that it is displayed on top

                    draftList.forEach(
                        draftOrderListValue -> draftOrderListValue.setId(String.valueOf(listValueIndex.incrementAndGet())));
                }
            }
            caseData.getCicCase().setDraftOrderCICList(draftList);
        }

        caseData.getCicCase().setOrderDueDates(new ArrayList<>());
        caseData.getCicCase().setFirstOrderDueDate(caseData.getCicCase().calculateFirstDueDate());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(details.getState())
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                          CaseDetails<CaseData, State> beforeDetails) {
        try {
            sendOrderNotification(details.getData().getHyphenatedCaseRef(), details.getData());
        } catch (Exception notificationException) {
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(format("# Send order notification failed %n## Please resend the order"))
                .build();
        }

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Order sent %n## %s",
                MessageUtil.generateSimpleMessage(details.getData().getCicCase())))
            .build();
    }



    private void sendOrderNotification(String caseNumber, CaseData caseData) {
        if (!CollectionUtils.isEmpty(caseData.getCicCase().getNotifyPartySubject())) {
            newOrderIssuedNotification.sendToSubject(caseData, caseNumber);
        }

        if (!CollectionUtils.isEmpty(caseData.getCicCase().getNotifyPartyRepresentative())) {
            newOrderIssuedNotification.sendToRepresentative(caseData, caseNumber);
        }

        if (!CollectionUtils.isEmpty(caseData.getCicCase().getNotifyPartyRespondent())) {
            newOrderIssuedNotification.sendToRespondent(caseData, caseNumber);
        }

        if (!CollectionUtils.isEmpty(caseData.getCicCase().getNotifyPartyApplicant())) {
            newOrderIssuedNotification.sendToApplicant(caseData, caseNumber);
        }

    }
}
