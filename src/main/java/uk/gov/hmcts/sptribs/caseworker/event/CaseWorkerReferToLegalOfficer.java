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

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_REFER_TO_LEGAL_OFFICER;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseStayed;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
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
            .forStates(CaseManagement, AwaitingHearing, AwaitingOutcome, CaseClosed, CaseStayed)
            .name("Refer case to legal officer")
            .showSummary()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::referred)
            .grant(CREATE_READ_UPDATE, ST_CIC_HEARING_CENTRE_ADMIN, ST_CIC_HEARING_CENTRE_TEAM_LEADER));

        referToLegalOfficerReason.addTo(pageBuilder);
        referToLegalOfficerAdditionalInfo.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Caseworker refer case to legal officer for Case Id: {}", details.getId());

        var caseData = details.getData();
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
