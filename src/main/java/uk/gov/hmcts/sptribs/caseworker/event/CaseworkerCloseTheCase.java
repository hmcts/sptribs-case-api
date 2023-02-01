package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.CloseCaseConcessionDetails;
import uk.gov.hmcts.sptribs.caseworker.event.page.CloseCaseConsentOrder;
import uk.gov.hmcts.sptribs.caseworker.event.page.CloseCaseReasonSelect;
import uk.gov.hmcts.sptribs.caseworker.event.page.CloseCaseRejectionDetails;
import uk.gov.hmcts.sptribs.caseworker.event.page.CloseCaseWarning;
import uk.gov.hmcts.sptribs.caseworker.event.page.CloseCaseWithdrawalDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CLOSE_THE_CASE;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseworkerCloseTheCase implements CCDConfig<CaseData, State, UserRole> {

    private static final CcdPageConfiguration closeCaseWarning = new CloseCaseWarning();
    private static final CcdPageConfiguration closeCaseReasonSelect = new CloseCaseReasonSelect();
    private static final CcdPageConfiguration closeCaseWithdrawalDetails = new CloseCaseWithdrawalDetails();
    private static final CcdPageConfiguration closeCaseRejectionDetails = new CloseCaseRejectionDetails();
    private static final CcdPageConfiguration closeCaseConcessionDetails = new CloseCaseConcessionDetails();
    private static final CcdPageConfiguration closeCaseConsentOrder = new CloseCaseConsentOrder();


    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        var pageBuilder = closeCase(configBuilder);
        closeCaseWarning.addTo(pageBuilder);
        closeCaseReasonSelect.addTo(pageBuilder);
        closeCaseWithdrawalDetails.addTo(pageBuilder);
        closeCaseRejectionDetails.addTo(pageBuilder);
        closeCaseConcessionDetails.addTo(pageBuilder);
        closeCaseConsentOrder.addTo(pageBuilder);
    }

    public PageBuilder closeCase(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(CASEWORKER_CLOSE_THE_CASE)
            .forStates(CaseManagement)
            .name("Case: Close case")
            .showSummary()
            .description("Close the case")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::closed)
            .showEventNotes()
            .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
            .grantHistoryOnly(SOLICITOR));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Caseworker close the case callback invoked for Case Id: {}", details.getId());
        var caseData = details.getData();
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(CaseClosed)
            .build();
    }

    public SubmittedCallbackResponse closed(CaseDetails<CaseData, State> details,
                                            CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Case closed")
            .build();
    }
}
