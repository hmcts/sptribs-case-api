package uk.gov.hmcts.sptribs.cftlib;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CCDDefinitionGenerator;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLib;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLibConfigurer;
import uk.gov.hmcts.sptribs.common.ccd.CcdCaseType;
import uk.gov.hmcts.sptribs.common.ccd.CcdServiceCode;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Component
public class CftLibConfig implements CFTLibConfigurer {

    @Value("ccd-CIC-${CCD_DEF_NAME:dev}.xlsx")
    String defName;

    @Value("Submitted")
    String state;


    @Autowired
    CCDDefinitionGenerator configWriter;

    @Override
    public void configure(CFTLib lib) throws Exception {
        for (String p : List.of(
            "DivCaseWorkerUser@AAT.com",
            "TEST_CASE_WORKER_USER@mailinator.com",
            "TEST_SOLICITOR@mailinator.com",
            "divorce_as_caseworker_admin@mailinator.com")) {
            lib.createProfile(p, "DIVORCE", "NO_FAULT_DIVORCE", state);
            lib.createProfile(p, CcdServiceCode.CIC.getCcdServiceName(), CcdCaseType.CIC.name(), state);
            lib.createProfile(p, CcdServiceCode.CS.getCcdServiceName(), CcdCaseType.CS.name(), state);
            lib.createProfile(p, CcdServiceCode.MH.getCcdServiceName(), CcdCaseType.MH.name(), state);
            lib.createProfile(p, CcdServiceCode.PHL.getCcdServiceName(), CcdCaseType.PHL.name(), state);
            lib.createProfile(p, CcdServiceCode.SEN.getCcdServiceName(), CcdCaseType.SEN.name(), state);
            lib.createProfile(p, CcdServiceCode.DD.getCcdServiceName(), CcdCaseType.DD.name(), state);
        }

        lib.createRoles(
            "caseworker-divorce-superuser",
            "caseworker-divorce-solicitor",
            "caseworker-divorce-systemupdate",
            "caseworker-sptribs-superuser",
            "caseworker-sptribs-cic-courtadmin",
            "citizen-sptribs-cic-dss",
            "caseworker-sptribs-cic-caseofficer",
            "caseworker-sptribs-cic-districtregistrar",
            "caseworker-sptribs-cic-districtjudge",
            "caseworker-sptribs-cic-respondent",
            "caseworker",
            "payments"
        );
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        var json = IOUtils.toString(resourceLoader.getResource("classpath:cftlib-am-role-assignments.json")
            .getInputStream(), Charset.defaultCharset());
        lib.configureRoleAssignments(json);

        // Generate and import CCD definitions
        generateCCDDefinition();

        var nfdDefinition = Files.readAllBytes(Path.of("build/ccd-config/" + defName));
        lib.importDefinition(nfdDefinition);
    }

    /**
     * Generate our JSON ccd definition and convert it to xlsx.
     * Doing this at runtime in the CftlibConfig allows use of spring boot devtool's
     * live reload functionality to rapidly edit and test code & definition changes.
     */
    private void generateCCDDefinition() throws Exception {
        // Export the JSON config.
        configWriter.generateAllCaseTypesToJSON(new File("build/definitions"));
        // Run the gradle task to convert to xlsx.
        var code = new ProcessBuilder("./gradlew", "buildCCDXlsx")
            .inheritIO()
            .start()
            .waitFor();
        if (code != 0) {
            throw new RuntimeException("Error converting ccd json to xlsx");
        }
    }
}
