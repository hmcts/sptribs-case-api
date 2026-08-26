package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
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
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocumentUpload;
import uk.gov.hmcts.sptribs.notification.NotificationConstantProfiles;
import uk.gov.hmcts.sptribs.notification.dispatcher.CaseReinstatedNotification;
import uk.gov.hmcts.sptribs.notification.dispatcher.NotificationDispatcher;
import uk.gov.hmcts.sptribs.notification.model.NotificationContext;
import uk.gov.hmcts.sptribs.notification.model.NotificationContextRequest;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_REINSTATE_CASE;
import static uk.gov.hmcts.sptribs.caseworker.util.MessageUtil.generateSimpleErrorMessage;
import static uk.gov.hmcts.sptribs.caseworker.util.MessageUtil.generateSimpleMessageFromCorrespondenceParties;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.convertToCaseworkerCICDocument;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.updateUploadedDocumentCategory;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReinstateCase implements CCDConfig<CaseData, State, UserRole> {

    private static final CcdPageConfiguration reinstateWarning = new ReinstateWarning();
    private static final CcdPageConfiguration reinstateReason = new ReinstateReasonSelect();
    private static final CcdPageConfiguration reinstateDocuments = new ReinstateUploadDocuments();
    private static final CcdPageConfiguration notifyParties = new ReinstateNotifyParties();

    private final CaseReinstatedNotification caseReinstatedNotification;
    private final NotificationDispatcher notificationDispatcher;

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
            .submittedCallback(this::submitted)
            .showSummary()
            .grant(CREATE_READ_UPDATE,
                ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE)
            .grantHistoryOnly(ST_CIC_JUDGE)
        );
    }

    private AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        final CaseData caseData = details.getData();
        List<ListValue<CaseworkerCICDocument>> documents = caseData.getCicCase().getReinstateDocuments();
        List<ListValue<CaseworkerCICDocumentUpload>> uploadedDocuments = convertToCaseworkerCICDocument(documents);
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
        List<ListValue<CaseworkerCICDocument>> documents = updateUploadedDocumentCategory(uploadedDocuments, false);
        caseData.getCicCase().setReinstateDocumentsUpload(new ArrayList<>());
        caseData.getCicCase().setReinstateDocuments(documents);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(CaseManagement)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {


        final CaseData caseData = details.getData();
        String caseNumber = caseData.getHyphenatedCaseRef();

        NotificationContextRequest request = NotificationContextRequest.builder()
            .caseData(caseData)
            .caseReference(caseNumber)
            .notification(caseReinstatedNotification)
            .build();

        NotificationContext notificationContext = NotificationConstantProfiles.REINSTATE_CASE.buildContext(
            request);

        notificationDispatcher.sendToCorrespondenceParties(notificationContext);

        if (CollectionUtils.isEmpty(notificationContext.getErrors())) {
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(format("# Case reinstated. %n## The case record will now be reopened. %n## %s ",
                    generateSimpleMessageFromCorrespondenceParties(notificationContext.getCorrespondenceParties())))
                .build();
        } else {
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(
                    format("# Case Reinstate notification failed %n## %s %n## Please resend the notification.",
                        generateSimpleErrorMessage(notificationContext.getErrors()))
                )
                .build();
        }
    }
}
