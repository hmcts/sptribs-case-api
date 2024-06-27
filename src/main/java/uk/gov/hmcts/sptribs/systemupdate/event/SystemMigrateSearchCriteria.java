package uk.gov.hmcts.sptribs.systemupdate.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class SystemMigrateSearchCriteria implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_MIGRATE_SEARCH_CRITERIA = "system-trigger-complete-hearing-outcome";

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(SYSTEM_MIGRATE_SEARCH_CRITERIA)
            .forAllStates()
            .name("System Migrate Search Criteria")
            .description("System Migrate Search Criteria")
            .grant(CREATE_READ_UPDATE_DELETE, SYSTEM_UPDATE);
    }
}
