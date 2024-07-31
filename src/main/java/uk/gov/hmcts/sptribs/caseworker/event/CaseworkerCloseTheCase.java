package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.CloseCaseConcessionDetails;
import uk.gov.hmcts.sptribs.caseworker.event.page.CloseCaseConsentOrder;
import uk.gov.hmcts.sptribs.caseworker.event.page.CloseCaseReasonSelect;
import uk.gov.hmcts.sptribs.caseworker.event.page.CloseCaseRejectionDetails;
import uk.gov.hmcts.sptribs.caseworker.event.page.CloseCaseRule27;
import uk.gov.hmcts.sptribs.caseworker.event.page.CloseCaseSelectRecipients;
import uk.gov.hmcts.sptribs.caseworker.event.page.CloseCaseStrikeOutDetails;
import uk.gov.hmcts.sptribs.caseworker.event.page.CloseCaseWarning;
import uk.gov.hmcts.sptribs.caseworker.event.page.CloseCaseWithdrawalDetails;
import uk.gov.hmcts.sptribs.caseworker.model.CloseCase;
import uk.gov.hmcts.sptribs.caseworker.util.MessageUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.notification.CaseWithdrawnNotification;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocumentUpload;
import uk.gov.hmcts.sptribs.judicialrefdata.JudicialService;

import java.util.List;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CLOSE_THE_CASE;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.ReadyToList;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.convertToCaseworkerCICDocument;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.updateUploadedDocumentCategory;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.validateUploadedDocuments;

@Component
@Slf4j
public class CaseworkerCloseTheCase implements CCDConfig<CaseData, State, UserRole> {

    private static final CcdPageConfiguration closeCaseWarning = new CloseCaseWarning();
    private static final CcdPageConfiguration closeCaseReasonSelect = new CloseCaseReasonSelect();
    private static final CcdPageConfiguration closeCaseWithdrawalDetails = new CloseCaseWithdrawalDetails();
    private static final CcdPageConfiguration closeCaseRejectionDetails = new CloseCaseRejectionDetails();
    private static final CcdPageConfiguration closeCaseConcessionDetails = new CloseCaseConcessionDetails();
    private static final CcdPageConfiguration closeCaseStrikeOutDetails = new CloseCaseStrikeOutDetails();
    private static final CcdPageConfiguration closeCaseConsentOrder = new CloseCaseConsentOrder();
    private static final CcdPageConfiguration closeCaseRule27 = new CloseCaseRule27();
    private static final CcdPageConfiguration closeCaseSelectRecipients = new CloseCaseSelectRecipients();

    @Autowired
    private JudicialService judicialService;

    @Autowired
    private CaseWithdrawnNotification caseWithdrawnNotification;

    @Value("${feature.wa.enabled}")
    private boolean isWorkAllocationEnabled;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        final PageBuilder pageBuilder = closeCase(configBuilder);
        closeCaseWarning.addTo(pageBuilder);
        closeCaseReasonSelect.addTo(pageBuilder);
        closeCaseWithdrawalDetails.addTo(pageBuilder);
        closeCaseRejectionDetails.addTo(pageBuilder);
        closeCaseConcessionDetails.addTo(pageBuilder);
        closeCaseStrikeOutDetails.addTo(pageBuilder);
        closeCaseConsentOrder.addTo(pageBuilder);
        closeCaseRule27.addTo(pageBuilder);
        uploadDocuments(pageBuilder);
        closeCaseSelectRecipients.addTo(pageBuilder);
    }

    public PageBuilder closeCase(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
            configBuilder.event(CASEWORKER_CLOSE_THE_CASE)
                .forStates(CaseManagement, ReadyToList)
                .name("Case: Close case")
                .description("Close the case")
                .showSummary()
                .aboutToStartCallback(this::aboutToStart)
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::submitted)
                .grant(CREATE_READ_UPDATE, SUPER_USER, ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER,
                    ST_CIC_HEARING_CENTRE_ADMIN, ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE)
                .grantHistoryOnly(ST_CIC_JUDGE);

        if (isWorkAllocationEnabled) {
            eventBuilder.publishToCamunda();
        }

        return new PageBuilder(eventBuilder);
    }

    private void uploadDocuments(PageBuilder pageBuilder) {
        String pageNameUpload = "closeCaseUploadDocuments";
        pageBuilder.page(pageNameUpload, this::midEvent)
            .pageLabel("Upload case documents")
            .label("LabelCloseCaseUploadDoc",
                """
                    Please upload copies of any information or evidence that you want to add to this case.
                    <h3>Files should be:</h3>
                    uploaded separately and not in one large file
                    a maximum of 100MB in size (larger files must be split)
                    labelled clearly, e.g. applicant-name-decision-notice.pdf




                    Note: If the remove button is disabled, please refresh the page to remove attachments
                    """)


            .complex(CaseData::getCloseCase)
            .optionalWithLabel(CloseCase::getDocumentsUpload, "File Attachments")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {

        final CaseData data = details.getData();
        final List<ListValue<CaseworkerCICDocumentUpload>> uploadedDocuments = data.getCloseCase().getDocumentsUpload();
        final List<String> errors = validateUploadedDocuments(uploadedDocuments);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        final CaseData caseData = details.getData();
        caseData.setCurrentEvent(CASEWORKER_CLOSE_THE_CASE);

        final DynamicList judicialUsersDynamicList = judicialService.getAllUsers(caseData);
        caseData.getCloseCase().setRejectionName(judicialUsersDynamicList);
        caseData.getCloseCase().setStrikeOutName(judicialUsersDynamicList);

        final List<ListValue<CaseworkerCICDocument>> documents = caseData.getCloseCase().getDocuments();
        final List<ListValue<CaseworkerCICDocumentUpload>> uploadedDocuments = convertToCaseworkerCICDocument(documents);
        caseData.getCloseCase().setDocumentsUpload(uploadedDocuments);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        log.info("Caseworker close the case callback invoked for Case Id: {}", details.getId());

        final CaseData caseData = details.getData();
        List<ListValue<CaseworkerCICDocumentUpload>> uploadedDocuments = caseData.getCloseCase().getDocumentsUpload();
        List<ListValue<CaseworkerCICDocument>> documents = updateUploadedDocumentCategory(uploadedDocuments, false);
        caseData.getCloseCase().setDocuments(documents);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(CaseClosed)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {

        String message = MessageUtil.generateSimpleMessage(
            details.getData().getCicCase(),
            "Case closed",
            "Use 'Reinstate case' if this case needs to be reopened in the future."
        );

        try {
            sendCaseWithdrawnNotification(details.getData().getHyphenatedCaseRef(), details.getData());
        } catch (Exception notificationException) {
            log.error("Case close notification failed with exception : {}", notificationException.getMessage());

            return SubmittedCallbackResponse.builder()
                .confirmationHeader(format("# Case close notification failed %n## Please resend the notification"))
                .build();
        }

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(message)
            .build();
    }

    private void sendCaseWithdrawnNotification(String caseNumber, CaseData caseData) {
        CicCase cicCase = caseData.getCicCase();
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartySubject())) {
            caseWithdrawnNotification.sendToSubject(caseData, caseNumber);
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRespondent())) {
            caseWithdrawnNotification.sendToRespondent(caseData, caseNumber);
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRepresentative())) {
            caseWithdrawnNotification.sendToRepresentative(caseData, caseNumber);
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyApplicant())) {
            caseWithdrawnNotification.sendToApplicant(caseData, caseNumber);
        }
    }
}
