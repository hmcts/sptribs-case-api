package uk.gov.hmcts.sptribs.systemupdate.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.YesNo;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
public class SystemTriggerCompleteHearingOutcome implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_TRIGGER_COMPLETE_HEARING_OUTCOME = "system-trigger-complete-hearing-outcome";

    @Value("${feature.wa.enabled}")
    private boolean isWorkAllocationEnabled;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder = configBuilder
            .event(SYSTEM_TRIGGER_COMPLETE_HEARING_OUTCOME)
            .forState(AwaitingHearing)
            .name("Trigger hearing outcome")
            .description("Trigger hearing outcome")
            .grant(CREATE_READ_UPDATE_DELETE, SYSTEM_UPDATE);

        if (isWorkAllocationEnabled) {
            eventBuilder.publishToCamunda()
                    .grant(CREATE_READ_UPDATE, ST_CIC_WA_CONFIG_USER);
        }
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> caseDetails,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = caseDetails.getData();
        caseData.setCompleteHearingOutcomeTask(YesNo.YES);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .build();
    }
}
