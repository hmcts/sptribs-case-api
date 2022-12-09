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
import java.util.List;

@Component
public class CftLibConfig implements CFTLibConfigurer {

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
            lib.createProfile(p, CcdServiceCode.ST_CIC.name(), CcdServiceCode.ST_CIC.getCaseType().getCaseName(), state);
            lib.createProfile(p, CcdServiceCode.ST_CS.name(), CcdServiceCode.ST_CS.getCaseType().getCaseName(), state);
            lib.createProfile(p, CcdServiceCode.ST_MH.name(), CcdServiceCode.ST_MH.getCaseType().getCaseName(), state);
            lib.createProfile(p, CcdServiceCode.ST_PHL.name(), CcdServiceCode.ST_PHL.getCaseType().getCaseName(), state);
            lib.createProfile(p, CcdServiceCode.ST_SEN.name(), CcdServiceCode.ST_SEN.getCaseType().getCaseName(), state);
            lib.createProfile(p, CcdServiceCode.ST_DD.name(), CcdServiceCode.ST_DD.getCaseType().getCaseName(), state);
        }

        lib.createRoles(
            "caseworker-sptribs-superuser",
            "caseworker-sptribs-systemupdate",
            "caseworker-sptribs-superuser",
            "caseworker-sptribs-cic-courtadmin",
            "citizen-sptribs-cic-dss",
            "caseworker-st_cic",
            "caseworker-sptribs-cic-caseofficer",
            "caseworker-sptribs-cic-districtregistrar",
            "caseworker-sptribs-cic-districtjudge",
            "caseworker-sptribs-cic-respondent",
            "caseworker",
            "payments",
            "solicitor",
            "solicitor-creator",
            "caseworker-divorce-superuser"
        );
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        var json = IOUtils.toString(resourceLoader.getResource("classpath:cftlib-am-role-assignments.json")
            .getInputStream(), Charset.defaultCharset());
        lib.configureRoleAssignments(json);

        configWriter.generateAllCaseTypesToJSON(new File("build/definitions"));
        // Load the JSON definitions for each caseType.
        lib.importJsonDefinition(new File("build/definitions/"+CcdServiceCode.ST_CIC.getCaseType().getCaseName()));
        lib.importJsonDefinition(new File("build/definitions/"+CcdServiceCode.ST_CS.getCaseType().getCaseName()));
        lib.importJsonDefinition(new File("build/definitions/"+CcdServiceCode.ST_DD.getCaseType().getCaseName()));
        lib.importJsonDefinition(new File("build/definitions/"+CcdServiceCode.ST_MH.getCaseType().getCaseName()));
        lib.importJsonDefinition(new File("build/definitions/"+CcdServiceCode.ST_PHL.getCaseType().getCaseName()));
        lib.importJsonDefinition(new File("build/definitions/"+CcdServiceCode.ST_SEN.getCaseType().getCaseName()));
    }
}
