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
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueDecisionMainContent;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueDecisionNotice;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueDecisionPreviewTemplate;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueDecisionSelectRecipients;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueDecisionSelectTemplate;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueDecisionUploadNotice;
import uk.gov.hmcts.sptribs.caseworker.util.MessageUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.services.DocumentsService;
import uk.gov.hmcts.sptribs.notification.dispatcher.DecisionIssuedNotification;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.FAILED_SAVING_DOCUMENT_TO_DATABASE;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.FAILED_SAVING_DOCUMENT_WITH_NO_FILENAME_TO_DATABASE;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_ISSUE_DECISION;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
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
@RequiredArgsConstructor
@Slf4j
public class CaseworkerIssueDecision implements CCDConfig<CaseData, State, UserRole> {

    private static final CcdPageConfiguration issueDecisionNotice = new IssueDecisionNotice();
    private static final CcdPageConfiguration issueDecisionSelectTemplate = new IssueDecisionSelectTemplate();
    private static final CcdPageConfiguration issueDecisionPreviewTemplate = new IssueDecisionPreviewTemplate();
    private static final CcdPageConfiguration issueDecisionUploadNotice = new IssueDecisionUploadNotice();
    private static final CcdPageConfiguration issueDecisionSelectRecipients = new IssueDecisionSelectRecipients();
    private static final CcdPageConfiguration issueDecisionMainContent = new IssueDecisionMainContent();

    private final CcdPageConfiguration issueDecisionFooter;
    private final DecisionIssuedNotification decisionIssuedNotification;
    private final Clock clock;
    private final DocumentsService documentsService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
            configBuilder
                .event(CASEWORKER_ISSUE_DECISION)
                .forStates(AwaitingOutcome)
                .name("Decision: Issue a decision")
                .description("Decision: Issue a decision")
                .showSummary()
                .aboutToStartCallback(this::aboutToStart)
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::submitted)
                .grant(CREATE_READ_UPDATE, SUPER_USER,
                    ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                    ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, ST_CIC_JUDGE, ST_CIC_WA_CONFIG_USER)
                .publishToCamunda();

        final PageBuilder pageBuilder = new PageBuilder(eventBuilder);
        issueDecisionNotice.addTo(pageBuilder);
        issueDecisionSelectTemplate.addTo(pageBuilder);
        issueDecisionMainContent.addTo(pageBuilder);
        issueDecisionUploadNotice.addTo(pageBuilder);
        issueDecisionFooter.addTo(pageBuilder);
        issueDecisionPreviewTemplate.addTo(pageBuilder);
        issueDecisionSelectRecipients.addTo(pageBuilder);

    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        final CaseData caseData = details.getData();

        caseData.setDecisionSignature("");

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = details.getData();
        final CICDocument decisionDocument = caseData.getCaseIssueDecision().getDecisionDocument();

        List<String> errors = new ArrayList<>();

        if (decisionDocument != null && decisionDocument.getDocumentLink() != null) {
            decisionDocument.getDocumentLink().setCategoryId("TD");
            try {
                documentsService.buildAndSaveNewDocumentEntity(
                    decisionDocument.getDocumentLink(),
                    details.getId(),
                    false
                );
            } catch (RuntimeException e) {
                if (decisionDocument.getDocumentLink().getFilename() != null
                    && !decisionDocument.getDocumentLink().getFilename().isEmpty()) {
                    log.error("Document entity with filename {} could not be saved: {}",
                        decisionDocument.getDocumentLink().getFilename(), e.getMessage());
                    errors.add(FAILED_SAVING_DOCUMENT_TO_DATABASE + decisionDocument.getDocumentLink().getFilename());
                } else {
                    log.error("Document entity has no filename");
                    errors.add(FAILED_SAVING_DOCUMENT_WITH_NO_FILENAME_TO_DATABASE);
                }
            }
        }

        caseData.getCaseIssueDecision().setDecisionDate(LocalDate.now(this.clock));

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(CaseManagement)
            .errors(errors)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        try {
            sendIssueDecisionNotification(details.getData().getHyphenatedCaseRef(), details.getData());
        } catch (Exception notificationException) {
            log.error("Issue a decision notification failed with exception : {}", notificationException.getMessage());
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(format("# Issue a decision notification failed %n## Please resend the notification"))
                .build();
        }

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Decision notice issued %n## %s",
                MessageUtil.generateSimpleMessage(details.getData().getCicCase())))
            .build();
    }

    private void sendIssueDecisionNotification(String caseNumber, CaseData data) {

        if (!CollectionUtils.isEmpty(data.getCicCase().getNotifyPartySubject())) {
            decisionIssuedNotification.sendToSubject(data, caseNumber);
        }
        if (!CollectionUtils.isEmpty(data.getCicCase().getNotifyPartyRespondent())) {
            decisionIssuedNotification.sendToRespondent(data, caseNumber);
        }
        if (!CollectionUtils.isEmpty(data.getCicCase().getNotifyPartyRepresentative())) {
            decisionIssuedNotification.sendToRepresentative(data, caseNumber);
        }
        if (!CollectionUtils.isEmpty(data.getCicCase().getNotifyPartyApplicant())) {
            decisionIssuedNotification.sendToApplicant(data, caseNumber);
        }
    }
}
