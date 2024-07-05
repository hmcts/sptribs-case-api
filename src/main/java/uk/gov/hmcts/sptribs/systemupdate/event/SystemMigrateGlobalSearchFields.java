package uk.gov.hmcts.sptribs.systemupdate.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.sptribs.caseworker.model.CaseManagementLocation;
import uk.gov.hmcts.sptribs.caseworker.service.ExtendedCaseDataService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.service.CcdSupplementaryDataService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.BASE_LOCATION;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.CASE_MANAGEMENT_CATEGORY;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.PUBLIC_DATA_CLASSIFICATION;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.REGION;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_WA_CASE_BASE_LOCATION;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_WA_CASE_MANAGEMENT_CATEGORY;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_WA_CASE_REGION;

@Component
@Slf4j
public class SystemMigrateGlobalSearchFields implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_MIGRATE_GLOBAL_SEARCH_FIELDS = "system-migrate-global-search-fields";

    @Autowired
    private CcdSupplementaryDataService ccdSupplementaryDataService;

    @Autowired
    private ExtendedCaseDataService extendedCaseDataService;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(SYSTEM_MIGRATE_GLOBAL_SEARCH_FIELDS)
            .forAllStates()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .name("System Migrate Global Search Fields")
            .description("System Migrate Global Search Fields")
            .grant(CREATE_READ_UPDATE_DELETE, SYSTEM_UPDATE);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        final Long caseId = details.getId();
        final CaseData caseData = details.getData();
        final Map<String, Object> dataClassification = setDataClassification(caseId);
        caseData.setCaseManagementCategory(setCaseManagementCategory());
        caseData.setCaseManagementLocation(setCaseManagementLocation());
        setSupplementaryData(caseId);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .dataClassification(dataClassification)
            .build();
    }

    private Map<String, Object> setDataClassification(Long caseId) {
        final Map<String, Object> dataClassification = extendedCaseDataService.getDataClassification(caseId.toString());

        dataClassification.put(CASE_MANAGEMENT_CATEGORY, PUBLIC_DATA_CLASSIFICATION);
        dataClassification.put(
            CASE_MANAGEMENT_LOCATION,
            Map.of(BASE_LOCATION, PUBLIC_DATA_CLASSIFICATION, REGION, PUBLIC_DATA_CLASSIFICATION)
        );

        return dataClassification;
    }

    private DynamicList setCaseManagementCategory() {
        final UUID uuid = UUID.randomUUID();
        final DynamicListElement cicCategory = DynamicListElement.builder()
            .code(uuid)
            .label(ST_CIC_WA_CASE_MANAGEMENT_CATEGORY)
            .build();
        final List<DynamicListElement> caseManagementCategoryList = new ArrayList<>();
        caseManagementCategoryList.add(cicCategory);

        return DynamicList.builder()
            .value(cicCategory)
            .listItems(caseManagementCategoryList)
            .build();
    }

    private CaseManagementLocation setCaseManagementLocation() {
        return CaseManagementLocation.builder()
            .baseLocation(ST_CIC_WA_CASE_BASE_LOCATION)
            .region(ST_CIC_WA_CASE_REGION)
            .build();
    }

    private void setSupplementaryData(Long caseId) {
        try {
            ccdSupplementaryDataService.submitSupplementaryDataToCcd(caseId.toString());
        } catch (Exception exception) {
            log.error("Unable to set Supplementary data with exception : {}", exception.getMessage());
        }
    }
}
