package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.ReferToLegalOfficerAdditionalInfo;
import uk.gov.hmcts.sptribs.caseworker.event.page.ReferToLegalOfficerReason;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.time.LocalDate;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_REFER_TO_LEGAL_OFFICER;
import static uk.gov.hmcts.sptribs.ciccase.model.State.*;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CaseWorkerReferToLegalOfficer implements CCDConfig<CaseData, State, UserRole> {

    private final ReferToLegalOfficerReason referToLegalOfficerReason = new ReferToLegalOfficerReason();
    private final ReferToLegalOfficerAdditionalInfo referToLegalOfficerAdditionalInfo = new ReferToLegalOfficerAdditionalInfo();

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(configBuilder
            .event(CASEWORKER_REFER_TO_LEGAL_OFFICER)
            .forStates(CaseManagement, ReadyToList, AwaitingHearing, AwaitingOutcome, CaseClosed, CaseStayed, Submitted)
            .name("Refer case to legal officer")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::referred)
            .grant(CREATE_READ_UPDATE, ST_CIC_HEARING_CENTRE_ADMIN, ST_CIC_HEARING_CENTRE_TEAM_LEADER,
                ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER));

        referToLegalOfficerReason.addTo(pageBuilder);
        referToLegalOfficerAdditionalInfo.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Caseworker refer case to legal officer for Case Id: {}", details.getId());

        CaseData caseData = details.getData();
        caseData.getReferToLegalOfficer().setReferralDate(LocalDate.now());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public SubmittedCallbackResponse referred(CaseDetails<CaseData, State> details,
                                              CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Referral completed")
            .build();
    }

}
