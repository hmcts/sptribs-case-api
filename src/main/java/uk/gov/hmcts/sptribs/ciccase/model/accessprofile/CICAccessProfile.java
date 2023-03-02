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
        ConfigBuilderHelper.configureWithMandatoryConfig(configBuilder);


        // This block works as intended and to our understanding of setting up AM/IDAM for legacy operation
        // However if we uncomment this although be case create cases as expected we cannot see them on the case list
        // Commented out until we start to tackle Access Profiles as we're going to need support
        //

        configBuilder.caseRoleToAccessProfile(UserRole.JUDGE)
            .accessProfiles(UserRole.getAccessProfileName(UserRole.SUPER_USER_CIC))
            .caseAccessCategories(UserRole.SUPER_USER_CIC.getCaseTypePermissions());

    }
}
