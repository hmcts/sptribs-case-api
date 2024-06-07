package uk.gov.hmcts.sptribs.systemupdate.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
public class SystemTriggerCompleteHearingOutcome implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_TRIGGER_COMPLETE_HEARING_OUTCOME = "system-trigger-complete-hearing-outcome";

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(SYSTEM_TRIGGER_COMPLETE_HEARING_OUTCOME)
            .forState(AwaitingHearing)
            .name("Trigger hearing outcome")
            .description("Trigger hearing outcome")
            .grant(CREATE_READ_UPDATE_DELETE, SYSTEM_UPDATE);
    }
}
