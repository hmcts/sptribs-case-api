package uk.gov.hmcts.sptribs.ciccase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.RetiredFields;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.SpecialEducationalNeedsData;
import uk.gov.hmcts.sptribs.common.ccd.CcdCaseType;
import uk.gov.hmcts.sptribs.common.ccd.CcdServiceCode;

import java.util.List;

import static uk.gov.hmcts.sptribs.ciccase.search.SearchInputFields.SEARCH_FIELD_LIST;
import static uk.gov.hmcts.sptribs.ciccase.search.SearchResultFields.SEARCH_RESULT_FIELD_LIST;


@Component
@Slf4j
public class SpecialEducationalNeeds implements CCDConfig<SpecialEducationalNeedsData, State, UserRole> {

    public static final String JURISDICTION_NAME = "SEND";
    public static final String JURISDICTION = "ST_SEND";

    @Autowired
    private List<CCDConfig<CaseData, State, UserRole>> cfgs;

    @Override
    public void configure(final ConfigBuilder<SpecialEducationalNeedsData, State, UserRole> configBuilder) {
        ConfigBuilderHelper.configureWithMandatoryConfig(configBuilder);

        configBuilder.caseType(CcdCaseType.SEN.name(), "SEN Case Type", CcdCaseType.SEN.getDescription());
        configBuilder.jurisdiction(JURISDICTION, JURISDICTION_NAME, CcdServiceCode.ST_SEND.getCcdServiceDescription());

        ConfigBuilderHelper.configure(configBuilder, cfgs);

        ConfigBuilderHelper.configureWithTestEvent(configBuilder);

        // to shutter the service within xui uncomment this line
        // configBuilder.shutterService();
        log.info("Building definition for " + System.getenv().getOrDefault("ENVIRONMENT", ""));
    }
}
