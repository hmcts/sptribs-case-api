package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.FlagDetail;
import uk.gov.hmcts.ccd.sdk.type.Flags;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.FlagAdditionalInfo;
import uk.gov.hmcts.sptribs.caseworker.event.page.FlagLevel;
import uk.gov.hmcts.sptribs.caseworker.event.page.FlagParties;
import uk.gov.hmcts.sptribs.caseworker.event.page.FlagTypePage;
import uk.gov.hmcts.sptribs.caseworker.util.MessageUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.model.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CASE_FLAG;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;
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

    @Value("${feature.case-flags.enabled}")
    private boolean caseFlagsEnabled;

    private static final CcdPageConfiguration flagLevel = new FlagLevel();
    private static final CcdPageConfiguration flagParties = new FlagParties();
    private static final CcdPageConfiguration flagType = new FlagTypePage();
    private static final CcdPageConfiguration flagAdditionalInfo = new FlagAdditionalInfo();

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        if (caseFlagsEnabled) {
            doConfigure(configBuilder);
        }
    }

    public void doConfigure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        var pageBuilder = caseFlag(configBuilder);
        flagLevel.addTo(pageBuilder);
        flagParties.addTo(pageBuilder);
        flagType.addTo(pageBuilder);
        flagAdditionalInfo.addTo(pageBuilder);
    }

    public PageBuilder caseFlag(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(CASEWORKER_CASE_FLAG)
            .forStates(Submitted, CaseManagement, AwaitingHearing, AwaitingOutcome)
            .name("Flags: Create flag")
            .showSummary()
            .description("Create a flag")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::flagCreated)
            .grant(CREATE_READ_UPDATE, SUPER_USER,
                ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER)
            .grantHistoryOnly(
                ST_CIC_CASEWORKER,
                ST_CIC_SENIOR_CASEWORKER,
                ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER,
                ST_CIC_SENIOR_JUDGE,
                SUPER_USER,
                ST_CIC_JUDGE));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Caseworker stay the case callback invoked for Case Id: {}", details.getId());

        var caseData = details.getData();
        var cicCase = caseData.getCicCase();

        var flagDetail = new FlagDetail();
        flagDetail.setName(cicCase.getFlagType().getLabel());
        flagDetail.setFlagCode(cicCase.getFlagType().getFlagCode());
        flagDetail.setFlagComment(cicCase.getFlagAdditionalDetail());
        flagDetail.setOtherDescription(cicCase.getFlagOtherDescription());
        flagDetail.setStatus(Status.ACTIVE.getLabel());
        var flag = new Flags();
        if (isEmpty(flag.getDetails())) {
            List<ListValue<FlagDetail>> listValues = new ArrayList<>();

            var listValue = ListValue
                .<FlagDetail>builder()
                .id("1")
                .value(flagDetail)
                .build();

            listValues.add(listValue);

            flag.setDetails(listValues);
        } else {
            AtomicInteger listValueIndex = new AtomicInteger(0);
            var listValue = ListValue
                .<FlagDetail>builder()
                .value(flagDetail)
                .build();

            flag.getDetails().add(0, listValue);
            flag.getDetails().forEach(flagsListValue -> flagsListValue.setId(String.valueOf(listValueIndex.incrementAndGet())));
        }
        if (cicCase.getFlagLevel().isPartyLevel()) {
            if (null != caseData.getCicCase().getNotifyPartyApplicant()
                && !isEmpty(caseData.getCicCase().getNotifyPartyApplicant())) {
                flag.setPartyName(caseData.getCicCase().getApplicantFullName());
                flag.setRoleOnCase(NotificationParties.APPLICANT.getLabel());
                caseData.setApplicantFlags(flag);
            } else if (null != caseData.getCicCase().getNotifyPartySubject()
                && !isEmpty(caseData.getCicCase().getNotifyPartySubject())) {
                flag.setPartyName(caseData.getCicCase().getFullName());
                flag.setRoleOnCase(NotificationParties.SUBJECT.getLabel());
                caseData.setSubjectFlags(flag);
            } else if (null != caseData.getCicCase().getNotifyPartyRepresentative()
                && !isEmpty(caseData.getCicCase().getNotifyPartyRepresentative())) {
                flag.setPartyName(caseData.getCicCase().getRepresentativeFullName());
                flag.setRoleOnCase(NotificationParties.REPRESENTATIVE.getLabel());
                caseData.setRepresentativeFlags(flag);
            }
        } else {
            caseData.setCaseFlags(flag);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(details.getState())
            .build();
    }

    public SubmittedCallbackResponse flagCreated(CaseDetails<CaseData, State> details,
                                                 CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Case Flag created %n## This Flag has been added to case %n## %s",
                MessageUtil.generateSimpleMessage(details.getData().getCicCase())))
            .build();
    }


}
