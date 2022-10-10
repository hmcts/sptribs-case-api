package uk.gov.hmcts.sptribs.common.ccd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.befta.dse.ccd.CcdEnvironment;
import uk.gov.hmcts.befta.dse.ccd.CcdRoleConfig;
import uk.gov.hmcts.befta.dse.ccd.DataLoaderToDefinitionStore;
import uk.gov.hmcts.befta.util.BeftaUtils;
import uk.gov.hmcts.sptribs.ciccase.CriminalInjuriesCompensation;

import java.util.List;
import java.util.Locale;


public class HighLevelDataSetupApp extends DataLoaderToDefinitionStore {

    private static final Logger logger = LoggerFactory.getLogger(HighLevelDataSetupApp.class);

    private static final CcdRoleConfig[] CCD_ROLES_NEEDED_FOR_NFD = {
        new CcdRoleConfig("caseworker-divorce-superuser", "PUBLIC"),
        new CcdRoleConfig("caseworker-sptribs-superuser", "PUBLIC"),
        new CcdRoleConfig("caseworker-divorce-solicitor", "PUBLIC"),
        new CcdRoleConfig("caseworker-divorce-systemupdate", "PUBLIC"),
        new CcdRoleConfig("payments", "PUBLIC")
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
    protected boolean shouldTolerateDataSetupFailure() {
        return true;
    }

    @Override
    public void addCcdRoles() {
        for (CcdRoleConfig roleConfig : CCD_ROLES_NEEDED_FOR_NFD) {
            try {
                logger.info("\n\nAdding CCD Role {}.", roleConfig);
                addCcdRole(roleConfig);
                logger.info("\n\nAdded CCD Role {}.", roleConfig);
            } catch (Exception e) {
                logger.error("\n\nCouldn't add CCD Role {} - Exception: {}.\n\n", roleConfig, e);
                if (!shouldTolerateDataSetupFailure()) {
                    throw e;
                }
            }
        }
    }

    @Override
    protected List<String> getAllDefinitionFilesToLoadAt(String definitionsPath) {
        String environmentName = environment.name().toLowerCase(Locale.UK);
        return List.of(
            "build/ccd-config/ccd-" + CriminalInjuriesCompensation.CASE_TYPE + "-" + environmentName + ".xlsx"
        );
    }

    @Override
    public void createRoleAssignments() {
        // Do not create role assignments.
        BeftaUtils.defaultLog("Will NOT create role assignments!");
    }
}
