package uk.gov.hmcts.sptribs.ciccase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.DisabilityDiscriminationData;
import uk.gov.hmcts.sptribs.common.ccd.CcdJurisdiction;
import uk.gov.hmcts.sptribs.common.ccd.CcdServiceCode;

import java.util.List;




@Component
@Slf4j
public class DisabilityDiscrimination implements CCDConfig<DisabilityDiscriminationData, State, UserRole> {

    @Autowired
    private List<CCDConfig<CaseData, State, UserRole>> cfgs;

    @Override
    public void configure(final ConfigBuilder<DisabilityDiscriminationData, State, UserRole> configBuilder) {
        ConfigBuilderHelper.configureWithMandatoryConfig(configBuilder);

        configBuilder.caseType(
            CcdServiceCode.ST_DD.getCaseType().getCaseTypeName(),
            CcdServiceCode.ST_DD.getCaseType().getCaseTypeAcronym(),
            CcdServiceCode.ST_DD.getCaseType().getDescription());

        configBuilder.jurisdiction(CcdJurisdiction.SPECIAL_EDUCATIONAL_NEEDS_AND_DISCRIMINATION.getJurisdictionId(),
            CcdJurisdiction.SPECIAL_EDUCATIONAL_NEEDS_AND_DISCRIMINATION.getJurisdictionName(),
            CcdServiceCode.ST_DD.getCcdServiceDescription());

        ConfigBuilderHelper.configure(configBuilder, cfgs);

        ConfigBuilderHelper.configureWithTestEvent(configBuilder);

        // to shutter the service within xui, uncomment this line
        // configBuilder.shutterService();
        log.info("Building definition for " + System.getenv().getOrDefault("ENVIRONMENT", ""));
    }
}
