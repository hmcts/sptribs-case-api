package uk.gov.hmcts.sptribs.common.ccd;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.befta.dse.ccd.CcdEnvironment;
import uk.gov.hmcts.befta.dse.ccd.CcdRoleConfig;
import uk.gov.hmcts.befta.dse.ccd.DataLoaderToDefinitionStore;
import uk.gov.hmcts.befta.exception.ImportException;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Slf4j
public class HighLevelDataSetupApp extends DataLoaderToDefinitionStore {

    // Shared by every case type in this service: CCD roles are service-wide, not
    // per case type.
    private static final CcdRoleConfig[] CCD_ROLES = {
        new CcdRoleConfig("caseworker-sptribs-superuser", "PUBLIC"),
        new CcdRoleConfig("caseworker", "PUBLIC"),
        new CcdRoleConfig("caseworker-sptribs-systemupdate", "PUBLIC"),
        new CcdRoleConfig("caseworker-sptribs", "PUBLIC"),
        new CcdRoleConfig("caseworker-sptribs-cic-districtjudge", "PUBLIC"),
        new CcdRoleConfig("caseworker-sptribs-cic-respondent", "PUBLIC"),
        new CcdRoleConfig("caseworker-st_cic-caseworker", "PUBLIC"),
        new CcdRoleConfig("caseworker-st_cic-senior-caseworker", "PUBLIC"),
        new CcdRoleConfig("caseworker-st_cic-hearing-centre-admin", "PUBLIC"),
        new CcdRoleConfig("caseworker-st_cic-hearing-centre-team-leader", "PUBLIC"),
        new CcdRoleConfig("caseworker-st_cic-senior-judge", "PUBLIC"),
        new CcdRoleConfig("caseworker-st_cic-judge", "PUBLIC"),
        new CcdRoleConfig("caseworker-st_cic-respondent", "PUBLIC"),
        new CcdRoleConfig("caseflags-admin", "PUBLIC"),
        new CcdRoleConfig("caseflags-viewer", "PUBLIC"),
        new CcdRoleConfig("citizen", "PUBLIC"),
        new CcdRoleConfig("caseworker-st-cic", "PUBLIC"),
        new CcdRoleConfig("caseworker-wa-task-configuration", "RESTRICTED"),
        new CcdRoleConfig("GS_profile", "PUBLIC"),
        new CcdRoleConfig("caseworker-ras-validation", "PUBLIC"),
        new CcdRoleConfig("non-respondent-user", "PUBLIC")
    };

    private final CcdEnvironment environment;

    public HighLevelDataSetupApp(CcdEnvironment dataSetupEnvironment) {
        super(dataSetupEnvironment);
        environment = dataSetupEnvironment;
    }

    public static void main(String[] args) throws Throwable {
        main(HighLevelDataSetupApp.class, args);
    }

    @Override
    protected boolean shouldTolerateDataSetupFailure(Throwable e) {
        /* Sometimes a heavy CCD definition would take more than 30 secs and throws 504 error.
        But still the CCD definition will eventually get imported without any issues.
        So, the 504 error code can be tolerated. */
        if (e instanceof ImportException importException) {
            return importException.getHttpStatusCode() == HttpStatus.GATEWAY_TIMEOUT.value();
        }

        return false;
    }

    @Override
    public void addCcdRoles() {
        for (CcdRoleConfig roleConfig : CCD_ROLES) {
            try {
                log.info("\n\nAdding CCD Role {}.", roleConfig);
                addCcdRole(roleConfig);
                log.info("\n\nAdded CCD Role {}.", roleConfig);
            } catch (Exception e) {
                log.error("\n\nCouldn't add CCD Role {} - Exception: {}.\n\n", roleConfig, e);
                if (!shouldTolerateDataSetupFailure(e)) {
                    throw e;
                }
            }
        }
    }

    @Override
    protected List<String> getAllDefinitionFilesToLoadAt(String definitionsPath) {
        String environmentName = environment.name().toLowerCase(Locale.UK);
        // One spreadsheet per case type, discovered from the enum rather than
        // listed here, so registering a case type is a single enum constant and
        // not an edit in two places that can silently disagree.
        return Arrays.stream(CcdServiceCode.values())
            .map(serviceCode -> "build/ccd-config/ccd-" + serviceCode.getCaseType().getCaseTypeName() + "-" + environmentName + ".xlsx")
            .toList();
    }
}
