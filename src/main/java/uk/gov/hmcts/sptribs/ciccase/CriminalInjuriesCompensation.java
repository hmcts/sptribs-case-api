package uk.gov.hmcts.sptribs.ciccase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.CriminalInjuriesCompensationData;
import uk.gov.hmcts.sptribs.common.ccd.CcdJurisdiction;
import uk.gov.hmcts.sptribs.common.ccd.CcdServiceCode;

import java.util.List;


@Component
@Slf4j
public class CriminalInjuriesCompensation implements CCDConfig<CriminalInjuriesCompensationData, State, UserRole> {

    @Autowired
    private List<CCDConfig<CaseData, State, UserRole>> cfgs;

    @Override
    public void configure(final ConfigBuilder<CriminalInjuriesCompensationData, State, UserRole> configBuilder) {
        ConfigBuilderHelper.configureWithMandatoryConfig(configBuilder);

        configBuilder.caseType(
            CcdServiceCode.ST_CIC.getCaseType().getCaseTypeName(),
            CcdServiceCode.ST_CIC.getCaseType().getCaseTypeAcronym(),
            CcdServiceCode.ST_CIC.getCaseType().getDescription());

        configBuilder.jurisdiction(CcdJurisdiction.CRIMINAL_INJURIES_COMPENSATION.getJurisdictionId(),
            CcdJurisdiction.CRIMINAL_INJURIES_COMPENSATION.getJurisdictionName(), CcdServiceCode.ST_CIC.getCcdServiceDescription());

        //
        //        configBuilder.caseRoleToAccessProfile(UserRole.SUPER_USER)
        //            .accessProfiles(UserRole.getAccessProfileName(UserRole.SUPER_USER))
        //            .caseAccessCategories(UserRole.SUPER_USER.getCaseTypePermissions()).legacyIdamRole();
        //        configBuilder.caseRoleToAccessProfile(UserRole.SYSTEMUPDATE)
        //            .accessProfiles(UserRole.getAccessProfileName(UserRole.SYSTEMUPDATE))
        //            .caseAccessCategories(UserRole.SYSTEMUPDATE.getCaseTypePermissions()).legacyIdamRole();
        //        configBuilder.caseRoleToAccessProfile(UserRole.SOLICITOR)
        //            .accessProfiles(UserRole.getAccessProfileName(UserRole.SOLICITOR))
        //            .caseAccessCategories(UserRole.SOLICITOR.getCaseTypePermissions()).legacyIdamRole();
        //        configBuilder.caseRoleToAccessProfile(UserRole.SUPER_USER_CIC)
        //            .accessProfiles(UserRole.getAccessProfileName(UserRole.SUPER_USER_CIC))
        //            .caseAccessCategories(UserRole.SUPER_USER_CIC.getCaseTypePermissions()).legacyIdamRole();
        //        configBuilder.caseRoleToAccessProfile(UserRole.COURT_ADMIN_CIC)
        //            .accessProfiles(UserRole.getAccessProfileName(UserRole.COURT_ADMIN_CIC))
        //            .caseAccessCategories(UserRole.COURT_ADMIN_CIC.getCaseTypePermissions()).legacyIdamRole();
        //        configBuilder.caseRoleToAccessProfile(UserRole.CASE_OFFICER_CIC)
        //            .accessProfiles(UserRole.getAccessProfileName(UserRole.CASE_OFFICER_CIC))
        //            .caseAccessCategories(UserRole.CASE_OFFICER_CIC.getCaseTypePermissions()).legacyIdamRole();
        //        configBuilder.caseRoleToAccessProfile(UserRole.DISTRICT_REGISTRAR_CIC)
        //            .accessProfiles(UserRole.getAccessProfileName(UserRole.DISTRICT_REGISTRAR_CIC))
        //            .caseAccessCategories(UserRole.DISTRICT_REGISTRAR_CIC.getCaseTypePermissions()).legacyIdamRole();
        //        configBuilder.caseRoleToAccessProfile(UserRole.DISTRICT_JUDGE_CIC)
        //            .accessProfiles(UserRole.getAccessProfileName(UserRole.DISTRICT_JUDGE_CIC))
        //            .caseAccessCategories(UserRole.DISTRICT_JUDGE_CIC.getCaseTypePermissions()).legacyIdamRole();
        //        configBuilder.caseRoleToAccessProfile(UserRole.RESPONDENT_CIC)
        //            .accessProfiles(UserRole.getAccessProfileName(UserRole.RESPONDENT_CIC))
        //            .caseAccessCategories(UserRole.RESPONDENT_CIC.getCaseTypePermissions()).legacyIdamRole();
        //        configBuilder.caseRoleToAccessProfile(UserRole.ST_CIC)
        //            .accessProfiles(UserRole.getAccessProfileName(UserRole.ST_CIC))
        //            .caseAccessCategories(UserRole.ST_CIC.getCaseTypePermissions()).legacyIdamRole();
        //        //        configBuilder.caseRoleToAccessProfile(UserRole.HMCTS_STAFF)
        //        //            .accessProfiles(UserRole.getAccessProfileName(UserRole.HMCTS_STAFF))
        //        //            .caseAccessCategories(UserRole.HMCTS_STAFF.getCaseTypePermissions());
        //        //        configBuilder.caseRoleToAccessProfile(UserRole.HMCTS_JUDICIARY)
        //        //            .accessProfiles(UserRole.getAccessProfileName(UserRole.HMCTS_JUDICIARY))
        //        //            .caseAccessCategories(UserRole.HMCTS_JUDICIARY.getCaseTypePermissions());
        //        configBuilder.caseRoleToAccessProfile(UserRole.CITIZEN_CIC)
        //            .accessProfiles(UserRole.getAccessProfileName(UserRole.CITIZEN_CIC))
        //            .caseAccessCategories(UserRole.CITIZEN_CIC.getCaseTypePermissions());


        ConfigBuilderHelper.configure(configBuilder, cfgs);

        ConfigBuilderHelper.configureWithTestEvent(configBuilder);

        // to shutter the service within xui uncomment this line
        // configBuilder.shutterService();
        log.info("Building definition for " + System.getenv().getOrDefault("ENVIRONMENT", ""));
    }
}
