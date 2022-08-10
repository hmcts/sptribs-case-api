package uk.gov.hmcts.divorce.common.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.ciccase.model.CaseData;
import uk.gov.hmcts.divorce.ciccase.model.CicCase;
import uk.gov.hmcts.divorce.ciccase.model.State;
import uk.gov.hmcts.divorce.ciccase.model.UserRole;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.nio.charset.Charset;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;

import static java.lang.System.getenv;
import static java.util.Collections.singletonList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.ciccase.model.State.Draft;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class CreateTestCase implements CCDConfig<CaseData, State, UserRole> {
    private static final String ENVIRONMENT_AAT = "aat";
    private static final String TEST_CREATE = "create-test-application";
    private static final String ASSESSMENT  = "classpath:data/assessment.json";

    @Autowired
    private CcdAccessService ccdAccessService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        var roles = new ArrayList<UserRole>();
        var env = getenv().getOrDefault("S2S_URL_BASE", "aat");

        if (env.contains(ENVIRONMENT_AAT)) {
            roles.add(SOLICITOR);
        }

        new PageBuilder(configBuilder
            .event(TEST_CREATE)
            .initialState(Draft)
            .name("Create Case")
            .showSummary()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE, roles.toArray(UserRole[]::new))
            .grantHistoryOnly(SUPER_USER, CASE_WORKER, LEGAL_ADVISOR, SOLICITOR, CITIZEN))
            .page("All Structured Data Test Page", this::midEvent)


            .complex(CaseData::getCicCase)
               .label("caseObject", "CIC  Case Categorisation \r\n" + "\r\nCase Record for [DRAFT]")
              .mandatoryWithLabel(CicCase::getCaseCategory, "")
              .mandatoryWithLabel(CicCase::getCaseSubcategory, "CIC Case Subcategory")
            .optionalWithLabel(CicCase::getComment,"Comments")
            .done()
            .page("Date")
            .label("dateObject","when was the case Received?\r\n" + "\r\nCase Record for [DRAFT]\r\n" + "\r\nDate of receipt")
            .complex(CaseData::getCicCase)
               .mandatoryWithLabel(CicCase::getCaseReceivedDate, "")
            .done()
            .page("objectSubject")
            .label("subjectObject", "Which parties are named on the tribunal form?\r\n" + "\r\nCase record for [DRAFT]")
            .complex(CaseData::getCicCase)
            .mandatoryWithLabel(CicCase::getSubjectCIC,"Select all that apply.")
            .optional(CicCase::getApplicantCIC,"")
            .optional(CicCase::getRepresentativeCic,"")
            .done()
            .page("applicantDetailsObject")
            .label("applicantDetailsObject","Who is the subject of this case?\r\n" + "\r\nCase record for [DRAFT]")
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getFullName)
            .optional(CicCase::getAddress)
            .optional(CicCase::getPhoneNumber)
            .optional(CicCase::getEmail)
            .mandatoryWithLabel(CicCase::getDateOfBirth,"")
            .mandatoryWithLabel(CicCase::getContactDetailsPrefrence,"")
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

        var file = ASSESSMENT;
        var resourceLoader = new DefaultResourceLoader();
        var json = IOUtils.toString(resourceLoader.getResource(file).getInputStream(), Charset.defaultCharset());
        var fixture = objectMapper.readValue(json, CaseData.class);

        fixture.getApplicant1().setSolicitorRepresented(details.getData().getApplicant1().getSolicitorRepresented());
        fixture.getApplicant2().setSolicitorRepresented(details.getData().getApplicant2().getSolicitorRepresented());
        fixture.setCaseInvite(details.getData().getCaseInvite());
        fixture.setHyphenatedCaseRef(fixture.formatCaseRef(details.getId()));

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(fixture)
            .state(details.getData().getApplication().getStateToTransitionApplicationTo())
            .build();
    }


    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details, CaseDetails<CaseData, State> before) {
        var data = details.getData();
        var caseId = details.getId();
        var app2Id = data.getCaseInvite().applicant2UserId();
        var auth = httpServletRequest.getHeader(AUTHORIZATION);

        if (data.getApplicant1().isRepresented()) {
            var orgId = details
                .getData()
                .getApplicant1()
                .getSolicitor()
                .getOrganisationPolicy()
                .getOrganisation()
                .getOrganisationId();

            ccdAccessService.addApplicant1SolicitorRole(auth, caseId, orgId);
        }

        if (data.getCaseInvite().applicant2UserId() != null && data.getApplicant2().isRepresented()) {
            var orgId = details
                .getData()
                .getApplicant2()
                .getSolicitor()
                .getOrganisationPolicy()
                .getOrganisation()
                .getOrganisationId();

            ccdAccessService.addRoleToCase(app2Id, caseId, orgId, APPLICANT_1_SOLICITOR);
        } else if (data.getCaseInvite().applicant2UserId() != null) {
            ccdAccessService.linkRespondentToApplication(auth, caseId, app2Id);
        }

        return SubmittedCallbackResponse.builder().build();
    }
}
