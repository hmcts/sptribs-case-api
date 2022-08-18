package uk.gov.hmcts.sptribs.common.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.ApplicantCICDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCICDetails;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.launchdarkly.FeatureToggleService;

import java.util.ArrayList;

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


        PageBuilder pageBuilder = new PageBuilder(
            configBuilder
                .event(TEST_CREATE)
                .initialState(Draft)
                .name("Create Case")
                .showSummary()
                .grant(CREATE_READ_UPDATE, roles.toArray(UserRole[]::new))
                .grantHistoryOnly(SUPER_USER, CASE_WORKER, LEGAL_ADVISOR, SOLICITOR, CITIZEN));
        pageBuilder.page("caseCategoryObjects")
            .label("caseCategoryObject", "CIC  Case Categorisation \r\n" + "\r\nCase Record for [DRAFT]")
            .complex(CaseData::getCicCase)
            .mandatoryWithLabel(CicCase::getCaseCategory, "")
            .mandatoryWithLabel(CicCase::getCaseSubcategory, "CIC Case Subcategory")
            .optionalWithLabel(CicCase::getComment, "Comments")
            .done();
        pageBuilder.page("dateObjects")
            .label("dateObject", "when was the case Received?\r\n" + "\r\nCase Record for [DRAFT]\r\n" + "\r\nDate of receipt")
            .complex(CaseData::getCicCase)
            .mandatoryWithLabel(CicCase::getCaseReceivedDate, "")
            .done();
        pageBuilder.page("objectSubjects")
            .label("subjectObject", "Which parties are named on the tribunal form?\r\n" + CASE_RECORD_DRAFT)
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getSubjectCIC)
            .optional(CicCase::getApplicantCIC)
            .optional(CicCase::getRepresentativeCic)
            .done();
        pageBuilder.page("subjectDetailsObjects")
            .label("subjectDetailsObject", "Who is the subject of this case?\r\n" + CASE_RECORD_DRAFT)
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getFullName)
            .optional(CicCase::getAddress)
            .optional(CicCase::getPhoneNumber)
            .optional(CicCase::getEmail)
            .mandatoryWithLabel(CicCase::getDateOfBirth, "")
            .mandatoryWithLabel(CicCase::getContactDetailsPrefrence, "")
            .done();
        pageBuilder.page("applicantDetailsObjects")
            .label("applicantDetailsObject", "Who is the Applicant in this case?\r\n" + CASE_RECORD_DRAFT)
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getApplicantFullName)
            .optional(CicCase::getApplicantAddress)
            .done()
            .complex(CaseData::getCicCase)
            .complex(CicCase::getApplicantCICDetails)
            .mandatory(ApplicantCICDetails::getPhoneNumber)
            .mandatory(ApplicantCICDetails::getEmailAddress)
            .optionalWithLabel(ApplicantCICDetails::getDateOfBirth, "")
            .mandatoryWithLabel(ApplicantCICDetails::getContactDetailsPrefrence, "")
            .done()
            .done();
        pageBuilder.page("representativeDetailsObjects")
            .label("representativeDetailsObject", "Who is the Representative of this case?(If Any)\r\n" + CASE_RECORD_DRAFT)
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getRepresentativeFullName)
            .optional(CicCase::getRepresentativeAddress)
            .done()
            .complex(CaseData::getCicCase)
            .complex(CicCase::getRepresentativeCICDetails)
            .optional(RepresentativeCICDetails::getPhoneNumber)
            .optional(RepresentativeCICDetails::getEmail)
            .mandatoryWithLabel(RepresentativeCICDetails::getDateOfBirth, "")
            .mandatoryWithLabel(RepresentativeCICDetails::getContactDetailsPrefrence, "")
            .done()
            .done();
        pageBuilder.page("objectContacts")
            .label("objectContact", "Who should receive information about the case?")
            .complex(CaseData::getCicCase)
            .optional(CicCase::getContactPreferencesDetailsForApplicationCIC)
            .done();
        pageBuilder.page("documentsUploadObjets")
            .label("upload", "<h1>Upload Tribunal Forms</h1>")
            .complex(CaseData::getCicCase)
            .label("documentUploadObjectLabel", "Case record for [DRAFT]\n"
                + "\nPlease upload a copy of the completed tribunal form,as well as any\n"
                + "\nsupporting document or other information that has been supplied.\n"
                + "\n<h3>Files should be:</h3>\n"
                + "\n.Uploading seperatly and not in one large file\n"
                + "\n.a maximum of 1000MB in size (large files must be split)\n"
                + "\n.labelled clearly, e.g. applicant-name-B1-for.pdf\n"
                + "<h3>Already uploaded files:</h3>\n"
                + "\n-None\n")
            .label("documentsUploadObjets2", "Add a file\n" + "\nUpload a file to the system")
            .optional(CicCase::getCaseDocumentsCIC)
            .done();


        //TODO this is a toggled off feature part of POC. should be removed in the future.
        //This feature toggle disabled two CCD config which represents the pages below.
        if (featureToggleService.isTestFeatureEnabled()) {
            pageBuilder.page("applicantDetailsObjects")
                .label("applicantDetailsObject", "Who is the subject of this case?\r\n" + CASE_RECORD_DRAFT)
                .complex(CaseData::getCicCase)
                .mandatory(CicCase::getFullName)
                .optional(CicCase::getAddress)
                .optional(CicCase::getPhoneNumber)
                .optional(CicCase::getEmail)
                .mandatoryWithLabel(CicCase::getDateOfBirth, "")
                .mandatoryWithLabel(CicCase::getContactPreferencesDetailsForApplicationCIC, "")
                .done();

        }
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


}
