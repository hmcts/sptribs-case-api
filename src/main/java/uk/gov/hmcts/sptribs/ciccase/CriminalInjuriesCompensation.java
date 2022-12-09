package uk.gov.hmcts.sptribs.ciccase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.RetiredFields;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdCaseType;
import uk.gov.hmcts.sptribs.common.ccd.CcdServiceCode;



@Component
@Slf4j
public class CriminalInjuriesCompensation implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASE_TYPE = "CIC";
    public static final String JURISDICTION = "DIVORCE";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.addPreEventHook(RetiredFields::migrate);
        configBuilder.setCallbackHost(System.getenv().getOrDefault("CASE_API_URL", "http://localhost:4013"));

        configBuilder.caseType(CcdCaseType.CIC.name(), "CIC Case Type", CcdCaseType.CIC.getDescription());
        configBuilder.jurisdiction(JURISDICTION, "CIC", CcdServiceCode.CIC.getCcdServiceDescription());


        configBuilder.caseRoleToAccessProfile(UserRole.SUPER_USER).accessProfiles(UserRole.SUPER_USER.getRole()).caseAccessCategories(UserRole.SUPER_USER.getCaseTypePermissions()).legacyIdamRole();
        configBuilder.caseRoleToAccessProfile(UserRole.SYSTEMUPDATE).accessProfiles(UserRole.SYSTEMUPDATE.getRole()).caseAccessCategories(UserRole.SYSTEMUPDATE.getCaseTypePermissions()).legacyIdamRole();
        configBuilder.caseRoleToAccessProfile(UserRole.SOLICITOR).accessProfiles(UserRole.SOLICITOR.getRole()).caseAccessCategories(UserRole.SOLICITOR.getCaseTypePermissions()).legacyIdamRole();
        configBuilder.caseRoleToAccessProfile(UserRole.SUPER_USER_CIC).accessProfiles(UserRole.SUPER_USER_CIC.getRole()).caseAccessCategories(UserRole.SUPER_USER_CIC.getCaseTypePermissions()).legacyIdamRole();
        configBuilder.caseRoleToAccessProfile(UserRole.COURT_ADMIN_CIC).accessProfiles(UserRole.COURT_ADMIN_CIC.getRole()).caseAccessCategories(UserRole.COURT_ADMIN_CIC.getCaseTypePermissions()).legacyIdamRole();
        configBuilder.caseRoleToAccessProfile(UserRole.CASE_OFFICER_CIC).accessProfiles(UserRole.CASE_OFFICER_CIC.getRole()).caseAccessCategories(UserRole.CASE_OFFICER_CIC.getCaseTypePermissions()).legacyIdamRole();
        configBuilder.caseRoleToAccessProfile(UserRole.DISTRICT_REGISTRAR_CIC).accessProfiles(UserRole.DISTRICT_REGISTRAR_CIC.getRole()).caseAccessCategories(UserRole.DISTRICT_REGISTRAR_CIC.getCaseTypePermissions()).legacyIdamRole();
        configBuilder.caseRoleToAccessProfile(UserRole.DISTRICT_JUDGE_CIC).accessProfiles(UserRole.DISTRICT_JUDGE_CIC.getRole()).caseAccessCategories(UserRole.DISTRICT_JUDGE_CIC.getCaseTypePermissions()).legacyIdamRole();
        configBuilder.caseRoleToAccessProfile(UserRole.RESPONDENT_CIC).accessProfiles(UserRole.RESPONDENT_CIC.getRole()).caseAccessCategories(UserRole.RESPONDENT_CIC.getCaseTypePermissions()).legacyIdamRole();
        configBuilder.caseRoleToAccessProfile(UserRole.HMCTS_STAFF).accessProfiles(UserRole.HMCTS_STAFF.getRole()).caseAccessCategories(UserRole.HMCTS_STAFF.getCaseTypePermissions());
        configBuilder.caseRoleToAccessProfile(UserRole.HMCTS_JUDICIARY).accessProfiles(UserRole.HMCTS_JUDICIARY.getRole()).caseAccessCategories(UserRole.HMCTS_JUDICIARY.getCaseTypePermissions());
        configBuilder.caseRoleToAccessProfile(UserRole.CITIZEN_CIC).accessProfiles(UserRole.CITIZEN_CIC.getRole()).caseAccessCategories(UserRole.CITIZEN_CIC.getCaseTypePermissions());




        // to shutter the service within xui uncomment this line
        // configBuilder.shutterService();
        log.info("Building definition for " + System.getenv().getOrDefault("ENVIRONMENT", ""));
    }
}
