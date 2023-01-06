package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.PartiesCIC;
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
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseworkerCaseFlag implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_CASE_FLAG = "caseworker-case-flag";


    private static final CcdPageConfiguration flagLevel = new FlagLevel();
    private static final CcdPageConfiguration flagParties = new FlagParties();
    private static final CcdPageConfiguration flagType = new FlagTypePage();
    private static final CcdPageConfiguration flagAdditionalInfo = new FlagAdditionalInfo();

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
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
            .name("Create a case flag")
            .showSummary()
            .description("Create a case flag")
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::flagCreated)
            .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
            .grantHistoryOnly(SOLICITOR));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Caseworker stay the case callback invoked for Case Id: {}", details.getId());

        var caseData = details.getData();
        var caseFlag = caseData.getCaseFlag();
        var flag = new Flags();
        var flagDetail = new FlagDetail();
        flagDetail.setName(caseFlag.getFlagType().getLabel());
        flagDetail.setFlagCode(caseFlag.getFlagType().getFlagCode());
        flagDetail.setFlagComment(caseFlag.getAdditionalDetail());
        flagDetail.setOtherDescription(caseFlag.getOtherDescription());
        flagDetail.setStatus(Status.ACTIVE.getLabel());
        if (caseFlag.getFlagLevel().isPartyLevel()) {
            if (null != caseData.getCicCase().getFlagPartyApplicant() && caseData.getCicCase().getFlagPartyApplicant().size() > 0) {
                flag.setPartyName(caseData.getCicCase().getApplicantFullName());
                flag.setRoleOnCase(PartiesCIC.APPLICANT.getLabel());
            } else if (null != caseData.getCicCase().getFlagPartySubject() && caseData.getCicCase().getFlagPartySubject().size() > 0) {
                flag.setPartyName(caseData.getCicCase().getFullName());
                flag.setRoleOnCase(PartiesCIC.SUBJECT.getLabel());
            } else if (null != caseData.getCicCase().getFlagPartyRepresentative()
                && caseData.getCicCase().getFlagPartyRepresentative().size() > 0) {
                flag.setPartyName(caseData.getCicCase().getRepresentativeFullName());
                flag.setRoleOnCase(PartiesCIC.REPRESENTATIVE.getLabel());
            }
        }
        var flagDetails = new ArrayList<FlagDetail>();
        flagDetails.add(flagDetail);
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
        if (isEmpty(caseData.getCaseFlag().getCaseFlags())) {
            List<ListValue<Flags>> listValues = new ArrayList<>();

            var listValue = ListValue
                .<Flags>builder()
                .id("1")
                .value(flag)
                .build();

            listValues.add(listValue);

            caseData.getCaseFlag().setCaseFlags(listValues);
        } else {
            AtomicInteger listValueIndex = new AtomicInteger(0);
            var listValue = ListValue
                .<Flags>builder()
                .value(flag)
                .build();

            caseData.getCaseFlag().getCaseFlags().add(0, listValue);
            caseData.getCaseFlag().getCaseFlags()
                .forEach(flagsListValue -> flagsListValue.setId(String.valueOf(listValueIndex.incrementAndGet())));
        }

        caseData.getCicCase().setFlagPartyApplicant(null);
        caseData.getCicCase().setFlagPartyRepresentative(null);
        caseData.getCicCase().setFlagPartySubject(null);
        caseData.getCaseFlag().setFlagLevel(null);
        caseData.getCaseFlag().setFlagType(null);
        caseData.getCaseFlag().setAdditionalDetail(null);
        caseData.getCaseFlag().setOtherDescription(null);
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(details.getState())
            .build();
    }

    public SubmittedCallbackResponse flagCreated(CaseDetails<CaseData, State> details,
                                                 CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Case Flag created %n## This Flag has been added to case"))
            .build();
    }
}
