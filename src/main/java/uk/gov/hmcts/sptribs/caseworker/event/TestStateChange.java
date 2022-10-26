package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.time.LocalDate;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Withdrawn;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class TestStateChange implements CCDConfig<CaseData, State, UserRole> {
    public static final String TEST_CHANGE_STATE = "change-state";


    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(TEST_CHANGE_STATE)
            .forAllStates()
            .name("Test change state")
            .showSummary()
            .description("Test change state")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::closed)
            .showEventNotes()
            .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
            .grantHistoryOnly(SOLICITOR))
            .page("testChangeState")
            .label("testChangeState", "<H2>Test change state</H2>")
            .complex(CaseData::getCicCase)
            .mandatoryWithLabel(CicCase::getTestState, "")
            .optional(CicCase::getDays);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {

        var caseData = details.getData();
        State newState = State.valueOf(caseData.getCicCase().getTestState().getName());

        if (newState == CaseClosed || newState == Withdrawn) {
            caseData.setClosureDate(LocalDate.now().minusDays(Long.parseLong(caseData.getCicCase().getDays())));
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(newState)
            .build();
    }

    public SubmittedCallbackResponse closed(CaseDetails<CaseData, State> details,
                                            CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# State changed"))
            .build();
    }
}
