package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueFinalDecisionNotice;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueFinalDecisionSelectTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.notification.CaseFinalDecisionIssuedNotification;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseworkerIssueFinalDecision implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_ISSUE_FINAL_DECISION = "caseworker-issue-final-decision";

    private static final CcdPageConfiguration issueFinalDecisionNotice = new IssueFinalDecisionNotice();
    private static final CcdPageConfiguration issueFinalDecisionSelectTemplate = new IssueFinalDecisionSelectTemplate();

    @Autowired
    private CaseFinalDecisionIssuedNotification caseFinalDecisionIssuedNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(configBuilder
            .event(CASEWORKER_ISSUE_FINAL_DECISION)
            .forStates(AwaitingOutcome)
            .name("Issue final decision")
            .description("Issue final decision")
            .showEventNotes()
            .submittedCallback(this::issued)
            .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
            .grantHistoryOnly(SOLICITOR));
        issueFinalDecisionNotice.addTo(pageBuilder);
        issueFinalDecisionSelectTemplate.addTo(pageBuilder);
    }

    public SubmittedCallbackResponse issued(CaseDetails<CaseData, State> details,
                                            CaseDetails<CaseData, State> beforeDetails) {
        var data = details.getData();
        var cicCase = data.getCicCase();
        String caseNumber = data.getHyphenatedCaseRef();
        final StringBuilder messageLine2 = new StringBuilder(100);
        messageLine2.append(" A notification will be sent  to: ");
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartySubject())) {
            messageLine2.append("Subject, ");
            caseFinalDecisionIssuedNotification.sendToSubject(details.getData(), caseNumber);
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRepresentative())) {
            messageLine2.append("Representative, ");
            caseFinalDecisionIssuedNotification.sendToRepresentative(details.getData(), caseNumber);
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRespondent())) {
            messageLine2.append("Respondent, ");
            caseFinalDecisionIssuedNotification.sendToRespondent(details.getData(), caseNumber);
        }

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Case issued %n##  Final decision has been issued. %n##"
                + "  %s ", messageLine2.substring(0, messageLine2.length() - 2)))
            .build();
    }
}
