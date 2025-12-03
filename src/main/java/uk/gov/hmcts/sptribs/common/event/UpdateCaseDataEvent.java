package uk.gov.hmcts.sptribs.common.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_UPDATE_CASE_DATA;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Slf4j
@Component
public class UpdateCaseDataEvent implements CCDConfig<CaseData, State, UserRole> {
    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
            configBuilder
                .event(CASEWORKER_UPDATE_CASE_DATA)
                .forAllStates()
                .name("Update Case Data")
                .description("Update Case Data")
                .retries(120, 120)
                .grant(CREATE_READ_UPDATE_DELETE, SUPER_USER);
    }
}
