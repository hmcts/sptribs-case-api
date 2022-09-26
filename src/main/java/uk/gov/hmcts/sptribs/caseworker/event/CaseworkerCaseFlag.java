package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;

import uk.gov.hmcts.sptribs.caseworker.event.page.FlagAdditionalInfo;

import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.ciccase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseworkerCaseFlag implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_CASE_FLAG = "caseworker-case-flag";


    private static final CcdPageConfiguration flagAdditionalInfo = new FlagAdditionalInfo();

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        var pageBuilder = caseFlag(configBuilder);
        flagAdditionalInfo.addTo(pageBuilder);
    }

    public PageBuilder caseFlag(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(CASEWORKER_CASE_FLAG)
            .forStates(POST_SUBMISSION_STATES)
            .name("Create a case flag")
            .showSummary()
            .description("Create a case flag")
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::flagCreated)
            .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
            .grantHistoryOnly(SOLICITOR))
            .page("addFlag")
            .label("addFlag", "<H2>Add a Flag to this case</H2>")
        ;
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Caseworker stay the case callback invoked for Case Id: {}", details.getId());

        var caseData = details.getData();

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public SubmittedCallbackResponse flagCreated(CaseDetails<CaseData, State> details,
                                                 CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Case Flag created %n## This Flag has been added to case"))
            .build();
    }
}
