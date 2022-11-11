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

    @Value("ccd-ST_CIC-${CCD_DEF_NAME:dev}.xlsx")
    String cicDefName;

    @Value("ccd-ST_CS-${CCD_DEF_NAME:dev}.xlsx")
    String csDefName;

    @Value("ccd-ST_DD-${CCD_DEF_NAME:dev}.xlsx")
    String ddDefName;

    @Value("ccd-ST_MH-${CCD_DEF_NAME:dev}.xlsx")
    String mhDefName;

    @Value("ccd-ST_PHL-${CCD_DEF_NAME:dev}.xlsx")
    String phlDefName;

    @Value("ccd-ST_SEN-${CCD_DEF_NAME:dev}.xlsx")
    String senDefName;

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
            lib.createProfile(p, CcdServiceCode.CIC.getCcdServiceName(), CcdCaseType.ST_CIC.name(), state);
            lib.createProfile(p, CcdServiceCode.CS.getCcdServiceName(), CcdCaseType.ST_CS.name(), state);
            lib.createProfile(p, CcdServiceCode.MH.getCcdServiceName(), CcdCaseType.ST_MH.name(), state);
            lib.createProfile(p, CcdServiceCode.PHL.getCcdServiceName(), CcdCaseType.ST_PHL.name(), state);
            lib.createProfile(p, CcdServiceCode.SEN.getCcdServiceName(), CcdCaseType.ST_SEN.name(), state);
            lib.createProfile(p, CcdServiceCode.DD.getCcdServiceName(), CcdCaseType.ST_DD.name(), state);
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

        configWriter.generateAllCaseTypesToJSON(new File("build/definitions"));
        lib.importJsonDefinition(new File("build/definitions/ST_CIC"));
    }
}
