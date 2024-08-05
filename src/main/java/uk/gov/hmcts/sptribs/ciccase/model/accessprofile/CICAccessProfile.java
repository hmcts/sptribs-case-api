package uk.gov.hmcts.sptribs.ciccase.model.accessprofile;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRolesForAccessProfiles;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.CriminalInjuriesCompensationData;

@Component
@Slf4j
@Setter
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
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.SYSTEMUPDATE)
            .accessProfiles(UserRolesForAccessProfiles.AC_SYSTEMUPDATE.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_ADMIN.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_HMCTS_STAFF)
            .accessProfiles(UserRolesForAccessProfiles.GS_PROFILE.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_HMCTS_CTSC)
            .accessProfiles(UserRolesForAccessProfiles.GS_PROFILE.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_HMCTS_LEGAL_OPERATIONS)
            .accessProfiles(UserRolesForAccessProfiles.GS_PROFILE.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_HMCTS_ADMIN)
            .accessProfiles(UserRolesForAccessProfiles.GS_PROFILE.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_HMCTS_JUDICIARY)
            .accessProfiles(UserRolesForAccessProfiles.GS_PROFILE.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_ST_SPECIFIC_ACCESS_APPROVER_LEGAL_OPS)
            .accessProfiles(UserRolesForAccessProfiles.GS_PROFILE.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_ST_SPECIFIC_ACCESS_APPROVER_CTSC)
            .accessProfiles(UserRolesForAccessProfiles.GS_PROFILE.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_ST_SPECIFIC_ACCESS_APPROVER_ADMIN)
            .accessProfiles(UserRolesForAccessProfiles.GS_PROFILE.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_ST_SPECIFIC_ACCESS_APPROVER_JUDICIARY)
            .accessProfiles(UserRolesForAccessProfiles.GS_PROFILE.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_ST_TASK_SUPERVISOR)
            .accessProfiles(UserRolesForAccessProfiles.GS_PROFILE.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_ST_CASE_ALLOCATOR)
            .accessProfiles(UserRolesForAccessProfiles.GS_PROFILE.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_ST_SENIOR_TRIBUNAL_CASEWORKER)
            .accessProfiles(UserRolesForAccessProfiles.CIC_SENIOR_CASEWORKER.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_ADMIN.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_ST_TRIBUNAL_CASEWORKER)
            .accessProfiles(UserRolesForAccessProfiles.CIC_CASEWORKER.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_ADMIN.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_ST_REGIONAL_CENTRE_TEAM_LEADER)
            .accessProfiles(UserRolesForAccessProfiles.CIC_CENTRE_TEAM_LEADER.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_ADMIN.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_ST_HEARING_CENTRE_TEAM_LEADER)
            .accessProfiles(UserRolesForAccessProfiles.CIC_CENTRE_TEAM_LEADER.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_ADMIN.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_ST_CTSC_TEAM_LEADER)
            .accessProfiles(UserRolesForAccessProfiles.CIC_CENTRE_TEAM_LEADER.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_ADMIN.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_ST_REGIONAL_CENTRE_ADMIN)
            .accessProfiles(UserRolesForAccessProfiles.CIC_CENTRE_ADMIN.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_ADMIN.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_ST_HEARING_CENTRE_ADMIN)
            .accessProfiles(UserRolesForAccessProfiles.CIC_CENTRE_ADMIN.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_ADMIN.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_ST_CTSC)
            .accessProfiles(UserRolesForAccessProfiles.CIC_CENTRE_ADMIN.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_ADMIN.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_ST_CICA)
            .accessProfiles(UserRolesForAccessProfiles.CIC_RESPONDENT.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_ADMIN.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_ST_SENIOR_JUDGE)
            .accessProfiles(UserRolesForAccessProfiles.CIC_SENIOR_JUDGE.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_VIEWER.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_ST_LEADERSHIP_JUDGE)
            .accessProfiles(UserRolesForAccessProfiles.CIC_SENIOR_JUDGE.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_VIEWER.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_ST_FEE_PAID_JUDGE)
            .accessProfiles(UserRolesForAccessProfiles.CIC_JUDGE.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_VIEWER.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_ST_JUDGE)
            .accessProfiles(UserRolesForAccessProfiles.CIC_JUDGE.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_VIEWER.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_ST_FEE_PAID_TRIBUNAL_MEMBER)
            .accessProfiles(UserRolesForAccessProfiles.CIC_JUDGE.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_VIEWER.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_ST_MEDICAL)
            .accessProfiles(UserRolesForAccessProfiles.CIC_JUDGE.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_VIEWER.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_ST_FEE_PAID_MEDICAL)
            .accessProfiles(UserRolesForAccessProfiles.CIC_JUDGE.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_VIEWER.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_ST_FEE_PAID_DISABILITY)
            .accessProfiles(UserRolesForAccessProfiles.CIC_JUDGE.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_VIEWER.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_ST_FEE_PAID_FINANCIAL)
            .accessProfiles(UserRolesForAccessProfiles.CIC_JUDGE.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_VIEWER.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_ST_ALLOCATED_JUDGE)
            .accessProfiles(UserRolesForAccessProfiles.CIC_JUDGE.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_VIEWER.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_ST_INTERLOC_JUDGE)
            .accessProfiles(UserRolesForAccessProfiles.CIC_JUDGE.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_VIEWER.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_ST_TRIBUNAL_MEMBER1)
            .accessProfiles(UserRolesForAccessProfiles.CIC_JUDGE.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_VIEWER.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_ST_TRIBUNAL_MEMBER2)
            .accessProfiles(UserRolesForAccessProfiles.CIC_JUDGE.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_VIEWER.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_ST_TRIBUNAL_MEMBER3)
            .accessProfiles(UserRolesForAccessProfiles.CIC_JUDGE.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_VIEWER.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_ST_APPRAISER1)
            .accessProfiles(UserRolesForAccessProfiles.CIC_JUDGE.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_VIEWER.getRole())
            .build();
        configBuilder.caseRoleToAccessProfile(UserRolesForAccessProfiles.RAS_ST_APPRAISER2)
            .accessProfiles(UserRolesForAccessProfiles.CIC_JUDGE.getRole(),
                UserRolesForAccessProfiles.AC_CASEFLAGS_VIEWER.getRole())
            .build();
    }
}
