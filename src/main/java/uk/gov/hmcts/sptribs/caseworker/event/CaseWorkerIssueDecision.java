package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueDecisionNotice;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueDecisionSelectRecipients;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueDecisionSelectTemplate;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueDecisionUploadNotice;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseWorkerIssueDecision implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_ISSUE_DECISION = "caseworker-issue-decision";

    private static final CcdPageConfiguration issueDecisionNotice = new IssueDecisionNotice();
    private static final CcdPageConfiguration issueDecisionSelectTemplate = new IssueDecisionSelectTemplate();
    private static final CcdPageConfiguration issueDecisionUploadNotice = new IssueDecisionUploadNotice();
    private static final CcdPageConfiguration issueDecisionSelectRecipients = new IssueDecisionSelectRecipients();


    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(configBuilder
            .event(CASEWORKER_ISSUE_DECISION)
            .forStates(AwaitingOutcome)
            .name("Issue a decision")
            .description("Issue a decision")
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
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

}
