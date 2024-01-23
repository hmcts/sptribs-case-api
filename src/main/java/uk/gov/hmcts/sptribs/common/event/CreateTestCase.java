package uk.gov.hmcts.sptribs.common.event;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.Flags;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.SecurityClass;
import uk.gov.hmcts.sptribs.caseworker.util.DynamicListUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CaseLocation;
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
import uk.gov.hmcts.sptribs.common.service.CcdSupplementaryDataService;
import uk.gov.hmcts.sptribs.common.service.SubmissionService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static java.lang.System.getenv;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Draft;
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
    public static final String TEST_CREATE = "caseworker-create-case";

    public static final String GLASGOW_TRIBUNALS_CENTRE = "366559";
    public static final String REGION = "11";
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
    private CcdSupplementaryDataService ccdSupplementaryDataService;

    @Autowired
    private ApplicationReceivedNotification applicationReceivedNotification;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final List<UserRole> roles = new ArrayList<>();
        final String env = getenv().getOrDefault("S2S_URL_BASE", "aat");
        roles.add(SOLICITOR);
        roles.add(ST_CIC_CASEWORKER);
        roles.add(ST_CIC_SENIOR_CASEWORKER);
        roles.add(ST_CIC_HEARING_CENTRE_ADMIN);
        roles.add(ST_CIC_HEARING_CENTRE_TEAM_LEADER);
        roles.add(ST_CIC_SENIOR_JUDGE);
        if (!env.contains(ENVIRONMENT_PROD)) {
            roles.add(SUPER_USER);
            roles.add(DISTRICT_JUDGE_CIC);
        }

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


    @SneakyThrows
    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        final CaseDetails<CaseData, State> submittedDetails = submissionService.submitApplication(details);
        final CaseData data = submittedDetails.getData();

        updateCategoryToCaseworkerDocument(data.getCicCase().getApplicantDocumentsUploaded());
        setIsRepresentativePresent(data);
        data.setSecurityClass(SecurityClass.PUBLIC);

        data.setCaseNameHmctsInternal(data.getCicCase().getFullName());
        data.setCaseManagementCategory(getCategoryList(data));
        data.setCaseManagementLocation(CaseLocation.builder()
            .baseLocation(GLASGOW_TRIBUNALS_CENTRE).region(REGION)
            .build());

        initialiseFlags(data);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(submittedDetails.getState())
            .build();
    }

    private DynamicList getCategoryList(CaseData data) {
        List<String> categories = new ArrayList<>();
        categories.add("Assessment");
        categories.add("Eligibility");

        DynamicList categoryList = DynamicListUtil.createDynamicList(categories);
        DynamicListElement selectedCategory = DynamicListElement.builder()
            .label(data.getCicCase().getCaseCategory().getLabel())
            .code(UUID.randomUUID()).build();
        categoryList.setValue(selectedCategory);

        return categoryList;
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        final CaseData data = details.getData();

        setSupplementaryData(details.getId());

        String claimNumber = data.getHyphenatedCaseRef();
        try {
            sendApplicationReceivedNotification(claimNumber, data);
        } catch (Exception notificationException) {
            log.error("Create case notification failed with exception : {}", notificationException.getMessage());
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(format("# Create case notification failed %n## Please resend the notification"))
                .build();
        }

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Case Created %n## Case reference number: %n## %s", claimNumber))
            .build();
    }

    private void initialiseFlags(CaseData data) {
        data.setCaseFlags(Flags.builder()
            .details(new ArrayList<>())
            .partyName(null)
            .roleOnCase(null)
            .build());

        if (null != data.getCicCase().getFullName()) {
            data.setSubjectFlags(Flags.builder()
                .details(new ArrayList<>())
                .partyName(data.getCicCase().getFullName())
                .roleOnCase("subject")
                .build()
            );
        }

        if (null != data.getCicCase().getApplicantFullName()) {
            data.setApplicantFlags(Flags.builder()
                .details(new ArrayList<>())
                .partyName(data.getCicCase().getApplicantFullName())
                .roleOnCase("applicant")
                .build()
            );
        }

        if (null != data.getCicCase().getRepresentativeFullName()) {
            data.setRepresentativeFlags(Flags.builder()
                .details(new ArrayList<>())
                .partyName(data.getCicCase().getRepresentativeFullName())
                .roleOnCase("Representative")
                .build()
            );
        }
    }

    private void setSupplementaryData(Long caseId) {
        try {
            ccdSupplementaryDataService.submitSupplementaryDataToCcd(caseId.toString());
        } catch (Exception exception) {
            log.error("Unable to set Supplementary data with exception : {}", exception.getMessage());
        }
    }

    private void sendApplicationReceivedNotification(String caseNumber, CaseData data) {
        CicCase cicCase = data.getCicCase();

        if (CollectionUtils.isNotEmpty(cicCase.getSubjectCIC())) {
            applicationReceivedNotification.sendToSubject(data, caseNumber);
        }

        if (CollectionUtils.isNotEmpty(cicCase.getApplicantCIC())) {
            applicationReceivedNotification.sendToApplicant(data, caseNumber);
        }

        if (CollectionUtils.isNotEmpty(cicCase.getRepresentativeCIC())) {
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
