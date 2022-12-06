package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueFinalDecisionNotice;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueFinalDecisionSelectTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRoleCIC;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRoleCIC.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRoleCIC.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRoleCIC.SUPER_USER_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseworkerIssueFinalDecision implements CCDConfig<CaseData, State, UserRoleCIC> {
    public static final String CASEWORKER_ISSUE_FINAL_DECISION = "caseworker-issue-final-decision";

    private static final CcdPageConfiguration issueFinalDecisionNotice = new IssueFinalDecisionNotice();
    private static final CcdPageConfiguration issueFinalDecisionSelectTemplate = new IssueFinalDecisionSelectTemplate();

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRoleCIC> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(configBuilder
            .event(CASEWORKER_ISSUE_FINAL_DECISION)
            .forStates(AwaitingOutcome)
            .name("Issue final decision")
            .description("Issue final decision")
            .showEventNotes()
            .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER_CIC)
            .grantHistoryOnly(SOLICITOR));
        issueFinalDecisionNotice.addTo(pageBuilder);
        issueFinalDecisionSelectTemplate.addTo(pageBuilder);
    }

}
