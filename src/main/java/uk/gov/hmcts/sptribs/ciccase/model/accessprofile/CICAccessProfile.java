package uk.gov.hmcts.sptribs.ciccase.model.accessprofile;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRolesForAccessProfiles;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.CriminalInjuriesCompensationData;

@Component
@Slf4j
public class CICAccessProfile implements CCDConfig<CriminalInjuriesCompensationData, State, UserRolesForAccessProfiles> {

    @Override
    public void configure(final ConfigBuilder<CriminalInjuriesCompensationData, State, UserRolesForAccessProfiles> configBuilder) {
        //ConfigBuilderHelper.configureWithMandatoryConfig(configBuilder);

        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.SUPER_USER)
            .accessProfiles(UserRolesForAccessProfiles.CIC_SUPER_USER.getRole())
            .caseAccessCategories(UserRolesForAccessProfiles.CIC_SUPER_USER.getCaseTypePermissions()).build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.ST_CIC_CASEWORKER)
            .accessProfiles(UserRolesForAccessProfiles.CIC_CASEWORKER.getRole())
            .caseAccessCategories(UserRolesForAccessProfiles.CIC_CASEWORKER.getCaseTypePermissions()).build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.ST_CIC_SENIOR_CASEWORKER)
            .accessProfiles(UserRolesForAccessProfiles.CIC_SENIOR_CASEWORKER.getRole())
            .caseAccessCategories(UserRolesForAccessProfiles.CIC_SENIOR_CASEWORKER.getCaseTypePermissions()).build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.ST_CIC_HEARING_CENTRE_ADMIN)
            .accessProfiles(UserRolesForAccessProfiles.CIC_CENTRE_ADMIN.getRole())
            .caseAccessCategories(UserRolesForAccessProfiles.CIC_CENTRE_ADMIN.getCaseTypePermissions()).build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.ST_CIC_HEARING_CENTRE_TEAM_LEADER)
            .accessProfiles(UserRolesForAccessProfiles.CIC_CENTRE_TEAM_LEADER.getRole())
            .caseAccessCategories(UserRolesForAccessProfiles.CIC_CENTRE_TEAM_LEADER.getCaseTypePermissions()).build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.ST_CIC_SENIOR_JUDGE)
            .accessProfiles(UserRolesForAccessProfiles.CIC_SENIOR_JUDGE.getRole())
            .caseAccessCategories(UserRolesForAccessProfiles.CIC_SENIOR_JUDGE.getCaseTypePermissions()).build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.ST_CIC_JUDGE)
            .accessProfiles(UserRolesForAccessProfiles.CIC_JUDGE.getRole())
            .caseAccessCategories(UserRolesForAccessProfiles.CIC_JUDGE.getCaseTypePermissions()).build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.ST_CIC_RESPONDENT)
            .accessProfiles(UserRolesForAccessProfiles.CIC_RESPONDENT.getRole())
            .caseAccessCategories(UserRolesForAccessProfiles.CIC_RESPONDENT.getCaseTypePermissions()).build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.CREATOR)
            .accessProfiles(UserRolesForAccessProfiles.CREATOR.getRole())
            .caseAccessCategories(UserRolesForAccessProfiles.CREATOR.getCaseTypePermissions()).build();
        /*configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.SOLICITOR)
            .accessProfiles(UserRolesForAccessProfiles.SOLICITOR.getRole())
            .caseAccessCategories(UserRolesForAccessProfiles.SOLICITOR.getCaseTypePermissions()).build();*/

        /*
        // This block works as intended and to our understanding of setting up AM/IDAM for legacy operation
        // However if we uncomment this although be case create cases as expected we cannot see them on the case list
        // Commented out until we start to tackle Access Profiles as we're going to need support
        //
        configBuilder.caseRoleToAccessProfile(UserRole.CIC_SUPER_USER)
            .accessProfiles(UserRole.getAccessProfileName(UserRole.CIC_SUPER_USER))
            .caseAccessCategories(UserRole.CIC_SUPER_USER.getCaseTypePermissions()).legacyIdamRole();
        configBuilder.caseRoleToAccessProfile(UserRole.SYSTEMUPDATE)
            .accessProfiles(UserRole.getAccessProfileName(UserRole.SYSTEMUPDATE))
            .caseAccessCategories(UserRole.SYSTEMUPDATE.getCaseTypePermissions()).legacyIdamRole();
        configBuilder.caseRoleToAccessProfile(UserRole.SOLICITOR)
            .accessProfiles(UserRole.getAccessProfileName(UserRole.SOLICITOR))
            .caseAccessCategories(UserRole.SOLICITOR.getCaseTypePermissions()).legacyIdamRole();
        configBuilder.caseRoleToAccessProfile(UserRole.COURT_ADMIN_CIC)
            .accessProfiles(UserRole.getAccessProfileName(UserRole.COURT_ADMIN_CIC))
            .caseAccessCategories(UserRole.COURT_ADMIN_CIC.getCaseTypePermissions()).legacyIdamRole();
        configBuilder.caseRoleToAccessProfile(UserRole.CASE_OFFICER_CIC)
            .accessProfiles(UserRole.getAccessProfileName(UserRole.CASE_OFFICER_CIC))
            .caseAccessCategories(UserRole.CASE_OFFICER_CIC.getCaseTypePermissions()).legacyIdamRole();
        configBuilder.caseRoleToAccessProfile(UserRole.DISTRICT_REGISTRAR_CIC)
            .accessProfiles(UserRole.getAccessProfileName(UserRole.DISTRICT_REGISTRAR_CIC))
            .caseAccessCategories(UserRole.DISTRICT_REGISTRAR_CIC.getCaseTypePermissions()).legacyIdamRole();
        configBuilder.caseRoleToAccessProfile(UserRole.DISTRICT_JUDGE_CIC)
            .accessProfiles(UserRole.getAccessProfileName(UserRole.DISTRICT_JUDGE_CIC))
            .caseAccessCategories(UserRole.DISTRICT_JUDGE_CIC.getCaseTypePermissions()).legacyIdamRole();
        configBuilder.caseRoleToAccessProfile(UserRole.RESPONDENT_CIC)
            .accessProfiles(UserRole.getAccessProfileName(UserRole.RESPONDENT_CIC))
            .caseAccessCategories(UserRole.RESPONDENT_CIC.getCaseTypePermissions()).legacyIdamRole();
        configBuilder.caseRoleToAccessProfile(UserRole.CITIZEN_CIC)
            .accessProfiles(UserRole.getAccessProfileName(UserRole.CITIZEN_CIC))
            .caseAccessCategories(UserRole.CITIZEN_CIC.getCaseTypePermissions()).legacyIdamRole();
        configBuilder.caseRoleToAccessProfile(UserRole.CREATOR)
            .accessProfiles(UserRole.getAccessProfileName(UserRole.CREATOR))
            .caseAccessCategories(UserRole.CREATOR.getCaseTypePermissions());
         */
    }
}
