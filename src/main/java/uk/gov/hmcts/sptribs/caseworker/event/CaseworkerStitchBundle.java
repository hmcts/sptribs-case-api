package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.STITCH_BUNDLE;
import static uk.gov.hmcts.sptribs.ciccase.model.State.BUNDLE_STATES;
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
public class CaseworkerStitchBundle implements CCDConfig<CaseData, State, UserRole> {

    @Value("${feature.bundling.enabled}")
    private boolean bundlingEnabled;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        if (bundlingEnabled) {
            doConfigure(configBuilder);
        }
    }

    private void doConfigure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(STITCH_BUNDLE)
            .forStates(BUNDLE_STATES)
            .name("Bundle: Stitch a bundle")
            .description("Bundle: Stitch a bundle")
            .showSummary()
            .aboutToSubmitCallback(this::aboutToSubmit)
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
                CIC_JUDGE))
            .page("stitchBundle")
            .pageLabel("Stitch a bundle")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Caseworker create bundle callback invoked for Case Id: {}", details.getId());

        var caseData = details.getData();


        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
