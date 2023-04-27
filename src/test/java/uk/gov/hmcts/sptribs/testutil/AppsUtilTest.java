package uk.gov.hmcts.sptribs.testutil;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.util.AppsUtil;

import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_CASE_TYPE;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_JURISDICTION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_CIC_ID;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource("classpath:application.yaml")
@SuppressWarnings("PMD")
public class AppsUtilTest {

    @Autowired
    AppsConfig appsConfig;

    @Test
    void isValidCaseTypeOfApplicationTest() {
        DssCaseData a100CaseData = DssCaseData.builder().caseTypeOfApplication(CASE_DATA_CIC_ID).build();
        Assert.assertTrue(AppsUtil.isValidCaseTypeOfApplication(appsConfig, a100CaseData));
    }

    @Test
    void inValidCaseTypeOfApplicationTest() {
        DssCaseData a100CaseData = DssCaseData.builder().caseTypeOfApplication("dummy").build();
        Assert.assertFalse(AppsUtil.isValidCaseTypeOfApplication(appsConfig, a100CaseData));
    }

    @Test
    void validateExactAppDetailsTest() {
        DssCaseData cicCaseData = DssCaseData.builder().caseTypeOfApplication(CASE_DATA_CIC_ID).build();
        AppsConfig.AppsDetails appDetails = AppsUtil.getExactAppsDetails(appsConfig, cicCaseData);
        Assert.assertEquals(ST_CIC_CASE_TYPE, appDetails.getCaseType());
        Assert.assertEquals(ST_CIC_JURISDICTION, appDetails.getJurisdiction());
    }
}
