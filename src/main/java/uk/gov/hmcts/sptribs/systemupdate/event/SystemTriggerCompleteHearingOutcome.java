package uk.gov.hmcts.sptribs.systemupdate.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.CriminalInjuriesCompensationData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import static uk.gov.hmcts.sptribs.caseworker.model.YesNo.YES;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
public class SystemTriggerCompleteHearingOutcome implements CCDConfig<CriminalInjuriesCompensationData, State, UserRole> {

    public static final String SYSTEM_TRIGGER_COMPLETE_HEARING_OUTCOME = "system-trigger-complete-hearing-outcome";

    @Override
    public void configure(ConfigBuilder<CriminalInjuriesCompensationData, State, UserRole> configBuilder) {
        Event.EventBuilder<CriminalInjuriesCompensationData, UserRole, State> eventBuilder = configBuilder
            .event(SYSTEM_TRIGGER_COMPLETE_HEARING_OUTCOME)
            .forState(AwaitingHearing)
            .name("Trigger hearing outcome")
            .description("Trigger hearing outcome")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE_DELETE, SYSTEM_UPDATE)
            .publishToCamunda()
            .grant(CREATE_READ_UPDATE, ST_CIC_WA_CONFIG_USER);
    }

    public AboutToStartOrSubmitResponse<CriminalInjuriesCompensationData, State> aboutToSubmit(CaseDetails<CriminalInjuriesCompensationData, State> caseDetails,
                                                                       CaseDetails<CriminalInjuriesCompensationData, State> beforeDetails) {
        final CriminalInjuriesCompensationData caseData = caseDetails.getData();
        caseData.setCompleteHearingOutcomeTask(YES);

        return AboutToStartOrSubmitResponse.<CriminalInjuriesCompensationData, State>builder()
                .data(caseData)
                .build();
    }
}
