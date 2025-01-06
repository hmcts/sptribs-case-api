package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.ReferToLegalOfficerAdditionalInfo;
import uk.gov.hmcts.sptribs.caseworker.event.page.ReferToLegalOfficerReason;
import uk.gov.hmcts.sptribs.caseworker.model.ReferToLegalOfficer;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.time.LocalDate;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_REFER_TO_LEGAL_OFFICER;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseStayed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.ReadyToList;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CaseWorkerReferToLegalOfficer implements CCDConfig<CaseData, State, UserRole> {

    private final ReferToLegalOfficerReason referToLegalOfficerReason = new ReferToLegalOfficerReason();
    private final ReferToLegalOfficerAdditionalInfo referToLegalOfficerAdditionalInfo = new ReferToLegalOfficerAdditionalInfo();

    @Value("${feature.wa.enabled}")
    private boolean isWorkAllocationEnabled;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
            configBuilder
                .event(CASEWORKER_REFER_TO_LEGAL_OFFICER)
                .forStates(CaseManagement, ReadyToList, AwaitingHearing, AwaitingOutcome, CaseClosed, CaseStayed)
                .name("Refer case to legal officer")
                .showSummary()
                .showEventNotes()
                .aboutToStartCallback(this::aboutToStart)
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::submitted)
                .grant(CREATE_READ_UPDATE, SUPER_USER,
                    ST_CIC_HEARING_CENTRE_ADMIN, ST_CIC_HEARING_CENTRE_TEAM_LEADER,
                    ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_JUDGE, ST_CIC_SENIOR_JUDGE);

        if (isWorkAllocationEnabled) {
            eventBuilder.publishToCamunda()
                        .grant(CREATE_READ_UPDATE, ST_CIC_WA_CONFIG_USER);
        }

        PageBuilder pageBuilder = new PageBuilder(eventBuilder);
        referToLegalOfficerReason.addTo(pageBuilder);
        referToLegalOfficerAdditionalInfo.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        CaseData caseData = details.getData();
        caseData.setReferToLegalOfficer(new ReferToLegalOfficer());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        CaseData caseData = details.getData();
        caseData.getReferToLegalOfficer().setReferralDate(LocalDate.now());
        if (isWorkAllocationEnabled
                && caseData.getReferToLegalOfficer() != null
                && caseData.getReferToLegalOfficer().getReferralReason() != null) {
            caseData.getCicCase().setReferralTypeForWA(caseData.getReferToLegalOfficer().getReferralReason().getLabel());
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                              CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Referral completed")
            .build();
    }

}
