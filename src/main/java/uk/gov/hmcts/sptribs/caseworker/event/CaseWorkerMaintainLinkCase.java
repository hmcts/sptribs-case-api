package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_MAINTAIN_LINK_CASE;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@Setter
public class CaseWorkerMaintainLinkCase implements CCDConfig<CaseData, State, UserRole> {

    @Value("${feature.link-case.enabled}")
    private boolean linkCaseEnabled;

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
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE,
                ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER));

        linkCaseSelectCase.addTo(pageBuilder);
    }

    @SneakyThrows
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

}
