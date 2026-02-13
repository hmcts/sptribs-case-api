package uk.gov.hmcts.sptribs.caseworker.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.util.CaseFlagsUtil;
import uk.gov.hmcts.sptribs.ciccase.CicCaseFieldsUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.PartiesCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.event.page.ApplicantDetails;
import uk.gov.hmcts.sptribs.common.event.page.CaseCategorisationDetails;
import uk.gov.hmcts.sptribs.common.event.page.ContactPreferenceDetails;
import uk.gov.hmcts.sptribs.common.event.page.DateOfInitialCicaDecision;
import uk.gov.hmcts.sptribs.common.event.page.DateOfReceipt;
import uk.gov.hmcts.sptribs.common.event.page.EditCicaCaseDetailsPage;
import uk.gov.hmcts.sptribs.common.event.page.FurtherDetails;
import uk.gov.hmcts.sptribs.common.event.page.RepresentativeDetails;
import uk.gov.hmcts.sptribs.common.event.page.SelectParties;
import uk.gov.hmcts.sptribs.common.event.page.SubjectDetails;
import uk.gov.hmcts.sptribs.common.service.SubmissionService;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_EDIT_CASE;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.DSS_Submitted;
import static uk.gov.hmcts.sptribs.ciccase.model.State.ReadyToList;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_RESPONDENT;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class CaseworkerEditCase implements CCDConfig<CaseData, State, UserRole> {

    private final CcdPageConfiguration editCaseCategorisationDetails = new CaseCategorisationDetails();
    private final CcdPageConfiguration editCicaCaseDetailsPage = new EditCicaCaseDetailsPage();
    private static final CcdPageConfiguration dateOfInitialCicaDecision = new DateOfInitialCicaDecision();
    private static final CcdPageConfiguration dateOfReceipt = new DateOfReceipt();
    private final CcdPageConfiguration editSelectedPartiesDetails = new SelectParties();
    private final CcdPageConfiguration editSubjectDetails = new SubjectDetails();
    private final CcdPageConfiguration editApplicantDetails = new ApplicantDetails();
    private final CcdPageConfiguration editRepresentativeDetails = new RepresentativeDetails();
    private static final CcdPageConfiguration editFurtherDetails = new FurtherDetails();
    private static final CcdPageConfiguration editContactPreferenceDetails = new ContactPreferenceDetails();

    private final SubmissionService submissionService;

    @Autowired
    public CaseworkerEditCase(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);
        editCaseCategorisationDetails.addTo(pageBuilder);
        editCicaCaseDetailsPage.addTo(pageBuilder);
        dateOfInitialCicaDecision.addTo(pageBuilder);
        dateOfReceipt.addTo(pageBuilder);
        editSelectedPartiesDetails.addTo(pageBuilder);
        editSubjectDetails.addTo(pageBuilder);
        editApplicantDetails.addTo(pageBuilder);
        editRepresentativeDetails.addTo(pageBuilder);
        editContactPreferenceDetails.addTo(pageBuilder);
        editFurtherDetails.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        CaseData data = details.getData();
        CaseData beforeData = beforeDetails.getData();

        if (checkNull(beforeData) && beforeData.getCicCase().getPartiesCIC().contains(PartiesCIC.REPRESENTATIVE)
            && checkNull(data) && !data.getCicCase().getPartiesCIC().contains(PartiesCIC.REPRESENTATIVE)) {
            CicCaseFieldsUtil.removeRepresentative(data);
        }

        if (checkNull(beforeData) && beforeData.getCicCase().getPartiesCIC().contains(PartiesCIC.APPLICANT)
            && checkNull(data) && !data.getCicCase().getPartiesCIC().contains(PartiesCIC.APPLICANT)) {
            CicCaseFieldsUtil.removeApplicant(data);
        }

        CaseDetails<CaseData, State> submittedDetails = submissionService.submitApplication(details);
        data = submittedDetails.getData();
        State state = beforeDetails.getState();
        if (state == DSS_Submitted) {
            state = Submitted;
        }

        CaseFlagsUtil.updateOrInitialiseFlags(data);
        CicCaseFieldsUtil.calculateAndSetIsCaseInTime(data);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(state)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Case Updated")
            .build();
    }

    private PageBuilder addEventConfig(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder = configBuilder
            .event(CASEWORKER_EDIT_CASE)
            .forStates(DSS_Submitted, Submitted, CaseManagement, ReadyToList, AwaitingHearing, AwaitingOutcome)
            .name("Case: Edit case")
            .description("Case: Edit case")
            .showSummary()
            .grant(CREATE_READ_UPDATE, SUPER_USER,
                ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, ST_CIC_RESPONDENT, ST_CIC_WA_CONFIG_USER)
            .grantHistoryOnly(ST_CIC_JUDGE)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .publishToCamunda();

        return new PageBuilder(eventBuilder);
    }

    private boolean checkNull(CaseData data) {
        return null != data.getCicCase() && null != data.getCicCase().getPartiesCIC();
    }

}
