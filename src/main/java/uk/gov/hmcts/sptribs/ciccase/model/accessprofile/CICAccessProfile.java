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

    }
}
