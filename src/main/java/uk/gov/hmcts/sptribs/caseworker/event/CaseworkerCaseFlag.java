package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.service.CcdSupplementaryDataService;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CASE_FLAG;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.ReadyToList;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.AC_CASE_FLAGS_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@Setter
public class CaseworkerCaseFlag implements CCDConfig<CaseData, State, UserRole> {


    private static final String ALWAYS_HIDE = "flagLauncher = \"ALWAYS_HIDE\"";

    @Value("${feature.case-flags.enabled}")
    private boolean caseFlagsEnabled;

    @Autowired
    private CcdSupplementaryDataService coreCaseApiService;


    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        if (caseFlagsEnabled) {
            doConfigure(configBuilder);
        }
    }

    public void doConfigure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_CASE_FLAG)
            .forStates(Submitted, CaseManagement, AwaitingHearing, AwaitingOutcome, ReadyToList)
            .name("Create Flag")
            .showSummary()
            .description("Create Flag")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::flagCreated)
            .grant(CREATE_READ_UPDATE, AC_CASE_FLAGS_ADMIN, SUPER_USER,
                    ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                    ST_CIC_HEARING_CENTRE_TEAM_LEADER)
            .grantHistoryOnly(
                ST_CIC_CASEWORKER,
                ST_CIC_SENIOR_CASEWORKER,
                ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER,
                ST_CIC_SENIOR_JUDGE,
                SUPER_USER,
                ST_CIC_JUDGE))
            .page("caseworkerCaseFlag")
            .pageLabel("Case Flags")
            .optional(CaseData::getCaseFlags, ALWAYS_HIDE, true, true)
            .optional(CaseData::getSubjectFlags, ALWAYS_HIDE, true, true)
            .optional(CaseData::getApplicantFlags, ALWAYS_HIDE, true, true)
            .optional(CaseData::getRepresentativeFlags, ALWAYS_HIDE, true, true)
            .optional(CaseData::getFlagLauncher,
                null, null, null, null, "#ARGUMENT(CREATE)");
    }


    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Create Case flags about to Submit invoked for Case Id: {}", details.getId());

        final CaseData caseData = details.getData();
        coreCaseApiService.submitSupplementaryDataToCcd(details.getId().toString());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(details.getState())
            .build();
    }

    public SubmittedCallbackResponse flagCreated(CaseDetails<CaseData, State> details,
                                                 CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
                .confirmationHeader(format("# Flag created %n## This Flag has been added to case"))
                .build();

    }


}
