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
import uk.gov.hmcts.sptribs.common.ccd.CcdCaseType;
import uk.gov.hmcts.sptribs.common.ccd.CcdServiceCode;

import java.util.List;



@Component
@Slf4j
public class CriminalInjuriesCompensation implements CCDConfig<CriminalInjuriesCompensationData, State, UserRole> {

    public static final String JURISDICTION_NAME = "CIC";
    public static final String JURISDICTION = "ST_CIC";

    @Autowired
    private List<CCDConfig<CaseData, State, UserRole>> cfgs;

    @Override
    public void configure(final ConfigBuilder<CriminalInjuriesCompensationData, State, UserRole> configBuilder) {
        ConfigBuilderHelper.configureWithMandatoryConfig(configBuilder);

        configBuilder.caseType(CcdCaseType.CIC.name(), "CIC Case Type", CcdCaseType.CIC.getDescription());
        configBuilder.jurisdiction(JURISDICTION, JURISDICTION_NAME, CcdServiceCode.ST_CIC.getCcdServiceDescription());

        ConfigBuilderHelper.configure(configBuilder, cfgs);

        ConfigBuilderHelper.configureWithTestEvent(configBuilder);

        // to shutter the service within xui uncomment this line
        // configBuilder.shutterService();
        log.info("Building definition for " + System.getenv().getOrDefault("ENVIRONMENT", ""));
    }
}
