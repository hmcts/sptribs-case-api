package uk.gov.hmcts.sptribs.systemupdate.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.caseworker.service.HearingJudgeTaskDateService;
import uk.gov.hmcts.sptribs.caseworker.service.HearingJudgeTaskService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import static uk.gov.hmcts.sptribs.caseworker.model.YesNo.YES;
import static uk.gov.hmcts.sptribs.ciccase.model.HearingState.Listed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
public class SystemTriggerAssignHearingJudge implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_TRIGGER_ASSIGN_HEARING_JUDGE = "system-trigger-assign-hearing-judge";

    private final HearingJudgeTaskService hearingJudgeTaskService;
    private final HearingJudgeTaskDateService dates;

    public SystemTriggerAssignHearingJudge(HearingJudgeTaskService hearingJudgeTaskService,
                                           HearingJudgeTaskDateService dates) {
        this.hearingJudgeTaskService = hearingJudgeTaskService;
        this.dates = dates;
    }

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.event(SYSTEM_TRIGGER_ASSIGN_HEARING_JUDGE)
            .forState(AwaitingHearing)
            .name("Trigger assign hearing judge")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE_DELETE, SYSTEM_UPDATE)
            .grant(CREATE_READ_UPDATE, ST_CIC_WA_CONFIG_USER)
            .publishToCamunda()
            .fields()
            .page("assignHearingJudgeTask")
            .readonlyNoSummary(CaseData::getAssignHearingJudgeTaskTrigger, "[STATE]=\"NEVER_SHOW\"");
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        CaseData caseData = details.getData();
        caseData.setAssignHearingJudgeTaskTrigger(null);
        if (caseData.getHearingList() != null) {
            for (ListValue<Listing> hearing : caseData.getHearingList()) {
                Listing listing = hearing.getValue();
                if (listing != null && listing.getHearingStatus() == Listed
                    && listing.getHearingJudgeTaskRequested() != YES
                    && dates.isDue(listing.getDate(), dates.releaseDate(listing.getDate()))) {
                    hearingJudgeTaskService.updateTask(caseData, listing);
                    break;
                }
            }
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder().data(caseData).build();
    }
}
