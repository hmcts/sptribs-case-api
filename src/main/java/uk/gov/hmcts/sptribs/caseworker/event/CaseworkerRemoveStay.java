package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.RemoveStay;
import uk.gov.hmcts.sptribs.caseworker.model.NextState;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseStayed;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseworkerRemoveStay implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_REMOVE_STAY = "caseworker-remove-stay";

    private static final CcdPageConfiguration removeStay = new RemoveStay();

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        var pageBuilder = remove(configBuilder);
        removeStay.addTo(pageBuilder);
    }

    public PageBuilder remove(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(CASEWORKER_REMOVE_STAY)
            .forStates(CaseStayed)
            .name("Remove Stay")
            .showSummary(true)
            .description("Remove Stay")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::stayRemoved)
            .showEventNotes()
            .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
            .grantHistoryOnly(SOLICITOR));

    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Caseworker stay the case callback invoked for Case Id: {}", details.getId());

        var caseData = details.getData();
        caseData.setCaseStay(null);
        caseData.getCicCase().setAfterStayState(NextState.CaseManagement);

        State newState = State.valueOf(caseData.getCicCase().getAfterStayState().getName());
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(newState)
            .build();
    }

    public SubmittedCallbackResponse stayRemoved(CaseDetails<CaseData, State> details,
                                                 CaseDetails<CaseData, State> beforeDetails) {
        var caseData = details.getData();
        caseData.setRemoveCaseStay(null);
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Stay Removed from Case"))
            .build();
    }
}
