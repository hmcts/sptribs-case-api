package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.ReinstateNotifyParties;
import uk.gov.hmcts.sptribs.caseworker.event.page.ReinstateReasonSelect;
import uk.gov.hmcts.sptribs.caseworker.event.page.ReinstateUploadDocuments;
import uk.gov.hmcts.sptribs.caseworker.event.page.ReinstateWarning;
import uk.gov.hmcts.sptribs.caseworker.util.MessageUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.notification.CaseReinstatedNotification;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocumentUpload;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_REINSTATE_CASE;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.addDateToUploadedDocuments;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.removeDateFromUploadedDocuments;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.updateCategoryToCaseworkerDocument;

@Component
@Slf4j
public class ReinstateCase implements CCDConfig<CaseData, State, UserRole> {

    private static final CcdPageConfiguration reinstateWarning = new ReinstateWarning();
    private static final CcdPageConfiguration reinstateReason = new ReinstateReasonSelect();
    private static final CcdPageConfiguration reinstateDocuments = new ReinstateUploadDocuments();
    private static final CcdPageConfiguration notifyParties = new ReinstateNotifyParties();

    @Autowired
    private CaseReinstatedNotification caseReinstatedNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        final PageBuilder pageBuilder = reinstateCase(configBuilder);
        reinstateWarning.addTo(pageBuilder);
        reinstateReason.addTo(pageBuilder);
        reinstateDocuments.addTo(pageBuilder);
        notifyParties.addTo(pageBuilder);
    }

    public PageBuilder reinstateCase(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(CASEWORKER_REINSTATE_CASE)
            .forStates(CaseClosed)
            .name("Case: Reinstate case")
            .description("Case: Reinstate case")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .aboutToStartCallback(this::aboutToStart)
            .submittedCallback(this::reinstated)
            .showSummary()
            .grant(CREATE_READ_UPDATE,
                ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE)
            .grantHistoryOnly(
                ST_CIC_CASEWORKER,
                ST_CIC_SENIOR_CASEWORKER,
                ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER,
                ST_CIC_SENIOR_JUDGE,
                ST_CIC_JUDGE)
        );
    }

    private AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        final CaseData caseData = details.getData();
        List<ListValue<CaseworkerCICDocument>> documents = caseData.getCicCase().getReinstateDocuments();
        List<ListValue<CaseworkerCICDocumentUpload>> uploadedDocuments = removeDateFromUploadedDocuments(documents);
        caseData.getCicCase().setReinstateDocumentsUpload(uploadedDocuments);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {

        final CaseData caseData = details.getData();
        List<ListValue<CaseworkerCICDocumentUpload>> uploadedDocuments = caseData.getCicCase().getReinstateDocumentsUpload();
        List<ListValue<CaseworkerCICDocument>> documents = addDateToUploadedDocuments(uploadedDocuments);
        caseData.getCicCase().setReinstateDocumentsUpload(new ArrayList<>());

        updateCategoryToCaseworkerDocument(documents);
        caseData.getCicCase().setReinstateDocuments(documents);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(CaseManagement)
            .build();
    }

    public SubmittedCallbackResponse reinstated(CaseDetails<CaseData, State> details,
                                                CaseDetails<CaseData, State> beforeDetails) {
        try {
            sendCaseReinstatedNotification(details.getData().getHyphenatedCaseRef(), details.getData());
        } catch (Exception notificationException) {
            log.error("Case Reinstate notification failed with exception : {}", notificationException.getMessage());
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(format("# Case Reinstate notification failed %n## Please resend the notification"))
                .build();
        }
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Case reinstated %n##  The case record will now be reopened"
                + ". %n## %s ", MessageUtil.generateSimpleMessage(details.getData().getCicCase())))
            .build();
    }

    private void sendCaseReinstatedNotification(String caseNumber, CaseData data) {
        CicCase cicCase = data.getCicCase();

        if (!isEmpty(cicCase.getNotifyPartySubject())) {
            caseReinstatedNotification.sendToSubject(data, caseNumber);
        }
        if (!isEmpty(cicCase.getNotifyPartyRepresentative())) {
            caseReinstatedNotification.sendToRepresentative(data, caseNumber);
        }
        if (!isEmpty(cicCase.getNotifyPartyRespondent())) {
            caseReinstatedNotification.sendToRespondent(data, caseNumber);
        }
        if (!isEmpty(cicCase.getNotifyPartyApplicant())) {
            caseReinstatedNotification.sendToApplicant(data, caseNumber);
        }
    }

}
