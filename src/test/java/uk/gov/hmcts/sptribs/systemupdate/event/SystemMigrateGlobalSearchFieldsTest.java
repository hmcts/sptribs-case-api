package uk.gov.hmcts.sptribs.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.service.ExtendedCaseDataService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.service.CcdSupplementaryDataService;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.BASE_LOCATION;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.CASE_MANAGEMENT_CATEGORY;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.PUBLIC_DATA_CLASSIFICATION;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.REGION;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_WA_CASE_BASE_LOCATION;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_WA_CASE_MANAGEMENT_CATEGORY;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_WA_CASE_REGION;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemMigrateGlobalSearchFields.SYSTEM_MIGRATE_GLOBAL_SEARCH_FIELDS;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class SystemMigrateGlobalSearchFieldsTest {

    @Mock
    private CcdSupplementaryDataService ccdSupplementaryDataService;

    @Mock
    private ExtendedCaseDataService extendedCaseDataService;

    @InjectMocks
    private SystemMigrateGlobalSearchFields systemMigrateGlobalSearchFields;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemMigrateGlobalSearchFields.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_MIGRATE_GLOBAL_SEARCH_FIELDS);
    }

    @Test
    void shouldPopulateAllGlobalSearchFieldsInAboutToSubmitCallback() {
        final CaseData caseData = new CaseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Map<String, Object> dataClassificationMap = new HashMap<>();

        when(extendedCaseDataService.getDataClassification(TEST_CASE_ID.toString()))
            .thenReturn(dataClassificationMap);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemMigrateGlobalSearchFields.aboutToSubmit(caseDetails, caseDetails);

        assertTrue(response.getDataClassification().containsKey(CASE_MANAGEMENT_CATEGORY));
        assertThat(response.getDataClassification().get(CASE_MANAGEMENT_CATEGORY)).isEqualTo(PUBLIC_DATA_CLASSIFICATION);
        assertTrue(response.getDataClassification().containsKey(CASE_MANAGEMENT_LOCATION));
        assertThat(response.getDataClassification().get(CASE_MANAGEMENT_LOCATION))
            .isEqualTo(Map.of(BASE_LOCATION, PUBLIC_DATA_CLASSIFICATION, REGION, PUBLIC_DATA_CLASSIFICATION));
        assertThat(response.getData().getCaseManagementLocation().getBaseLocation()).isEqualTo(ST_CIC_WA_CASE_BASE_LOCATION);
        assertThat(response.getData().getCaseManagementLocation().getRegion()).isEqualTo(ST_CIC_WA_CASE_REGION);
        assertThat(response.getData().getCaseManagementCategory().getValueLabel()).isEqualTo(ST_CIC_WA_CASE_MANAGEMENT_CATEGORY);
        assertThat(response.getData().getCaseManagementCategory().getListItems()).hasSize(1);

    }

    @Test
    void shouldCallSupplementaryDataEndpointInSubmittedCallback() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);

        systemMigrateGlobalSearchFields.submitted(caseDetails, caseDetails);

        verify(ccdSupplementaryDataService).submitSupplementaryDataToCcd(TEST_CASE_ID.toString());
    }
}
