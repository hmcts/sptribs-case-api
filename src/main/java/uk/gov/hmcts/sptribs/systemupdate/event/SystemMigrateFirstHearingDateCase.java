package uk.gov.hmcts.sptribs.systemupdate.event;

import lombok.extern.slf4j.Slf4j;
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
public class SystemMigrateFirstHearingDateCase implements CCDConfig<CaseData, State, UserRole> {
    public static final String SYSTEM_MIGRATE_FIRST_HEARING_DATE = "system-migrate-first-hearing-date";

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(SYSTEM_MIGRATE_FIRST_HEARING_DATE)
            .forAllStates()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .name("Fix first hearing date")
            .description("Clears stale firstHearingDate values")
            .grant(CREATE_READ_UPDATE_DELETE, SYSTEM_UPDATE);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        CaseDetails<CaseData, State> caseDetails, CaseDetails<CaseData, State> beforeDetails) {

        log.info("Migrating date for case Id: {}", caseDetails.getId());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseDetails.getData())
            .build();
    }
}
