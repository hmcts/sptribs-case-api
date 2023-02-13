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
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.CloseCaseConcessionDetails;
import uk.gov.hmcts.sptribs.caseworker.event.page.CloseCaseConsentOrder;
import uk.gov.hmcts.sptribs.caseworker.event.page.CloseCaseReasonSelect;
import uk.gov.hmcts.sptribs.caseworker.event.page.CloseCaseRejectionDetails;
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
import uk.gov.hmcts.sptribs.judicialrefdata.JudicialService;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CLOSE_THE_CASE;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

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
    private static final CcdPageConfiguration closeCaseSelectRecipients = new CloseCaseSelectRecipients();

    @Autowired
    private JudicialService judicialService;

    @Autowired
    private CaseWithdrawnNotification caseWithdrawnNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        var pageBuilder = closeCase(configBuilder);
        closeCaseWarning.addTo(pageBuilder);
        closeCaseReasonSelect.addTo(pageBuilder);
        closeCaseWithdrawalDetails.addTo(pageBuilder);
        closeCaseRejectionDetails.addTo(pageBuilder);
        closeCaseConcessionDetails.addTo(pageBuilder);
        closeCaseStrikeOutDetails.addTo(pageBuilder);
        closeCaseConsentOrder.addTo(pageBuilder);
        uploadDocuments(pageBuilder);
        closeCaseSelectRecipients.addTo(pageBuilder);
    }

    public PageBuilder closeCase(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(CASEWORKER_CLOSE_THE_CASE)
            .forStates(CaseManagement)
            .name("Case: Close case")
            .showSummary()
            .description("Close the case")
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::closed)
            .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
            .grantHistoryOnly(SOLICITOR));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        var caseData = details.getData();
        caseData.setCurrentEvent(CASEWORKER_CLOSE_THE_CASE);

        DynamicList judicialUsersDynamicList = judicialService.getAllUsers();
        caseData.getCloseCase().setRejectionName(judicialUsersDynamicList);
        caseData.getCloseCase().setStrikeOutName(judicialUsersDynamicList);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Caseworker close the case callback invoked for Case Id: {}", details.getId());
        var caseData = details.getData();
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(CaseClosed)
            .build();
    }

    public SubmittedCallbackResponse closed(CaseDetails<CaseData, State> details,
                                            CaseDetails<CaseData, State> beforeDetails) {
        String message = MessageUtil.generateSimpleMessage(details.getData().getCicCase(), "Case closed",
            "Use 'Reinstate case' if this case needs to be reopened in the future.");
        sendCaseWithdrawnNotification(details.getData().getHyphenatedCaseRef(), details.getData());

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
    }

    private void uploadDocuments(PageBuilder pageBuilder) {
        String pageNameUpload = "closeCaseUploadDocuments";
        pageBuilder.page(pageNameUpload)
            .pageLabel("Upload case documents")
            .label("LabelCloseCaseUploadDoc",
                """
                    Please upload copies of any information or evidence that you want to add to this case.
                    <h3>Files should be:</h3>
                    uploaded separately and not in one large file
                    a maximum of 100MB in size (larger files must be split)
                    labelled clearly, e.g. applicant-name-decision-notice.pdf
                    """)
            .complex(CaseData::getCloseCase)
            .optionalWithLabel(CloseCase::getDocuments, "File Attachments")
            .done();
    }
}
