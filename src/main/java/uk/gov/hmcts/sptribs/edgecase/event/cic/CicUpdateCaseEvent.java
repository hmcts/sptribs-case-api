package uk.gov.hmcts.sptribs.edgecase.event.cic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdCaseType;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.util.AppsUtil;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CITIZEN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CREATOR;
import static uk.gov.hmcts.sptribs.edgecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CicUpdateCaseEvent implements CCDConfig<CaseData, State, UserRole>  {

    @Autowired
    AppsConfig appsConfig;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(AppsUtil.getExactAppsDetailsByCaseType(appsConfig, CcdCaseType.CIC.getCaseTypeName()).getEventIds()
                       .getUpdateEvent())
            .forStates(State.Draft, State.Submitted)
            .name("Edge case (cic)")
            .description("Application update (cic)")
            .retries(120, 120)
            .grant(CREATE_READ_UPDATE, CITIZEN_CIC)
            .grant(CREATE_READ_UPDATE, CREATOR);
    }

}
