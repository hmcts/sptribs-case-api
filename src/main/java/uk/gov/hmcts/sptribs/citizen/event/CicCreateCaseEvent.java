package uk.gov.hmcts.sptribs.citizen.event;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.CaseLocation;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdCaseType;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.common.service.CcdSupplementaryDataService;
import uk.gov.hmcts.sptribs.util.AppsUtil;

import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CREATOR;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_WA_CASE_BASE_LOCATION;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_WA_CASE_MANAGEMENT_CATEGORY;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_WA_CASE_REGION;

@Component
@Slf4j
@Setter
public class CicCreateCaseEvent implements CCDConfig<CaseData, State, UserRole> {

    private final AppsConfig appsConfig;
    private final CcdSupplementaryDataService ccdSupplementaryDataService;

    @Autowired
    public CicCreateCaseEvent(AppsConfig appsConfig, CcdSupplementaryDataService ccdSupplementaryDataService) {
        this.appsConfig = appsConfig;
        this.ccdSupplementaryDataService = ccdSupplementaryDataService;
    }

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
            .submittedCallback(this::submitted)
            .retries(120, 120);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = details.getData();

        caseData.setHyphenatedCaseRef(details.getData().formatCaseRef(details.getId()));
        setIsRepresentativePresent(caseData);
        setDefaultCaseDetails(caseData);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(State.DSS_Draft)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        ccdSupplementaryDataService.submitSupplementaryDataRequestToCcd(details.getId().toString());

        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Application Created %n##")
            .build();
    }

    private void setIsRepresentativePresent(CaseData data) {
        if (data.getDssCaseData().getRepresentativeFullName() != null) {
            data.getDssCaseData().setIsRepresentativePresent(YesOrNo.YES);
        } else {
            data.getDssCaseData().setIsRepresentativePresent(YesOrNo.NO);
        }
    }

    private void setDefaultCaseDetails(CaseData data) {
        data.setCaseManagementLocation(
            CaseLocation
                .builder()
                .baseLocation(ST_CIC_WA_CASE_BASE_LOCATION)
                .region(ST_CIC_WA_CASE_REGION)
                .build()
        );

        DynamicListElement caseManagementCategory = new DynamicListElement(
            UUID.randomUUID(), ST_CIC_WA_CASE_MANAGEMENT_CATEGORY);
        data.setCaseManagementCategory(
            DynamicList
                .builder()
                .listItems(List.of(caseManagementCategory))
                .value(caseManagementCategory)
                .build()
        );
    }
}
