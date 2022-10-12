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
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.event.page.ApplicantDetails;
import uk.gov.hmcts.sptribs.common.event.page.ContactPreferenceDetails;
import uk.gov.hmcts.sptribs.common.event.page.FurtherDetails;
import uk.gov.hmcts.sptribs.common.event.page.RepresentativeDetails;
import uk.gov.hmcts.sptribs.common.event.page.SelectParties;
import uk.gov.hmcts.sptribs.common.event.page.SubjectDetails;
import uk.gov.hmcts.sptribs.common.service.SubmissionService;
import uk.gov.hmcts.sptribs.launchdarkly.FeatureToggleService;

import java.util.ArrayList;

import static java.lang.String.format;
import static java.lang.System.getenv;
import static java.util.Collections.singletonList;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Draft;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CITIZEN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class CreateTestCase implements CCDConfig<CaseData, State, UserRole> {
    private static final String ENVIRONMENT_AAT = "aat";
    private static final String TEST_CREATE = "create-test-application";
    private final FeatureToggleService featureToggleService;

    private static final CcdPageConfiguration selectParties = new SelectParties();
    private static final CcdPageConfiguration applicantDetails = new ApplicantDetails();
    private static final CcdPageConfiguration subjectDetails = new SubjectDetails();
    private static final CcdPageConfiguration representativeDetails = new RepresentativeDetails();
    private static final CcdPageConfiguration furtherDetails = new FurtherDetails();
    private static final CcdPageConfiguration contactPreferenceDetails = new ContactPreferenceDetails();

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
                .grantHistoryOnly(SUPER_USER, COURT_ADMIN_CIC, SOLICITOR, CITIZEN_CIC));

            caseCategory(pageBuilder);
            buildSelectParty(pageBuilder);
            subjectDetails.addTo(pageBuilder);
            applicantDetails.addTo(pageBuilder);
            representativeDetails.addTo(pageBuilder);
            buildSelectPartys(pageBuilder);


            uploadDocuments(pageBuilder);
            furtherDetails.addTo(pageBuilder);
        }
    }

    private void buildSelectParty(PageBuilder pageBuilder) {
        selectParties.addTo(pageBuilder);
    }

    private void buildSelectPartys(PageBuilder pageBuilder) {
        contactPreferenceDetails.addTo(pageBuilder);
    }


    private void caseCategory(PageBuilder pageBuilder) {
        pageBuilder
            .page("caseCategoryObjects", this::midEvent)
            .label("caseCategoryObject", "CIC  Case Categorisation \r\n" + "\r\nCase Record for [DRAFT]")
            .complex(CaseData::getCicCase)
            .mandatoryWithLabel(CicCase::getCaseCategory, "")
            .mandatoryWithLabel(CicCase::getCaseSubcategory, "CIC Case Subcategory")
            .done()
            .page("dateObjects")
            .label("dateObject", "when was the case Received?\r\n" + "\r\nCase Record for [DRAFT]\r\n" + "\r\nDate of receipt")
            .complex(CaseData::getCicCase)
            .mandatoryWithLabel(CicCase::getCaseReceivedDate, "")
            .done();
    }


    private void uploadDocuments(PageBuilder pageBuilder) {
        pageBuilder.page("documentsUploadObjets")
            .label("upload", "<h1>Upload tribunal forms</h1>")
            .complex(CaseData::getCicCase)
            .label("documentUploadObjectLabel",
                 "\nPlease upload a copy of the completed tribunal form, as well as any"
                + " supporting documents or other information that has been supplied.\n"
                + "\n<h3>Files should be:</h3>\n"
                + "\n- uploaded separately, and not in one large file\n" + "\n- a maximum of 100MB in size (large files must be split)\n"
                + "\n- labelled clearly, e.g. applicant-name-B1-form.pdf\n\n")
            .optionalWithLabel(CicCase::getCaseDocumentsCIC, "File Attachments")
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

        setIsRepresentativePresent(data);

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

    private void setIsRepresentativePresent(CaseData data) {
        if (null != data.getCicCase().getRepresentativeFullName()) {
            data.getCicCase().setIsRepresentativePresent(YesOrNo.YES);
        } else {
            data.getCicCase().setIsRepresentativePresent(YesOrNo.NO);
        }
    }


}
