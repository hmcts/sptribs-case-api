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
import java.util.Map;

@Component
public class CftLibConfig implements CFTLibConfigurer {

    private static final String BUILD_DEFINITIONS = "build/definitions/";
    @Value("Submitted")
    String state;


    @Autowired
    CCDDefinitionGenerator configWriter;

    @Override
    public void configure(CFTLib lib) throws Exception {

        var roleList = List.of("caseworker-sptribs-superuser",
            "caseworker-sptribs-systemupdate",
            "caseworker-sptribs",
            "caseworker-sptribs-cic-courtadmin",
            "citizen-sptribs-cic-dss",
            "caseworker-st_cic",
            "caseworker-sptribs-cic-caseofficer",
            "caseworker-sptribs-cic-districtregistrar",
            "caseworker-sptribs-cic-respondent",
            "caseworker",
            "payments",
            "solicitor");

        var users = Map.of(
            "TEST_CASE_WORKER_USER@mailinator.com", roleList,
            "TEST_SOLICITOR@mailinator.com", roleList);

        for (var p : users.entrySet()) {
            lib.createIdamUser(p.getKey(), p.getValue().toArray(new String[0]));
            lib.createProfile(p.getKey(), CcdJurisdiction.CRIMINAL_INJURIES_COMPENSATION.getJurisdictionId(),
                CcdServiceCode.ST_CIC.getCaseType().getCaseTypeName(), state);
            lib.createProfile(p.getKey(), CcdJurisdiction.CARE_STANDARDS.getJurisdictionId(),
                CcdServiceCode.ST_CS.getCaseType().getCaseTypeName(), state);
            lib.createProfile(p.getKey(), CcdJurisdiction.MENTAL_HEALTH.getJurisdictionId(),
                CcdServiceCode.ST_MH.getCaseType().getCaseTypeName(), state);
            lib.createProfile(p.getKey(), CcdJurisdiction.PRIMARY_HEALTH_LISTS.getJurisdictionId(),
                CcdServiceCode.ST_PHL.getCaseType().getCaseTypeName(), state);
            lib.createProfile(p.getKey(), CcdJurisdiction.SPECIAL_EDUCATIONAL_NEEDS_AND_DISCRIMINATION.getJurisdictionId(),
                CcdServiceCode.ST_SEN.getCaseType().getCaseTypeName(), state);
            lib.createProfile(p.getKey(), CcdJurisdiction.SPECIAL_EDUCATIONAL_NEEDS_AND_DISCRIMINATION.getJurisdictionId(),
                CcdServiceCode.ST_DD.getCaseType().getCaseTypeName(), state);
        }

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
