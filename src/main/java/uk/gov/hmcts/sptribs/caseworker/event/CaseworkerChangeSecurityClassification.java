package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.caseworker.model.SecurityClass;
import uk.gov.hmcts.sptribs.caseworker.service.ExtendedCaseDataService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.idam.IdamService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CHANGE_SECURITY_CLASS;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CaseworkerChangeSecurityClassification implements CCDConfig<CaseData, State, UserRole> {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private IdamService idamService;

    @Autowired
    private ExtendedCaseDataService caseDataService;


    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(configBuilder
            .event(CHANGE_SECURITY_CLASS)
            .forAllStates()
            .name("Case: Security classification")
            .description("Case: Security classification")
            .showSummary()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE,
                ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE));
        changeSecurityClass(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        var caseData = details.getData();
        String securityClassification = caseData.getSecurityClass().getLabel();
        Map<String, Object> dataClassification = caseDataService.getDataClassification(details.getId().toString());
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .dataClassification(dataClassification)
            .securityClassification(securityClassification)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Security classification changed")
            .build();
    }

    private void changeSecurityClass(PageBuilder pageBuilder) {
        pageBuilder.page("changeSecurityClass", this::midEvent)
            .pageLabel("Security classification selection")
            .mandatory(CaseData::getSecurityClass);

    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {

        final List<String> errors = new ArrayList<>();
        var caseData = details.getData();

        final User user = idamService.retrieveUser(request.getHeader(AUTHORIZATION));
        if (!checkAvailableForNewClass(user, caseData.getSecurityClass())) {
            errors.add("You do not have permission to change the case to the selected Security Classification");
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .errors(errors)
            .build();

    }

    private boolean checkAvailableForNewClass(User user, SecurityClass newClass) {
        List<String> roles = user.getUserDetails().getRoles();
        return Arrays.stream(newClass.getPermittedRoles()).anyMatch(role -> roles.contains(role));
    }
}
