package uk.gov.hmcts.sptribs.ciccase.model.accessprofile;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.ConfigBuilderHelper;
import uk.gov.hmcts.sptribs.ciccase.CriminalInjuriesCompensation;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.CriminalInjuriesCompensationData;

@Component
@Slf4j
public class CICAccessProfile extends CriminalInjuriesCompensation {

    @Override
    public void configure(final ConfigBuilder<CriminalInjuriesCompensationData, State, UserRole> configBuilder) {
        //ConfigBuilderHelper.configureWithMandatoryConfig(configBuilder);

        configBuilder.caseRoleToAccessProfile(UserRole.CIC_SUPER_USER)
            .accessProfiles("cic-superuser")
            .caseAccessCategories(UserRole.CIC_SUPER_USER.getCaseTypePermissions()).build();
        configBuilder.caseRoleToAccessProfile(UserRole.CIC_CASEWORKER)
            .accessProfiles("cic-caseworker")
            .caseAccessCategories(UserRole.CIC_CASEWORKER.getCaseTypePermissions()).build();
        configBuilder.caseRoleToAccessProfile(UserRole.CIC_SENIOR_CASEWORKER)
            .accessProfiles("cic-senior-caseworker")
            .caseAccessCategories(UserRole.CIC_SENIOR_CASEWORKER.getCaseTypePermissions()).build();
        configBuilder.caseRoleToAccessProfile(UserRole.CIC_CENTRE_ADMIN)
            .accessProfiles("cic-centre-admin")
            .caseAccessCategories(UserRole.CIC_CENTRE_ADMIN.getCaseTypePermissions()).build();
        configBuilder.caseRoleToAccessProfile(UserRole.CIC_CENTRE_TEAM_LEADER)
            .accessProfiles("cic-centre-team-leader")
            .caseAccessCategories(UserRole.CIC_CENTRE_TEAM_LEADER.getCaseTypePermissions()).build();
        configBuilder.caseRoleToAccessProfile(UserRole.CIC_SENIOR_JUDGE)
            .accessProfiles("cic-senior-judge")
            .caseAccessCategories(UserRole.CIC_SENIOR_JUDGE.getCaseTypePermissions()).build();
        configBuilder.caseRoleToAccessProfile(UserRole.CIC_JUDGE)
            .accessProfiles("cic-judge")
            .caseAccessCategories(UserRole.CIC_JUDGE.getCaseTypePermissions()).build();
        configBuilder.caseRoleToAccessProfile(UserRole.CIC_RESPONDENT)
            .accessProfiles("cic-respondent")
            .caseAccessCategories(UserRole.CIC_RESPONDENT.getCaseTypePermissions()).build();

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
