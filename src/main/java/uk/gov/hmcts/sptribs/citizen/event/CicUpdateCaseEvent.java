package uk.gov.hmcts.sptribs.citizen.event;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdCaseType;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.util.AppsUtil;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CITIZEN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CREATOR;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
@Setter
public class CicUpdateCaseEvent implements CCDConfig<CaseData, State, UserRole> {

    @Value("${feature.dss-frontend.enabled}")
    private boolean dssUpdateCaseEnabled;

    @Autowired
    AppsConfig appsConfig;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        if (dssUpdateCaseEnabled) {
            doConfigure(configBuilder);
        }
    }

    private void doConfigure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(AppsUtil.getExactAppsDetailsByCaseType(appsConfig, CcdCaseType.CIC.getCaseTypeName()).getEventIds()
                .getUpdateEvent())
            .forStates(State.Draft, State.Submitted, State.DSS_Draft)
            .name("Update case (cic)")
            .description("Application update (cic)")
            .retries(120, 120)
            .grant(CREATE_READ_UPDATE_DELETE, CITIZEN_CIC, CREATOR)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        final CaseData data = details.getData();

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }

}
