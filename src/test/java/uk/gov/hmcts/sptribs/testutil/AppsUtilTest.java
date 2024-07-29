package uk.gov.hmcts.sptribs.testutil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.constants.CommonConstants;
import uk.gov.hmcts.sptribs.util.AppsUtil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_CASE_TYPE;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_JURISDICTION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_CIC_ID;

@ExtendWith(SpringExtension.class)
public class AppsUtilTest {

    @Mock
    private AppsConfig.AppsDetails cicAppDetails;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        cicAppDetails = new AppsConfig.AppsDetails();
        cicAppDetails.setCaseType(CommonConstants.ST_CIC_CASE_TYPE);
        cicAppDetails.setJurisdiction(CommonConstants.ST_CIC_JURISDICTION);
        cicAppDetails.setCaseTypeOfApplication(List.of(CASE_DATA_CIC_ID));
    }

    @Test
    void isValidCaseTypeOfApplicationTest() {
        DssCaseData a100CaseData = DssCaseData.builder().caseTypeOfApplication(CASE_DATA_CIC_ID).build();
        AppsConfig appsConfig = getDefaultAppsConfig();
        assertTrue(AppsUtil.isValidCaseTypeOfApplication(appsConfig, a100CaseData));
    }

    @Test
    void inValidCaseTypeOfApplicationTest() {
        DssCaseData a100CaseData = DssCaseData.builder().caseTypeOfApplication("dummy").build();
        AppsConfig appsConfig = getDefaultAppsConfig();
        assertFalse(AppsUtil.isValidCaseTypeOfApplication(appsConfig, a100CaseData));
    }

    @Test
    void validateExactAppDetailsTest() {
        DssCaseData cicCaseData = DssCaseData.builder().caseTypeOfApplication(CASE_DATA_CIC_ID).build();
        AppsConfig appsConfig = getDefaultAppsConfig();
        AppsConfig.AppsDetails appDetails = AppsUtil.getExactAppsDetails(appsConfig, cicCaseData);
        assertEquals(ST_CIC_CASE_TYPE, appDetails.getCaseType());
        assertEquals(ST_CIC_JURISDICTION, appDetails.getJurisdiction());
    }

    @Test
    void validateExactAppDetailsByCaseTypeOfApplicationTest() {
        AppsConfig appsConfig = getDefaultAppsConfig();
        AppsConfig.AppsDetails appDetails = AppsUtil.getExactAppsDetails(appsConfig, CASE_DATA_CIC_ID);
        assertEquals(ST_CIC_CASE_TYPE, appDetails.getCaseType());
        assertEquals(ST_CIC_JURISDICTION, appDetails.getJurisdiction());
    }

    @Test
    void validateExactAppDetailsByCaseTypeTest() {
        AppsConfig appsConfig = getDefaultAppsConfig();
        AppsConfig.AppsDetails appDetails = AppsUtil.getExactAppsDetailsByCaseType(appsConfig, ST_CIC_CASE_TYPE);
        assertEquals(CASE_DATA_CIC_ID, appDetails.getCaseTypeOfApplication().get(0));
        assertEquals(ST_CIC_JURISDICTION, appDetails.getJurisdiction());
    }

    private AppsConfig getDefaultAppsConfig() {
        AppsConfig appsConfig = new AppsConfig();
        appsConfig.setApps(List.of(cicAppDetails));
        return  appsConfig;
    }
}
