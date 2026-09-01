package uk.gov.hmcts.sptribs.caseworker.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.SecurityClass;
import uk.gov.hmcts.sptribs.caseworker.service.ExtendedCaseDataService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.CriminalInjuriesCompensationData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.idam.CICUser;
import uk.gov.hmcts.sptribs.idam.IdamService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CHANGE_SECURITY_CLASS;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@Setter
public class CaseworkerChangeSecurityClassification implements CCDConfig<CriminalInjuriesCompensationData, State, UserRole> {

    @Value("${feature.security-classification.enabled}")
    private boolean securityClassificationEnabled;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private IdamService idamService;

    @Autowired
    private ExtendedCaseDataService caseDataService;


    @Override
    public void configure(final ConfigBuilder<CriminalInjuriesCompensationData, State, UserRole> configBuilder) {
        if (securityClassificationEnabled) {
            doConfigure(configBuilder);
        }
    }

    private void doConfigure(final ConfigBuilder<CriminalInjuriesCompensationData, State, UserRole> configBuilder) {
        new PageBuilder<>(configBuilder
            .event(CHANGE_SECURITY_CLASS)
            .forAllStates()
            .name("Case: Security classification")
            .description("Case: Security classification")
            .showSummary()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE,
                ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE)
            .grantHistoryOnly(
                ST_CIC_CASEWORKER,
                ST_CIC_SENIOR_CASEWORKER,
                ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER,
                ST_CIC_SENIOR_JUDGE,
                SUPER_USER,
                ST_CIC_JUDGE))
            .page("changeSecurityClass", this::midEvent)
            .pageLabel("Security classification selection")
            .mandatory(CaseData::getSecurityClass);
    }

    public AboutToStartOrSubmitResponse<CriminalInjuriesCompensationData, State> aboutToSubmit(final CaseDetails<CriminalInjuriesCompensationData, State> details,
                                                                       final CaseDetails<CriminalInjuriesCompensationData, State> beforeDetails) {

        final CriminalInjuriesCompensationData caseData = details.getData();

        String securityClassification = caseData.getSecurityClass().getLabel();
        Map<String, Object> dataClassification = caseDataService.getDataClassification(details.getId().toString());

        return AboutToStartOrSubmitResponse.<CriminalInjuriesCompensationData, State>builder()
            .data(caseData)
            .dataClassification(dataClassification)
            .securityClassification(securityClassification)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CriminalInjuriesCompensationData, State> details,
                                               CaseDetails<CriminalInjuriesCompensationData, State> beforeDetails) {

        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Security classification changed")
            .build();
    }

    public AboutToStartOrSubmitResponse<CriminalInjuriesCompensationData, State> midEvent(CaseDetails<CriminalInjuriesCompensationData, State> details,
                                                                  CaseDetails<CriminalInjuriesCompensationData, State> detailsBefore) {

        final List<String> errors = new ArrayList<>();
        final CriminalInjuriesCompensationData caseData = details.getData();

        final CICUser user = idamService.retrieveUser(request.getHeader(AUTHORIZATION));
        if (!checkAvailableForNewClass(user, caseData.getSecurityClass())) {
            errors.add("You do not have permission to change the case to the selected Security Classification");
        }

        return AboutToStartOrSubmitResponse.<CriminalInjuriesCompensationData, State>builder()
            .data(caseData)
            .errors(errors)
            .build();
    }

    private boolean checkAvailableForNewClass(CICUser user, SecurityClass newClass) {
        List<String> roles = user.getUserInfo().getRoles();
        return Arrays.stream(newClass.getPermittedRoles()).anyMatch(roles::contains);
    }
}
