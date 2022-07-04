package uk.gov.hmcts.divorce.systemupdate.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.ciccase.model.CaseData;
import uk.gov.hmcts.divorce.ciccase.model.State;
import uk.gov.hmcts.divorce.ciccase.model.UserRole;

import static uk.gov.hmcts.divorce.ciccase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class SystemRemindApplicant2 implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_REMIND_APPLICANT2 = "system-remind-applicant2";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(SYSTEM_REMIND_APPLICANT2)
            .forState(AwaitingApplicant2Response)
            .name("Remind Applicant 2")
            .description("Remind Applicant 2 that Application has not been reviewed")
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        CaseData data = details.getData();

        //notificationDispatcher.send(applicationRemindApplicant2Notification, data, details.getId());

        data.getApplication().setApplicant2ReminderSent(YesOrNo.YES);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
