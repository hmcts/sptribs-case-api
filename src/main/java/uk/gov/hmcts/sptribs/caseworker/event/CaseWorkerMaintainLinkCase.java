package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.MaintainLinkCaseSelectCase;
import uk.gov.hmcts.sptribs.caseworker.util.MessageUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.links.LinkService;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_MAINTAIN_LINK_CASE;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.NO_LINKS;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.YES;
import static uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil.maintainCaseLinks;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CASEWORKER_ADMIN_PROFILE;
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
public class CaseWorkerMaintainLinkCase implements CCDConfig<CaseData, State, UserRole> {

    @Value("${feature.link-case.enabled}")
    private boolean linkCaseEnabled;

    @Autowired
    private LinkService linkService;

    private static final String SHOW = "caseLinkExists != \"YES\"";
    private static final CcdPageConfiguration maintainLinkCaseSelectCase = new MaintainLinkCaseSelectCase();

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
            .name("Maintain Case Links")
            .showSummary()
            .description("Maintain Case Links")
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE, SUPER_USER,
                ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER,CASEWORKER_ADMIN_PROFILE)
            .grantHistoryOnly(
                ST_CIC_CASEWORKER,
                ST_CIC_SENIOR_CASEWORKER,
                ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER,
                ST_CIC_SENIOR_JUDGE,
                SUPER_USER,
                ST_CIC_JUDGE));
        addNoLinks(pageBuilder);
        addWarning(pageBuilder);
        maintainLinkCaseSelectCase.addTo(pageBuilder);
    }


    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        log.info("Caseworker link the case callback invoked for Case Id: {}", details.getId());
        var data = details.getData();
        if (CollectionUtils.isEmpty(data.getCaseLinks())) {
            data.setCaseLinkExists(NO_LINKS);
        } else {
            data.setCaseLinkExists(YES);
            data.getCicCase().setLinkDynamicList(linkService.prepareLinkList(data));
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
        data.setCaseLinks(linkService.removeLinks(data));

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(details.getState())
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(MessageUtil.generateSimpleMessage(
                "Link removed from case", ""))
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
