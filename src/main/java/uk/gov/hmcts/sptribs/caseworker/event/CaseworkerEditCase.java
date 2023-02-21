package uk.gov.hmcts.sptribs.caseworker.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.PartiesCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.event.page.ApplicantDetails;
import uk.gov.hmcts.sptribs.common.event.page.CaseCategorisationDetails;
import uk.gov.hmcts.sptribs.common.event.page.ContactPreferenceDetails;
import uk.gov.hmcts.sptribs.common.event.page.DateOfReceipt;
import uk.gov.hmcts.sptribs.common.event.page.FurtherDetails;
import uk.gov.hmcts.sptribs.common.event.page.RepresentativeDetails;
import uk.gov.hmcts.sptribs.common.event.page.SelectParties;
import uk.gov.hmcts.sptribs.common.event.page.SubjectDetails;
import uk.gov.hmcts.sptribs.common.service.SubmissionService;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_EDIT_CASE;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
public class CaseworkerEditCase implements CCDConfig<CaseData, State, UserRole> {

    private final CcdPageConfiguration editCaseCategorisationDetails = new CaseCategorisationDetails();
    private static final CcdPageConfiguration dateOfReceipt = new DateOfReceipt();
    private final CcdPageConfiguration editSelectedPartiesDetails = new SelectParties();
    private final CcdPageConfiguration editSubjectDetails = new SubjectDetails();
    private final CcdPageConfiguration editApplicantDetails = new ApplicantDetails();
    private final CcdPageConfiguration editRepresentativeDetails = new RepresentativeDetails();
    private static final CcdPageConfiguration editFurtherDetails = new FurtherDetails();
    private static final CcdPageConfiguration editContactPreferenceDetails = new ContactPreferenceDetails();

    @Autowired
    private final SubmissionService submissionService;

    public CaseworkerEditCase(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        var pageBuilder = addEventConfig(configBuilder);
        editCaseCategorisationDetails.addTo(pageBuilder);
        dateOfReceipt.addTo(pageBuilder);
        editSelectedPartiesDetails.addTo(pageBuilder);
        editSubjectDetails.addTo(pageBuilder);
        editApplicantDetails.addTo(pageBuilder);
        editRepresentativeDetails.addTo(pageBuilder);
        editContactPreferenceDetails.addTo(pageBuilder);
        editFurtherDetails.addTo(pageBuilder);
    }

    private PageBuilder addEventConfig(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(CASEWORKER_EDIT_CASE)
            .forStates(Submitted, CaseManagement, AwaitingHearing, AwaitingOutcome)
            .name("Case: Edit case")
            .description("")
            .showSummary()
            .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        CaseData data = details.getData();
        CaseData beforeData = beforeDetails.getData();
        if (checkNull(beforeData) && beforeData.getCicCase().getPartiesCIC().contains(PartiesCIC.REPRESENTATIVE)
            && checkNull(data) && !data.getCicCase().getPartiesCIC().contains(PartiesCIC.REPRESENTATIVE)) {
            data.getCicCase().removeRepresentative();
        }

        if (checkNull(beforeData) && beforeData.getCicCase().getPartiesCIC().contains(PartiesCIC.APPLICANT)
            && checkNull(data) && !data.getCicCase().getPartiesCIC().contains(PartiesCIC.APPLICANT)) {
            data.getCicCase().removeApplicant();
        }

        var submittedDetails = submissionService.submitApplication(details);
        data = submittedDetails.getData();

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Case Updated")
            .build();
    }

    private boolean checkNull(CaseData data) {
        return null != data.getCicCase() && null != data.getCicCase().getPartiesCIC();
    }
}
