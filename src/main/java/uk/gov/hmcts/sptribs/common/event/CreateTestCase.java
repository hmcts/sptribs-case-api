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

            pageBuilder
                .page("caseCategoryObjects",this::midEvent)
                .label("caseCategoryObject", "CIC  Case Categorisation \r\n" + "\r\nCase Record for [DRAFT]")
                .complex(CaseData::getCicCase)
                .mandatoryWithLabel(CicCase::getCaseCategory, "")
                .mandatoryWithLabel(CicCase::getCaseSubcategory, "CIC Case Subcategory")
                .optionalWithLabel(CicCase::getComment,"Comments")
                .done()
                .page("dateObjects")
                .label("dateObject","when was the case Received?\r\n" + "\r\nCase Record for [DRAFT]\r\n" + "\r\nDate of receipt")
                .complex(CaseData::getCicCase)
                .mandatoryWithLabel(CicCase::getCaseReceivedDate, "")
                .done();

            pageBuilder
                .page("objectSubjects")
                .label("subjectObject", "Which parties are named on the tribunal form?\r\n" + "\r\nCase record for [DRAFT]")
                .complex(CaseData::getCicCase)
                .mandatory(CicCase::getSubjectCIC)
                .optional(CicCase::getRepresentativeCIC)
                .done();

            pageBuilder.page("applicantDetailsObjects")
                .label("applicantDetailsObject","Who is the subject of this case?\r\n" + "\r\nCase record for [DRAFT]")
                .complex(CaseData::getCicCase)
                .mandatory(CicCase::getFullName)
                .optional(CicCase::getAddress)
                .optional(CicCase::getPhoneNumber)
                .optional(CicCase::getEmail)
                .mandatoryWithLabel(CicCase::getDateOfBirth,"")
                .mandatoryWithLabel(CicCase::getContactDetailsPreference,"")
                .done();

            pageBuilder.page("representativeDetailsObjects")
                .label("representativeDetailsObject","Who is the Representative of this case?(If Any)\r\n" + "\r\nCase record for [DRAFT]")
                .complex(CaseData::getCicCase)
                .optional(CicCase::getRepresentativeCICDetails)
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

    @SneakyThrows
    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        CaseData data = details.getData();
        State state = details.getState();

        var submittedDetails = submissionService.submitApplication(details);
        data = submittedDetails.getData();
        state = submittedDetails.getState();

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
