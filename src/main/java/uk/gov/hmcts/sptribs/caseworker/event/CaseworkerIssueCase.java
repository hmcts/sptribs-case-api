package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueCaseAdditionalDocument;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueCaseNotifyParties;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseworkerIssueCase implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_ISSUE_CASE = "caseworker-issue-case";

    private static final CcdPageConfiguration issueCaseAdditionalDocument = new IssueCaseAdditionalDocument();
    private static final CcdPageConfiguration issueCaseNotifyParties = new IssueCaseNotifyParties();

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(configBuilder
            .event(CASEWORKER_ISSUE_CASE)
            .forStates(CaseManagement)
            .name("Issue case to respondent")
            .description("Issue case to respondent\"")
            .showEventNotes()
            .showSummary()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::issued)
            .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
            .grantHistoryOnly(SOLICITOR));
        issueCaseAdditionalDocument.addTo(pageBuilder);
        issueCaseNotifyParties.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        var caseData = details.getData();
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(details.getState())
            .build();
    }

    public SubmittedCallbackResponse issued(CaseDetails<CaseData, State> details,
                                            CaseDetails<CaseData, State> beforeDetails) {
        var cicCase = details.getData().getCicCase();
        final StringBuilder messageLine2 = new StringBuilder(100);
        messageLine2.append(" A notification will be sent  to: ");
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartySubject())) {
            messageLine2.append("Subject, ");
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyApplicant())) {
            messageLine2.append("Applicant, ");
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRepresentative())) {
            messageLine2.append("Representative, ");
        }

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Case issued %n##  This case has now been issued. "
                + ". %n## %s ", messageLine2.substring(0, messageLine2.length() - 2)))
            .build();
    }
}
