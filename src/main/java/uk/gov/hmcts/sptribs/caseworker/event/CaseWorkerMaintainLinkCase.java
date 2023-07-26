package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.LinkCaseSelectCase;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_MAINTAIN_LINK_CASE;
import static uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil.maintainCaseLinks;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CIC_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CIC_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CIC_SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@Setter
public class CaseWorkerMaintainLinkCase implements CCDConfig<CaseData, State, UserRole> {

    @Value("${feature.link-case.enabled}")
    private boolean linkCaseEnabled;

    private static final String SHOW = "caseLinkExists != \"YES\"";
    private static final CcdPageConfiguration linkCaseSelectCase = new LinkCaseSelectCase();

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        if (linkCaseEnabled) {
            doConfigure(configBuilder);
        }
    }

    private void doConfigure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(configBuilder
            .event(CASEWORKER_MAINTAIN_LINK_CASE)
            .forStates(Submitted, CaseManagement, AwaitingHearing, AwaitingOutcome)
            .name("Links: Maintain Link case")
            .showSummary()
            .description("Links: Maintain Link case")
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE, CIC_SUPER_USER,
                CIC_CASEWORKER, CIC_SENIOR_CASEWORKER, CIC_CENTRE_ADMIN,
                CIC_CENTRE_TEAM_LEADER)
            .grantHistoryOnly(
                CIC_CASEWORKER,
                CIC_SENIOR_CASEWORKER,
                CIC_CENTRE_ADMIN,
                CIC_CENTRE_TEAM_LEADER,
                CIC_SENIOR_JUDGE,
                CIC_SUPER_USER,
                CIC_JUDGE));
        addNoLinks(pageBuilder);
        addWarning(pageBuilder);
        linkCaseSelectCase.addTo(pageBuilder);
    }


    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        log.info("Caseworker link the case callback invoked for Case Id: {}", details.getId());
        var data = details.getData();
        if (CollectionUtils.isEmpty(data.getCaseLinks())) {
            data.setCaseLinkExists("No links on this case");
        } else {
            data.setCaseLinkExists("YES");
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(details.getState())
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Caseworker link the case callback invoked for Case Id: {}", details.getId());
        var data = details.getData();

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(details.getState())
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("Case Updated")
            .build();
    }

    private void addWarning(PageBuilder pageBuilder) {
        pageBuilder.page("caseworkerMaintainLinkBeforeYouStart")
            .pageLabel("Before you start")
            .pageShowConditions(maintainCaseLinks())
            .label("beforeYouStartLabelForMaintain",
                "If there are linked hearings for the case you need to un-link then you must unlink the hearing first");
    }

    private void addNoLinks(PageBuilder pageBuilder) {

        pageBuilder.page("caseworkerCaseLinkNoLinks", this::midEvent)
            .pageLabel("Warning")
            .pageShowConditions(maintainCaseLinks())
            .mandatory(CaseData::getCaseLinkExists);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {

        CaseData caseData = details.getData();
        final List<String> errors = new ArrayList<>();
        errors.add("You can not proceed, press cancel to exit");

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .errors(errors)
            .build();
    }


}
