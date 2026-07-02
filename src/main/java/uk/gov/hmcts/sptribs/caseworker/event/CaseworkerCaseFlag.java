package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.FlagDetail;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.ApplyAnonymity;
import uk.gov.hmcts.sptribs.caseworker.util.CaseFlagsUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.service.CcdSupplementaryDataService;
import uk.gov.hmcts.sptribs.notification.dispatcher.AnonymityAppliedNotification;

import java.util.ArrayList;
import java.util.List;

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
@RequiredArgsConstructor
public class CaseworkerCaseFlag implements CCDConfig<CaseData, State, UserRole> {

    private static final String ALWAYS_HIDE = "flagLauncher = \"ALWAYS_HIDE\"";

    private final CcdSupplementaryDataService coreCaseApiService;
    private final ApplyAnonymity applyAnonymity;
    private final AnonymityAppliedNotification anonymityAppliedNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_CASE_FLAG)
            .forStates(Submitted, CaseManagement, AwaitingHearing, AwaitingOutcome, ReadyToList)
            .name("Create Flag")
            .description("Create Flag")
            .showSummary()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE, AC_CASE_FLAGS_ADMIN, SUPER_USER,
                ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER)
            .grantHistoryOnly(
                ST_CIC_SENIOR_JUDGE, ST_CIC_JUDGE))
            .page("caseworkerCaseFlag")
            .pageLabel("Case Flags")
            .optional(CaseData::getCaseFlags, ALWAYS_HIDE, true, true)
            .optional(CaseData::getSubjectFlags, ALWAYS_HIDE, true, true)
            .optional(CaseData::getApplicantFlags, ALWAYS_HIDE, true, true)
            .optional(CaseData::getRepresentativeFlags, ALWAYS_HIDE, true, true)
            .optional(CaseData::getFlagLauncher,
                null, null, null, null, "#ARGUMENT(CREATE)");
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                        CaseDetails<CaseData, State> beforeDetails) {
        CaseData caseData = details.getData() == null ? CaseData.builder().build() : details.getData();
        CaseData beforeData = beforeDetails == null ? null : beforeDetails.getData();
        CicCase cicCase = caseData.getCicCase();
        if (cicCase == null) {
            cicCase = new CicCase();
            caseData.setCicCase(cicCase);
        }

        List<String> errors = new ArrayList<>();
        ListValue<FlagDetail> effectiveAnonymityFlag = CaseFlagsUtil.mergeAnonymityFlagsPreserveOriginalId(caseData, beforeData);
        boolean hasActiveAnonymityFlag = CaseFlagsUtil.isActiveFlag(effectiveAnonymityFlag);
        cicCase.setAnonymiseYesOrNo(hasActiveAnonymityFlag ? YesOrNo.YES : YesOrNo.NO);
        applyAnonymity.applyAnonymitySelection(cicCase, errors);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .errors(errors)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {

        coreCaseApiService.submitSupplementaryDataToCcd(details.getId() != null ? details.getId().toString() : null);

        anonymityAppliedNotification.sendAnonymityNotificationIfNewlyApplied(
            details.getData(),
            beforeDetails == null ? null : beforeDetails.getData(),
            details.getId() != null ? details.getId().toString() : null
        );

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Flag created %n## This Flag has been added to case"))
            .build();
    }
}
