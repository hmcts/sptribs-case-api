package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_EDIT_CICA_CASE_DETAILS;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseStayed;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;


@Component
@Slf4j
public class CaseWorkerEditCicaCaseDetails implements CCDConfig<CaseData, State, UserRole> {


    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {


        configBuilder
            .event(CASEWORKER_EDIT_CICA_CASE_DETAILS)
            .forStates(CaseManagement, AwaitingHearing, AwaitingOutcome, CaseStayed, CaseClosed)
            .name("Case: Edit case details")
            .description("Case: Edit case details")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
            .grantHistoryOnly(SOLICITOR);


    }


    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        CaseData caseData = details.getData();

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(details.getState())
            .build();
    }


}
