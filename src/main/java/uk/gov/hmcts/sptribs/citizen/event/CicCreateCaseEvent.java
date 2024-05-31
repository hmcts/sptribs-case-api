package uk.gov.hmcts.sptribs.citizen.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdCaseType;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.util.AppsUtil;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CREATOR;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CicCreateCaseEvent implements CCDConfig<CaseData, State, UserRole> {

    @Autowired
    AppsConfig appsConfig;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(AppsUtil.getExactAppsDetailsByCaseType(appsConfig, CcdCaseType.CIC.getCaseTypeName()).getEventIds()
                .getCreateEvent())
            .initialState(State.Draft)
            .name("Create draft case (DSS)")
            .description("Apply for edge case (DSS)")
            .grant(CREATE_READ_UPDATE, CITIZEN, CREATOR)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .retries(120, 120);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = details.getData();

        caseData.setHyphenatedCaseRef(details.getData().formatCaseRef(details.getId()));
        setIsRepresentativePresent(caseData);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(State.DSS_Draft)
            .build();
    }

    private void setIsRepresentativePresent(CaseData data) {
        if (data.getDssCaseData().getRepresentativeFullName() != null) {
            data.getDssCaseData().setIsRepresentativePresent(YesOrNo.YES);
        } else {
            data.getDssCaseData().setIsRepresentativePresent(YesOrNo.NO);
        }
    }
}
