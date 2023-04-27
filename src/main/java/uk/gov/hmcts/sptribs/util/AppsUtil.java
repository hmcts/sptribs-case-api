package uk.gov.hmcts.sptribs.util;

import lombok.NoArgsConstructor;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;

@NoArgsConstructor
@SuppressWarnings("HideUtilityClassConstructor")
public class AppsUtil {

    public static boolean isValidCaseTypeOfApplication(AppsConfig appsConfig, DssCaseData caseData) {
        return caseData.getCaseTypeOfApplication() != null && appsConfig.getApps().stream()
            .anyMatch(eachApps -> eachApps.getCaseTypeOfApplication().contains(caseData.getCaseTypeOfApplication()));
    }

    public static AppsConfig.AppsDetails getExactAppsDetails(AppsConfig appsConfig, DssCaseData caseData) {
        return appsConfig.getApps().stream()
            .filter(eachApps -> eachApps.getCaseTypeOfApplication().contains(caseData.getCaseTypeOfApplication()))
            .findFirst().orElse(null);
    }

    public static AppsConfig.AppsDetails getExactAppsDetails(AppsConfig appsConfig, String caseTypeOfApplication) {
        return appsConfig.getApps().stream()
            .filter(eachApps -> eachApps.getCaseTypeOfApplication().contains(caseTypeOfApplication))
            .findFirst().orElse(null);
    }

    public static AppsConfig.AppsDetails getExactAppsDetailsByCaseType(AppsConfig appsConfig, String caseType) {
        return appsConfig.getApps().stream()
            .filter(eachApps -> eachApps.getCaseType().equalsIgnoreCase(caseType))
            .findFirst().orElse(null);
    }
}
