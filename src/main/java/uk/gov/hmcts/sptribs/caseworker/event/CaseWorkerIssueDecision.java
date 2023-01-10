package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueDecisionNotice;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueDecisionSelectRecipients;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueDecisionSelectTemplate;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueDecisionUploadNotice;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.notification.CaseUnstayedNotification;
import uk.gov.hmcts.sptribs.common.notification.DecisionIssuedNotification;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_ISSUE_DECISION;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseWorkerIssueDecision implements CCDConfig<CaseData, State, UserRole> {

    private static final CcdPageConfiguration issueDecisionNotice = new IssueDecisionNotice();
    private static final CcdPageConfiguration issueDecisionSelectTemplate = new IssueDecisionSelectTemplate();
    private static final CcdPageConfiguration issueDecisionUploadNotice = new IssueDecisionUploadNotice();
    private static final CcdPageConfiguration issueDecisionSelectRecipients = new IssueDecisionSelectRecipients();

    @Autowired
    private DecisionIssuedNotification decisionIssuedNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(configBuilder
            .event(CASEWORKER_ISSUE_DECISION)
            .forStates(AwaitingOutcome)
            .name("Issue a decision")
            .description("Issue a decision")
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
            .grantHistoryOnly(SOLICITOR));
        issueDecisionNotice.addTo(pageBuilder);
        issueDecisionSelectTemplate.addTo(pageBuilder);
        issueDecisionUploadNotice.addTo(pageBuilder);
        issueDecisionSelectRecipients.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        var caseData = details.getData();
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(CaseManagement)//or AwaitingHearing
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        var caseData = details.getData();

        sendIssueDecisionNotification(caseData.getHyphenatedCaseRef(), caseData);

        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Issue Decision")
            .build();
    }

    private void sendIssueDecisionNotification(String caseNumber, CaseData data) {
        CicCase cicCase = data.getCicCase();

        if (!cicCase.getSubjectCIC().isEmpty()) {
            decisionIssuedNotification.sendToSubject(data, caseNumber);
        }

        if (!cicCase.getApplicantCIC().isEmpty()) {
            decisionIssuedNotification.sendToApplicant(data, caseNumber);
        }

        if (!cicCase.getRepresentativeCIC().isEmpty()) {
            decisionIssuedNotification.sendToRepresentative(data, caseNumber);
        }
    }

}
