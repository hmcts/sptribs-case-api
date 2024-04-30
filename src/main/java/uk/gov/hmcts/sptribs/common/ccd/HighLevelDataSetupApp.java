package uk.gov.hmcts.sptribs.common.ccd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.befta.dse.ccd.CcdEnvironment;
import uk.gov.hmcts.befta.dse.ccd.CcdRoleConfig;
import uk.gov.hmcts.befta.dse.ccd.DataLoaderToDefinitionStore;
import uk.gov.hmcts.befta.exception.ImportException;

import java.util.List;
import java.util.Locale;


public class HighLevelDataSetupApp extends DataLoaderToDefinitionStore {

    private static final Logger logger = LoggerFactory.getLogger(HighLevelDataSetupApp.class);

    private static final CcdRoleConfig[] CCD_ROLES_NEEDED_FOR_ST_CIC = {
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
        new CcdRoleConfig("caseworker-st-cic", "PUBLIC")
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
        for (CcdRoleConfig roleConfig : CCD_ROLES_NEEDED_FOR_ST_CIC) {
            try {
                logger.info("\n\nAdding CCD Role {}.", roleConfig);
                addCcdRole(roleConfig);
                logger.info("\n\nAdded CCD Role {}.", roleConfig);
            } catch (Exception e) {
                logger.error("\n\nCouldn't add CCD Role {} - Exception: {}.\n\n", roleConfig, e);
                if (!shouldTolerateDataSetupFailure(e)) {
                    throw e;
                }
            }
        }
    }

    @Override
    protected List<String> getAllDefinitionFilesToLoadAt(String definitionsPath) {
        String environmentName = environment.name().toLowerCase(Locale.UK);
        return List.of(
            "build/ccd-config/ccd-" + CcdServiceCode.ST_CIC.getCaseType().getCaseTypeName() + "-" + environmentName + ".xlsx"
        );
    }
}
