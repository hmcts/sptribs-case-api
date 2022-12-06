package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.AmendOrderDueDates;
import uk.gov.hmcts.sptribs.caseworker.event.page.ManageSelectOrderTemplates;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRoleCIC;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseStayed;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRoleCIC.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRoleCIC.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRoleCIC.SUPER_USER_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;


@Component
@Slf4j
public class CaseWorkerManageOrderDueDate implements CCDConfig<CaseData, State, UserRoleCIC> {

    private static final CcdPageConfiguration manageSelectOrderTemplates = new ManageSelectOrderTemplates();
    private static final CcdPageConfiguration amendOrderDueDates = new AmendOrderDueDates();

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRoleCIC> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(
            configBuilder
                .event("Manage order due dates")
                .forStates(CaseManagement, AwaitingHearing, AwaitingOutcome, CaseClosed, CaseStayed)
                .name("Manage order due date")
                .description("Manage order due date")
                .showEventNotes()
                .showSummary()
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::orderDatesManaged)
                .showEventNotes()
                .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER_CIC)
                .grantHistoryOnly(SOLICITOR));

        manageSelectOrderTemplates.addTo(pageBuilder);
        amendOrderDueDates.addTo(pageBuilder);


    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {


        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .state(details.getState())
            .build();

    }

    public SubmittedCallbackResponse orderDatesManaged(CaseDetails<CaseData, State> details,
                                                  CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(" Due dates amended.")
            .build();
    }
}
