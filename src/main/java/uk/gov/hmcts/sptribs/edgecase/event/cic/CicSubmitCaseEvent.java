package uk.gov.hmcts.sptribs.edgecase.event.cic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.common.ccd.CcdCaseType;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.edgecase.model.CaseData;
import uk.gov.hmcts.sptribs.edgecase.model.State;
import uk.gov.hmcts.sptribs.edgecase.model.UserRole;
import uk.gov.hmcts.sptribs.util.AppsUtil;

import static uk.gov.hmcts.sptribs.edgecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.sptribs.edgecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CicSubmitCaseEvent implements CCDConfig<CaseData, State, UserRole>  {

    @Autowired
    AppsConfig appsConfig;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(AppsUtil.getExactAppsDetailsByCaseType(appsConfig, CcdCaseType.CIC.getCaseTypeName()).getEventIds()
                       .getSubmitEvent())
            .forStates(State.DRAFT)
            .name("Submit case (cic)")
            .description("Applicant confirms SOT (cic)")
            .retries(120, 120)
            .grant(CREATE_READ_UPDATE, CITIZEN)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        //TODO logic needs to be updated separately as per edge-case application requirement
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(details.getState())
            .build();
    }

}
