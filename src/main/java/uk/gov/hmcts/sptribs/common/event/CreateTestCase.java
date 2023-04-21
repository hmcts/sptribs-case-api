package uk.gov.hmcts.sptribs.common.event;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.SecurityClass;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.event.page.ApplicantDetails;
import uk.gov.hmcts.sptribs.common.event.page.CaseCategorisationDetails;
import uk.gov.hmcts.sptribs.common.event.page.CaseUploadDocuments;
import uk.gov.hmcts.sptribs.common.event.page.ContactPreferenceDetails;
import uk.gov.hmcts.sptribs.common.event.page.DateOfReceipt;
import uk.gov.hmcts.sptribs.common.event.page.FurtherDetails;
import uk.gov.hmcts.sptribs.common.event.page.RepresentativeDetails;
import uk.gov.hmcts.sptribs.common.event.page.SelectParties;
import uk.gov.hmcts.sptribs.common.event.page.SubjectDetails;
import uk.gov.hmcts.sptribs.common.notification.ApplicationReceivedNotification;
import uk.gov.hmcts.sptribs.common.service.SubmissionService;
import uk.gov.hmcts.sptribs.launchdarkly.FeatureToggleService;

import java.util.ArrayList;

import static java.lang.String.format;
import static java.lang.System.getenv;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Draft;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.DISTRICT_JUDGE_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.updateCategoryToCaseworkerDocument;

@Slf4j
@Component
public class CreateTestCase implements CCDConfig<CaseData, State, UserRole> {

    private static final String ENVIRONMENT_PROD = "prod";
    private static final String TEST_CREATE = "caseworker-create-case";
    private final FeatureToggleService featureToggleService;

    private static final CcdPageConfiguration categorisationDetails = new CaseCategorisationDetails();
    private static final CcdPageConfiguration dateOfReceipt = new DateOfReceipt();
    private static final CcdPageConfiguration selectParties = new SelectParties();
    private static final CcdPageConfiguration caseUploadDocuments = new CaseUploadDocuments();
    private static final CcdPageConfiguration subjectDetails = new SubjectDetails();
    private static final CcdPageConfiguration applicantDetails = new ApplicantDetails();
    private static final CcdPageConfiguration representativeDetails = new RepresentativeDetails();
    private static final CcdPageConfiguration furtherDetails = new FurtherDetails();
    private static final CcdPageConfiguration contactPreferenceDetails = new ContactPreferenceDetails();

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private ApplicationReceivedNotification applicationReceivedNotification;

    public CreateTestCase(FeatureToggleService featureToggleService) {
        this.featureToggleService = featureToggleService;
    }


    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        var roles = new ArrayList<UserRole>();
        final String env = getenv().getOrDefault("S2S_URL_BASE", "aat");
        roles.add(SOLICITOR);
        roles.add(COURT_ADMIN_CIC);
        roles.add(ST_CIC_CASEWORKER);
        roles.add(ST_CIC_SENIOR_CASEWORKER);
        roles.add(ST_CIC_HEARING_CENTRE_ADMIN);
        roles.add(ST_CIC_HEARING_CENTRE_TEAM_LEADER);
        roles.add(ST_CIC_SENIOR_JUDGE);
        if (!env.contains(ENVIRONMENT_PROD)) {
            roles.add(SUPER_USER);
            roles.add(COURT_ADMIN_CIC);
            roles.add(DISTRICT_JUDGE_CIC);
        }

        if (featureToggleService.isCicCreateCaseFeatureEnabled()) {
            PageBuilder pageBuilder = new PageBuilder(configBuilder
                .event(TEST_CREATE)
                .initialState(Draft)
                .name("Create Case")
                .showSummary()
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::submitted)
                .grant(CREATE_READ_UPDATE,
                    ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                    ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE));

            categorisationDetails.addTo(pageBuilder);
            dateOfReceipt.addTo(pageBuilder);
            selectParties.addTo(pageBuilder);
            subjectDetails.addTo(pageBuilder);
            applicantDetails.addTo(pageBuilder);
            representativeDetails.addTo(pageBuilder);
            contactPreferenceDetails.addTo(pageBuilder);
            caseUploadDocuments.addTo(pageBuilder);
            furtherDetails.addTo(pageBuilder);
        }
    }


    @SneakyThrows
    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        var submittedDetails = submissionService.submitApplication(details);
        CaseData data = submittedDetails.getData();

        updateCategoryToCaseworkerDocument(data.getCicCase().getApplicantDocumentsUploaded());
        setIsRepresentativePresent(data);
        data.setSecurityClass(SecurityClass.PUBLIC);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(submittedDetails.getState())
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        var data = details.getData();
        String claimNumber = data.getHyphenatedCaseRef();

        sendApplicationReceivedNotification(claimNumber, data);

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Case Created %n## Case reference number: %n## %s", claimNumber))
            .build();
    }

    private void sendApplicationReceivedNotification(String caseNumber, CaseData data) {
        CicCase cicCase = data.getCicCase();

        if (!cicCase.getSubjectCIC().isEmpty()) {
            applicationReceivedNotification.sendToSubject(data, caseNumber);
        }

        if (!cicCase.getApplicantCIC().isEmpty()) {
            applicationReceivedNotification.sendToApplicant(data, caseNumber);
        }

        if (!cicCase.getRepresentativeCIC().isEmpty()) {
            applicationReceivedNotification.sendToRepresentative(data, caseNumber);
        }
    }

    private void setIsRepresentativePresent(CaseData data) {
        if (null != data.getCicCase().getRepresentativeFullName()) {
            data.getCicCase().setIsRepresentativePresent(YesOrNo.YES);
        } else {
            data.getCicCase().setIsRepresentativePresent(YesOrNo.NO);
        }
    }


}
