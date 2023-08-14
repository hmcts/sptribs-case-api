package uk.gov.hmcts.sptribs.citizen.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CITIZEN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CREATOR;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CicCreateCaseEvent implements CCDConfig<CaseData, State, UserRole> {

    @Value("${feature.dss-frontend.enabled}")
    private boolean dssCreateCaseEnabled;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        if (dssCreateCaseEnabled) {
            doConfigure(configBuilder);
        }
    }

    private void doConfigure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event("citizen-cic-create-dss-application")
            .initialState(State.Draft)
            .name("Create draft case (DSS)")
            .description("Apply for edge case (DSS)")
            .grant(CREATE_READ_UPDATE, CITIZEN_CIC, CREATOR)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .retries(120, 120);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        var caseData = details.getData();
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(State.DSS_Draft)
            .build();
    }


}
