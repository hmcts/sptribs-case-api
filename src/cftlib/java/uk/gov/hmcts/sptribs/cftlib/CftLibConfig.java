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

        List<String> roleList = List.of("caseworker",
            "caseworker-st_cic",
            "caseworker-st_cic-caseworker",
            "pui-case-manager",
            "jrd-admin");

        Map<String, List<String>> users = Map.of(
            "TEST_CASE_WORKER_USER@mailinator.com", roleList,
            "TEST_SOLICITOR@mailinator.com", roleList,
            // The citizen-facing journeys need a citizen to sign in as. Without
            // one, only caseworker paths are exercisable locally.
            "TEST_CITIZEN_USER@mailinator.com", List.of("citizen"));

        for (Map.Entry<String, List<String>> p : users.entrySet()) {
            lib.createIdamUser(p.getKey(), p.getValue().toArray(new String[0]));
            // CCD needs a profile row per (user, jurisdiction, case type), so
            // every user gets one for every case type this service registers.
            for (CcdServiceCode serviceCode : CcdServiceCode.values()) {
                lib.createProfile(p.getKey(), serviceCode.getJurisdiction().getJurisdictionId(),
                    serviceCode.getCaseType().getCaseTypeName(), state);
            }
        }

        lib.createRoles(
            "caseworker-sptribs-superuser",
            "caseworker-sptribs-systemupdate",
            "caseworker-sptribs",
            "caseworker-st_cic",
            "caseworker-sptribs-cic-districtjudge",
            "caseworker-sptribs-cic-respondent",
            "caseworker",
            "solicitor",
            "pui-case-manager",
            "pui-finance-manager",
            "pui-organisation-manager",
            "pui-user-manager",
            "jrd-admin",
            "caseworker-st_cic-caseworker",
            "caseworker-st_cic-senior-caseworker",
            "caseworker-st_cic-hearing-centre-admin",
            "caseworker-st_cic-hearing-centre-team-leader",
            "caseworker-st_cic-senior-judge",
            "caseworker-st_cic-judge",
            "caseworker-st_cic-respondent",
            "caseflags-admin",
            "caseflags-viewer",
            "citizen",
            "caseworker-wa-task-configuration",
            "GS_profile",
            "caseworker-ras-validation",
            "non-respondent-user"
        );

        ResourceLoader resourceLoader = new DefaultResourceLoader();
        String json = IOUtils.toString(resourceLoader.getResource("classpath:cftlib-am-role-assignments.json")
            .getInputStream(), Charset.defaultCharset());
        lib.configureRoleAssignments(json);

        configWriter.generateAllCaseTypesToJSON(new File(BUILD_DEFINITIONS));
        // One definition directory per case type, named after the case type id.
        for (CcdServiceCode serviceCode : CcdServiceCode.values()) {
            lib.importJsonDefinition(new File(BUILD_DEFINITIONS + serviceCode.getCaseType().getCaseTypeName()));
        }
    }

}
