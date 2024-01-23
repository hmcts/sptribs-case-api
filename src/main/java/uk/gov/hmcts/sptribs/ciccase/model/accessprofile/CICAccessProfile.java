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

        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.SUPER_USER)
            .accessProfiles(UserRolesForAccessProfiles.CIC_SUPER_USER.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_ADMIN.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.ST_CIC_CASEWORKER)
            .accessProfiles(UserRolesForAccessProfiles.CIC_CASEWORKER.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_ADMIN.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.ST_CIC_SENIOR_CASEWORKER)
            .accessProfiles(UserRolesForAccessProfiles.CIC_SENIOR_CASEWORKER.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_ADMIN.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.ST_CIC_HEARING_CENTRE_ADMIN)
            .accessProfiles(UserRolesForAccessProfiles.CIC_CENTRE_ADMIN.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_ADMIN.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.ST_CIC_HEARING_CENTRE_TEAM_LEADER)
            .accessProfiles(UserRolesForAccessProfiles.CIC_CENTRE_TEAM_LEADER.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_ADMIN.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.ST_CIC_SENIOR_JUDGE)
            .accessProfiles(UserRolesForAccessProfiles.CIC_SENIOR_JUDGE.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_VIEWER.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.ST_CIC_JUDGE)
            .accessProfiles(UserRolesForAccessProfiles.CIC_JUDGE.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_VIEWER.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.ST_CIC_RESPONDENT)
            .accessProfiles(UserRolesForAccessProfiles.CIC_RESPONDENT.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.CREATOR)
            .accessProfiles(UserRolesForAccessProfiles.CREATOR.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.CASEWORKER)
            .accessProfiles(UserRolesForAccessProfiles.AC_CASEWORKER.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.CITIZEN_CIC)
            .accessProfiles(UserRolesForAccessProfiles.AC_CITIZEN.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.ST_CIC_WA_CONFIG_USER)
            .accessProfiles(UserRolesForAccessProfiles.AC_ST_CIC_WA_CONFIG_USER.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.SYSTEMUPDATE)
            .accessProfiles(UserRolesForAccessProfiles.AC_SYSTEMUPDATE.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.CASE_OFFICER_CIC)
                .accessProfiles(UserRolesForAccessProfiles.AP_CASE_OFFICER_CIC.getRole())
                .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.COURT_ADMIN_CIC)
                .accessProfiles(UserRolesForAccessProfiles.AP_COURT_ADMIN_CIC.getRole())
                .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.DISTRICT_JUDGE_CIC)
                .accessProfiles(UserRolesForAccessProfiles.AP_DISTRICT_JUDGE_CIC_PROFILE.getRole())
                .build();
        // Role Assignments
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.SENIOR_TRIBUNAL_CASE_WORKER)
                .accessProfiles(UserRolesForAccessProfiles.CIC_SENIOR_CASEWORKER_PROFILE.getRole())
                .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.TRIBUNAL_CASE_WORKER)
                .accessProfiles(UserRolesForAccessProfiles.CIC_CASEWORKER_PROFILE.getRole())
                .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.HEARING_CENTRE_TEAM_LEADER)
                .accessProfiles(UserRolesForAccessProfiles.CIC_CENTRE_TEAM_LEADER_PROFILE.getRole())
                .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.REGIONAL_CENTRE_TEAM_LEADER)
                .accessProfiles(UserRolesForAccessProfiles.CIC_CENTRE_TEAM_LEADER_PROFILE.getRole())
                .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.CTSC_TEAM_LEADER)
                .accessProfiles(UserRolesForAccessProfiles.CIC_CENTRE_TEAM_LEADER_PROFILE.getRole())
                .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.HEARING_CENTRE_ADMIN)
                .accessProfiles(UserRolesForAccessProfiles.CIC_CENTRE_ADMIN_PROFILE.getRole())
                .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.REGIONAL_CENTRE_ADMIN)
                .accessProfiles(UserRolesForAccessProfiles.CIC_CENTRE_ADMIN_PROFILE.getRole())
                .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.CTSC_ADMIN)
                .accessProfiles(UserRolesForAccessProfiles.CIC_CENTRE_ADMIN_PROFILE.getRole())
                .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.CICA)
                .accessProfiles(UserRolesForAccessProfiles.CIC_RESPONDENT_PROFILE.getRole())
                .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.JUDGE)
                .accessProfiles(UserRolesForAccessProfiles.CIC_JUDGE_PROFILE.getRole(),
                        UserRolesForAccessProfiles.AP_DISTRICT_JUDGE_CIC_PROFILE.getRole())
                .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.FEE_PAID_JUDGE)
                .accessProfiles(UserRolesForAccessProfiles.CIC_JUDGE_PROFILE.getRole(),
                        UserRolesForAccessProfiles.AP_DISTRICT_JUDGE_CIC_PROFILE.getRole())
                .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.SENIOR_JUDGE)
                .accessProfiles(UserRolesForAccessProfiles.CIC_SENIOR_JUDGE.getRole(),
                        UserRolesForAccessProfiles.CIC_JUDGE_PROFILE.getRole(),
                        UserRolesForAccessProfiles.AP_DISTRICT_JUDGE_CIC_PROFILE.getRole())
                .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.LEADERSHIP_JUDGE)
                .accessProfiles(UserRolesForAccessProfiles.CIC_SENIOR_JUDGE.getRole(),
                        UserRolesForAccessProfiles.CIC_JUDGE_PROFILE.getRole(),
                        UserRolesForAccessProfiles.AP_DISTRICT_JUDGE_CIC_PROFILE.getRole())
                .build();
    }
}
