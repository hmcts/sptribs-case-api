package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
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

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class ReinstateCase implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_REINSTATE_CASE = "caseworker-reinstate-state";

    private static final CcdPageConfiguration reinstateWarning = new ReinstateWarning();
    private static final CcdPageConfiguration reinstateReason = new ReinstateReasonSelect();
    private static final CcdPageConfiguration reinstateDocuments = new ReinstateUploadDocuments();
    private static final CcdPageConfiguration notifyParties = new ReinstateNotifyParties();


    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        var pageBuilder = reinstateCase(configBuilder);
        reinstateWarning.addTo(pageBuilder);
        reinstateReason.addTo(pageBuilder);
        reinstateDocuments.addTo(pageBuilder);
        notifyParties.addTo(pageBuilder);
    }

    public PageBuilder reinstateCase(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(CASEWORKER_REINSTATE_CASE)
            .forStates(CaseClosed)
            .name("Reinstate case")
            .description("Reinstate case")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::reinstated)
            .showEventNotes()
            .showSummary()
            .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
            .grantHistoryOnly(SOLICITOR));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {

        var caseData = details.getData();
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(CaseManagement)
            .build();
    }

    public SubmittedCallbackResponse reinstated(CaseDetails<CaseData, State> details,
                                                CaseDetails<CaseData, State> beforeDetails) {
        var cicCase = details.getData().getCicCase();
        final StringBuilder messageLine2 = new StringBuilder(100);
        messageLine2.append(" A notification will be sent  to: ");
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartySubject())) {
            messageLine2.append("Subject, ");
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRespondent())) {
            messageLine2.append("Respondent, ");
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRepresentative())) {
            messageLine2.append("Representative, ");
        }

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Case reinstated %n##  The case record will now be reopened"
                + ". %n## %s ", messageLine2.substring(0, messageLine2.length() - 2)))
            .build();
    }

}
