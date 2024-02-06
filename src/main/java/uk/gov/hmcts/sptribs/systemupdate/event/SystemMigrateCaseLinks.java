package uk.gov.hmcts.sptribs.systemupdate.event;

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

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
@Setter
public class SystemMigrateCaseLinks implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_MIGRATE_CASE_LINKS = "system-migrate-case-links";

    @Value("${feature.migration.enabled}")
    private boolean caseLinksMigrationEnabled;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        if (caseLinksMigrationEnabled) {
            configBuilder
                .event(SYSTEM_MIGRATE_CASE_LINKS)
                .forAllStates()
                .aboutToSubmitCallback(this::aboutToSubmit)
                .name("Migrate case links")
                .description("Migrate case links for old cases")
                .grant(CREATE_READ_UPDATE_DELETE, SYSTEM_UPDATE);
        }
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        log.info("Migrating case links for case Id: {}", details.getId());

        final CaseData data = details.getData();
        data.setCaseNameHmctsInternal(data.getCicCase().getFullName());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }
}
