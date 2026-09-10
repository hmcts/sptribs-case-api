package uk.gov.hmcts.sptribs.systemupdate.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.CriminalInjuriesCompensationData;

import static uk.gov.hmcts.sptribs.caseworker.model.YesNo.YES;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
public class SystemTriggerStitchCollateHearingBundle implements CCDConfig<CriminalInjuriesCompensationData, State, UserRole> {
    public static final String SYSTEM_TRIGGER_STITCH_COLLATE_HEARING_BUNDLE = "system-trigger-stitch-collate-hearing-bundle";

    @Override
    public void configure(final ConfigBuilder<CriminalInjuriesCompensationData, State, UserRole> configBuilder) {
        Event.EventBuilder<CriminalInjuriesCompensationData, UserRole, State> eventBuilder = configBuilder
            .event(SYSTEM_TRIGGER_STITCH_COLLATE_HEARING_BUNDLE)
            .forState(AwaitingHearing)
            .name("Trigger stitch hearing bundle")
            .description("Trigger stitch hearing bundle")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE_DELETE, SYSTEM_UPDATE)
            .publishToCamunda()
            .grant(CREATE_READ_UPDATE, ST_CIC_WA_CONFIG_USER);

    }

    public AboutToStartOrSubmitResponse<CriminalInjuriesCompensationData,
        State> aboutToSubmit(final CaseDetails<CriminalInjuriesCompensationData, State> details,
                                                                       final CaseDetails<CriminalInjuriesCompensationData,
                                                                           State> beforeDetails) {
        CriminalInjuriesCompensationData caseData = details.getData();
        caseData.setStitchHearingBundleTask(YES);

        return AboutToStartOrSubmitResponse.<CriminalInjuriesCompensationData, State>builder()
            .data(caseData)
            .build();
    }
}
