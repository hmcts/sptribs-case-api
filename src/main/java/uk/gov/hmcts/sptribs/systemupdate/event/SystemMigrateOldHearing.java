package uk.gov.hmcts.sptribs.systemupdate.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.service.HearingService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class SystemMigrateOldHearing implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_MIGRATE_OLD_HEARING = "system-migrate-old-hearing";

    private final HearingService hearingService;

    @Autowired
    public SystemMigrateOldHearing(HearingService hearingService) {
        this.hearingService = hearingService;
    }

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(SYSTEM_MIGRATE_OLD_HEARING)
            .forAllStates()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .name("Migrate old hearing")
            .description("Migrate old hearing to new hearing list")
            .grant(CREATE_READ_UPDATE_DELETE, SYSTEM_UPDATE);

    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        log.info("Migrating old hearing for case Id: {}", details.getId());

        CaseData data = details.getData();
        hearingService.addListingIfExists(data);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }
}
