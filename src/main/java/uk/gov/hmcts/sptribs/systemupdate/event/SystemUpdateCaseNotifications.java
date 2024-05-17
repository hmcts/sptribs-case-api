package uk.gov.hmcts.sptribs.systemupdate.event;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

public class SystemUpdateCaseNotifications implements CCDConfig<CaseData, State, UserRole> {
    public static final String SYSTEM_UPDATE_CASE_NOTIFICATIONS = "system-update-case-notifications";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(SYSTEM_UPDATE_CASE_NOTIFICATIONS)
            .forAllStates()
            .name("Update case notifications")
            .description("Update case notifications")
            .grant(CREATE_READ_UPDATE_DELETE, SYSTEM_UPDATE);
    }
}
