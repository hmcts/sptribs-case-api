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
import uk.gov.hmcts.sptribs.common.ccd.CcdJurisdiction;
import uk.gov.hmcts.sptribs.common.ccd.CcdServiceCode;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

@Component
public class CftLibConfig implements CFTLibConfigurer {

    private static final String BUILD_DEFINITIONS = "build/definitions/";
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
            lib.createProfile(p, CcdJurisdiction.CRIMINAL_INJURIES_COMPENSATION.getJurisdictionId(),
                CcdServiceCode.ST_CIC.getCaseType().getCaseTypeName(), state);
            lib.createProfile(p, CcdJurisdiction.CARE_STANDARDS.getJurisdictionId(),
                CcdServiceCode.ST_CS.getCaseType().getCaseTypeName(), state);
            lib.createProfile(p, CcdJurisdiction.MENTAL_HEALTH.getJurisdictionId(),
                CcdServiceCode.ST_MH.getCaseType().getCaseTypeName(), state);
            lib.createProfile(p, CcdJurisdiction.PRIMARY_HEALTH_LISTS.getJurisdictionId(),
                CcdServiceCode.ST_PHL.getCaseType().getCaseTypeName(), state);
            lib.createProfile(p, CcdJurisdiction.SPECIAL_EDUCATIONAL_NEEDS_AND_DISCRIMINATION.getJurisdictionId(),
                CcdServiceCode.ST_SEN.getCaseType().getCaseTypeName(), state);
            lib.createProfile(p, CcdJurisdiction.SPECIAL_EDUCATIONAL_NEEDS_AND_DISCRIMINATION.getJurisdictionId(),
                CcdServiceCode.ST_DD.getCaseType().getCaseTypeName(), state);
        }

        // last 3 roles for divorce below needs to be changed and updated to match to the sptribs roles we have
        lib.createRoles(
            "caseworker-sptribs-superuser",
            "caseworker-sptribs-systemupdate",
            "caseworker-sptribs",
            "caseworker-sptribs-cic-courtadmin",
            "citizen-sptribs-cic-dss",
            "caseworker-st_cic",
            "caseworker-sptribs-cic-caseofficer",
            "caseworker-sptribs-cic-districtregistrar",
            "caseworker-sptribs-cic-districtjudge",
            "caseworker-sptribs-cic-respondent",
            "caseworker",
            "payments",
            "solicitor"
        );
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        var json = IOUtils.toString(resourceLoader.getResource("classpath:cftlib-am-role-assignments.json")
            .getInputStream(), Charset.defaultCharset());
        lib.configureRoleAssignments(json);

        configWriter.generateAllCaseTypesToJSON(new File(BUILD_DEFINITIONS));
        // Load the JSON definitions for each caseType.
        lib.importJsonDefinition(new File(BUILD_DEFINITIONS + CcdServiceCode.ST_CIC.getCaseType().getCaseTypeName()));
        lib.importJsonDefinition(new File(BUILD_DEFINITIONS + CcdServiceCode.ST_CS.getCaseType().getCaseTypeName()));
        lib.importJsonDefinition(new File(BUILD_DEFINITIONS + CcdServiceCode.ST_DD.getCaseType().getCaseTypeName()));
        lib.importJsonDefinition(new File(BUILD_DEFINITIONS + CcdServiceCode.ST_MH.getCaseType().getCaseTypeName()));
        lib.importJsonDefinition(new File(BUILD_DEFINITIONS + CcdServiceCode.ST_PHL.getCaseType().getCaseTypeName()));
        lib.importJsonDefinition(new File(BUILD_DEFINITIONS + CcdServiceCode.ST_SEN.getCaseType().getCaseTypeName()));
    }
}
