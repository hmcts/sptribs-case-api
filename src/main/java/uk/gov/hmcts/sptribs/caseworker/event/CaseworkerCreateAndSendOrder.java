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
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.ApplyAnonymity;
import uk.gov.hmcts.sptribs.caseworker.event.page.CreateAndSendOrderIssueSelect;
import uk.gov.hmcts.sptribs.caseworker.event.page.DraftOrderFooter;
import uk.gov.hmcts.sptribs.caseworker.event.page.SendOrderNotifyParties;
import uk.gov.hmcts.sptribs.caseworker.event.page.SendOrderOrderDueDates;
import uk.gov.hmcts.sptribs.caseworker.event.page.SendOrderUploadOrder;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderContentCIC;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.caseworker.util.CaseFlagsUtil;
import uk.gov.hmcts.sptribs.caseworker.util.MessageUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.event.page.CreateDraftOrder;
import uk.gov.hmcts.sptribs.common.event.page.DraftOrderMainContentPage;
import uk.gov.hmcts.sptribs.common.event.page.PreviewDraftOrder;
import uk.gov.hmcts.sptribs.common.service.CcdSupplementaryDataService;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.notification.dispatcher.NewOrderIssuedNotification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.caseworker.model.CreateAndSendIssuingType.CREATE_AND_SEND_NEW_ORDER;
import static uk.gov.hmcts.sptribs.caseworker.model.CreateAndSendIssuingType.UPLOAD_A_NEW_ORDER_FROM_YOUR_COMPUTER;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CREATE_AND_SEND_ORDER;
import static uk.gov.hmcts.sptribs.caseworker.util.EventUtil.getRecipients;
import static uk.gov.hmcts.sptribs.caseworker.util.SendOrderUtil.updateCicCaseOrderList;
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
import static uk.gov.hmcts.sptribs.document.DocumentUtil.updateCategoryToDocument;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaseworkerCreateAndSendOrder implements CCDConfig<CaseData, State, UserRole> {

    private static final CcdPageConfiguration createSendIssuingSelect = new CreateAndSendOrderIssueSelect();

    private static final CcdPageConfiguration createDraftOrder = new CreateDraftOrder();
    private static final CcdPageConfiguration draftOrderMainContentPage = new DraftOrderMainContentPage();
    private static final CcdPageConfiguration uploadOrder = new SendOrderUploadOrder();
    private static final CcdPageConfiguration orderDueDates = new SendOrderOrderDueDates();
    private static final CcdPageConfiguration previewOrder = new PreviewDraftOrder();
    private static final CcdPageConfiguration notifyParties = new SendOrderNotifyParties();

    private final ApplyAnonymity applyAnonymitySelect;
    private final DraftOrderFooter draftOrderFooter;
    private final NewOrderIssuedNotification newOrderIssuedNotification;

    private final CcdSupplementaryDataService ccdSupplementaryDataService;

    private boolean caseFlagAdded = false;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
            configBuilder
                .event(CASEWORKER_CREATE_AND_SEND_ORDER)
                .forStates(CaseManagement, ReadyToList, AwaitingHearing, CaseStayed, CaseClosed)
                .name("Orders: Create and send order")
                .description("Orders: Create and send order")
                .showSummary()
                .aboutToStartCallback(this::aboutToStart)
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::submitted)
                .grant(CREATE_READ_UPDATE, SUPER_USER,
                    ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                    ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, ST_CIC_JUDGE, ST_CIC_WA_CONFIG_USER);

        PageBuilder pageBuilder = new PageBuilder(eventBuilder);
        applyAnonymitySelect.addTo(pageBuilder);
        createSendIssuingSelect.addTo(pageBuilder);
        createDraftOrder.addTo(pageBuilder);
        draftOrderMainContentPage.addTo(pageBuilder);
        draftOrderFooter.addTo(pageBuilder);
        uploadOrder.addTo(pageBuilder);

        orderDueDates.addTo(pageBuilder);
        notifyParties.addTo(pageBuilder);
        previewOrder.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        final CaseData caseData = details.getData();
        caseData.setCurrentEvent(CASEWORKER_CREATE_AND_SEND_ORDER);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = details.getData();

        Order.OrderBuilder orderBuilder = Order.builder();
        if (caseData.getCicCase().getCreateAndSendIssuingTypes().equals(CREATE_AND_SEND_NEW_ORDER)) {
            DraftOrderCIC draftOrderCIC = DraftOrderCIC.builder()
                .draftOrderContentCIC(caseData.getDraftOrderContentCIC())
                .templateGeneratedDocument(caseData.getCicCase().getOrderTemplateIssued())
                .build();

            orderBuilder.draftOrder(draftOrderCIC);

            caseData.setDraftOrderContentCIC(new DraftOrderContentCIC());
            caseData.getCicCase().setOrderTemplateIssued(null);
        }

        if (caseData.getCicCase().getCreateAndSendIssuingTypes().equals(UPLOAD_A_NEW_ORDER_FROM_YOUR_COMPUTER)) {
            if (caseData.getCicCase().getOrderFile() != null) {
                updateCategoryToDocument(caseData.getCicCase().getOrderFile(), DocumentType.TRIBUNAL_DIRECTION.getCategory());
            }
            orderBuilder.uploadedFile(caseData.getCicCase().getOrderFile());
        }

        final Order order = orderBuilder
            .dueDateList(caseData.getCicCase().getOrderDueDates())
            .parties(getRecipients(caseData.getCicCase()))
            .orderSentDate(LocalDate.now())
            .build();

        updateCicCaseOrderList(caseData, order);

        if (YesOrNo.YES.equals(caseData.getCicCase().getAnonymiseYesOrNo()) && caseData.getCicCase().getAnonymisedAppellantName() != null) {
            applyAnonymityCaseFlag(caseData);
        }

        caseData.getCicCase().setCreateAndSendIssuingTypes(null);
        caseData.getCicCase().setOrderFile(null);
        caseData.getCicCase().setOrderTemplateIssued(null);
        caseData.getCicCase().setOrderReminderYesOrNo(null);

        caseData.getCicCase().setOrderDueDates(new ArrayList<>());
        caseData.getCicCase().setFirstOrderDueDate(caseData.getCicCase().calculateFirstDueDate());
        caseData.setCurrentEvent("");

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .state(details.getState())
                .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                                      CaseDetails<CaseData, State> beforeDetails) {
        if (this.caseFlagAdded) {
            ccdSupplementaryDataService.submitSupplementaryDataToCcd(details.getId().toString());
        }

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

    private void applyAnonymityCaseFlag(CaseData data) {
        CaseFlagsUtil.addFlag(data, "CF0012", "Applied anonymity");
    }
}
