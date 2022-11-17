package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
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
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
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
        final StringBuilder emailMessage = getEmailMessage(cicCase);
        StringBuilder postMessage = getPostMessage(cicCase);
        String message = "";
        if (null != postMessage) {
            message = format("# Case reinstated %n##  The case record will now be reopened."
                + " %s %n##  %s", emailMessage.substring(0, emailMessage.length() - 2), postMessage.substring(0, postMessage.length() - 2));
        } else {
            message = format("# Case reinstated %n##  The case record will now be reopened. "
                + " %s ", emailMessage.substring(0, emailMessage.length() - 2));

        }

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(message)
            .build();
    }

    private StringBuilder getPostMessage(final CicCase cicCase) {
        boolean post = false;
        StringBuilder postMessage = new StringBuilder(100);
        postMessage.append("It will be sent via post to:");
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartySubject())
            && !ObjectUtils.isEmpty(cicCase.getAddress())) {
            postMessage.append("Subject, ");
            post = true;
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRepresentative())
            && !ObjectUtils.isEmpty(cicCase.getRepresentativeAddress())) {
            postMessage.append("Representative, ");
            post = true;
        }
        if (post) {
            return postMessage;
        }
        return null;
    }

    private StringBuilder getEmailMessage(final CicCase cicCase) {
        final StringBuilder messageLine = new StringBuilder(100);
        messageLine.append(" A notification will be sent via email to: ");

        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartySubject())
            && StringUtils.hasText(cicCase.getEmail())) {
            messageLine.append("Subject, ");
            cicCase.setNotifyPartySubject(null);
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRespondent())) {
            messageLine.append("Respondent, ");
            cicCase.setNotifyPartyRespondent(null);
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRepresentative())
            && StringUtils.hasText(cicCase.getRepresentativeEmailAddress())) {
            messageLine.append("Representative, ");
            cicCase.setNotifyPartyRepresentative(null);
        }
        return messageLine;
    }

}
