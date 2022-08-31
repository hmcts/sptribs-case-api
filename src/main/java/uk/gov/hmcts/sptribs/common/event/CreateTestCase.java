package uk.gov.hmcts.sptribs.common.event;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.service.SubmissionService;
import uk.gov.hmcts.sptribs.launchdarkly.FeatureToggleService;

import java.util.ArrayList;

import static java.lang.String.format;
import static java.lang.System.getenv;
import static java.util.Collections.singletonList;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Draft;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class CreateTestCase implements CCDConfig<CaseData, State, UserRole> {
    private static final String ENVIRONMENT_AAT = "aat";
    private static final String TEST_CREATE = "create-test-application";
    private static final String CASE_RECORD_DRAFT = "\r\nCase record for [DRAFT]";
    private final FeatureToggleService featureToggleService;

    @Autowired
    private SubmissionService submissionService;

    public CreateTestCase(FeatureToggleService featureToggleService) {
        this.featureToggleService = featureToggleService;
    }


    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        var roles = new ArrayList<UserRole>();
        var env = getenv().getOrDefault("S2S_URL_BASE", "aat");

        if (env.contains(ENVIRONMENT_AAT)) {
            roles.add(SOLICITOR);
        }

        if (featureToggleService.isCicCreateCaseFeatureEnabled()) {
            PageBuilder pageBuilder = new PageBuilder(configBuilder
                .event(TEST_CREATE)
                .initialState(Draft)
                .name("Create Case")
                .showSummary()
                .grant(CREATE_READ_UPDATE, roles.toArray(UserRole[]::new))
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::submitted)
                .grantHistoryOnly(SUPER_USER, CASE_WORKER, LEGAL_ADVISOR, SOLICITOR, CITIZEN));

            caseCategory(pageBuilder);
            selectParties(pageBuilder);
            subjectCategory(pageBuilder);

            pageBuilder.page("applicantDetailsObjects")
                .label("applicantDetailsObject", "Who is the applicant in this case?\r\n" + CASE_RECORD_DRAFT)
                .complex(CaseData::getCicCase)
                .mandatory(CicCase::getApplicantFullName)
                .mandatory(CicCase::getApplicantPhoneNumber)
                .mandatory(CicCase::getApplicantEmailAddress)
                .optionalWithLabel(CicCase::getApplicantDateOfBirth, "")
                .mandatoryWithLabel(CicCase::getApplicantContactDetailsPreference, "")
                .mandatory(CicCase::getApplicantAddress, "cicCaseApplicantContactDetailsPreference = \"Post\"")
                .done();
            pageBuilder.page("representativeDetailsObjects")
                .label("representativeDetailsObject", "Who is the Representative of this case?(If Any)\r\n" + CASE_RECORD_DRAFT)
                .complex(CaseData::getCicCase)
                .mandatory(CicCase::getRepresentativeFullName)
                .optional(CicCase::getRepresentativeOrgName)
                .mandatory(CicCase::getRepresentativePhoneNumber)
                .optional(CicCase::getRepresentativeReference)
                .mandatoryWithLabel(CicCase::getIsRepresentativeQualified, "")
                .mandatory(CicCase::getRepresentativeContactDetailsPreference)
                .mandatory(CicCase::getRepresentativeEmailAddress, "cicCaseRepresentativeContactDetailsPreference = \"Email\"")
                .mandatory(CicCase::getRepresentativeAddress, "cicCaseRepresentativeContactDetailsPreference = \"Post\"")
                .done();
            pageBuilder.page("objectContacts")
                .label("objectContact", "Who should receive information about the case?")
                .complex(CaseData::getCicCase)
                .optional(CicCase::getContactDetailsPreference)
                .done();
            uploadDocuments(pageBuilder);
            furtherDetails(pageBuilder);
        }
    }

    private void subjectCategory(PageBuilder pageBuilder) {
        pageBuilder.page("subjectDetailsObjects")
            .label("subjectDetailsObject", "Who is the subject of this case?\r\n" + CASE_RECORD_DRAFT)
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getFullName)
            .optional(CicCase::getAddress)
            .optional(CicCase::getPhoneNumber)
            .mandatoryWithLabel(CicCase::getDateOfBirth, "")
            .mandatoryWithLabel(CicCase::getContactPreferenceType, "")
            .mandatory(CicCase::getEmail, "cicCaseContactPreferenceType = \"Email\"")
            .done();
    }

    private void selectParties(PageBuilder pageBuilder) {
        pageBuilder
            .page("objectSubjects")
            .label("subjectObject", "Which parties are named on the tribunal form?\r\n" + "\r\nCase record for [DRAFT]")
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getSubjectCIC)
            .optional(CicCase::getApplicantCIC)
            .optional(CicCase::getRepresentativeCIC)
            .done();
    }

    private void caseCategory(PageBuilder pageBuilder) {
        pageBuilder
            .page("caseCategoryObjects", this::midEvent)
            .label("caseCategoryObject", "CIC  Case Categorisation \r\n" + "\r\nCase Record for [DRAFT]")
            .complex(CaseData::getCicCase)
            .mandatoryWithLabel(CicCase::getCaseCategory, "")
            .mandatoryWithLabel(CicCase::getCaseSubcategory, "CIC Case Subcategory")
            .optionalWithLabel(CicCase::getComment, "Comments")
            .done()
            .page("dateObjects")
            .label("dateObject", "when was the case Received?\r\n" + "\r\nCase Record for [DRAFT]\r\n" + "\r\nDate of receipt")
            .complex(CaseData::getCicCase)
            .mandatoryWithLabel(CicCase::getCaseReceivedDate, "")
            .done();
    }


    private void uploadDocuments(PageBuilder pageBuilder) {
        pageBuilder.page("documentsUploadObjets")
            .label("upload", "<h1>Upload Tribunal Forms</h1>")
            .complex(CaseData::getCicCase)
            .label("documentUploadObjectLabel", "Case record for [DRAFT]\n"
                + "\nPlease upload a copy of the completed tribunal form,as well as any\n"
                + "\nsupporting document or other information that has been supplied.\n"
                + "\n<h3>Files should be:</h3>\n"
                + "\n.Uploading seperatly and not in one large file\n" + "\n.a maximum of 1000MB in size (large files must be split)\n"
                + "\n.labelled clearly, e.g. applicant-name-B1-for.pdf\n" + "<h3>Already uploaded files:</h3>\n" + "\n-None\n")
            .label("documentsUploadObjets2", "Add a file\n" + "\nUpload a file to the system")
            .optional(CicCase::getCaseDocumentsCIC)
            .done();
    }

    private void furtherDetails(PageBuilder pageBuilder) {
        pageBuilder.page("objectFurtherDetails")
            .label("objectAdditionalDetails", "<h2>Enter further details about this case</h2>")
            .complex(CaseData::getCicCase)
            .optional(CicCase::getSchemeCic)
            .optional(CicCase::getClaimLinkedToCic)
            .optional(CicCase::getCicaReferenceNumber, "cicCaseClaimLinkedToCic = \"Yes\"")
            .optional(CicCase::getCompensationClaimLinkCIC)
            .optional(CicCase::getPoliceAuthority)
            .optional(CicCase::getFormReceivedInTime)
            .optional(CicCase::getMissedTheDeadLineCic)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {

        final CaseData data = details.getData();
        try {

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(data)
                .build();
        } catch (IllegalArgumentException e) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(singletonList("User ID entered for applicant 2 is an invalid UUID"))
                .build();
        }
    }

    @SneakyThrows
    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        CaseData data = details.getData();
        State state = details.getState();

        var submittedDetails = submissionService.submitApplication(details);
        data = submittedDetails.getData();
        state = submittedDetails.getState();

        //data.getCicCase().setIsRepresentativePresent(YesOrNo.YES);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(state)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        var data = details.getData();

        String claimNumber = data.getHyphenatedCaseRef();

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Case Created %n## Case reference number: %n## %s", claimNumber))
            .build();
    }


}
