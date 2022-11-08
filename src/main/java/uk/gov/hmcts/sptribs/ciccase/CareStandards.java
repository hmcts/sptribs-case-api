package uk.gov.hmcts.sptribs.cscase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.cscase.model.CaseData;
import uk.gov.hmcts.sptribs.cscase.model.RetiredFields;
import uk.gov.hmcts.sptribs.cscase.model.State;
import uk.gov.hmcts.sptribs.cscase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdCaseType;
import uk.gov.hmcts.sptribs.common.ccd.CcdServiceCode;


@Component
@Slf4j
public class CareStandards implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASE_TYPE = "CS";
    public static final String JURISDICTION = "DIVORCE";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.addPreEventHook(RetiredFields::migrate);
        configBuilder.setCallbackHost(System.getenv().getOrDefault("CASE_API_URL", "http://localhost:4013"));

        configBuilder.caseType(CcdCaseType.ST_CS.name(), "CS Case Type", CcdCaseType.ST_CS.getDescription());
        configBuilder.jurisdiction(JURISDICTION, CASE_TYPE, CcdServiceCode.CS.getCcdServiceDescription());


        // to shutter the service within xui uncomment this line
        // configBuilder.shutterService();
        log.info("Building definition for " + System.getenv().getOrDefault("ENVIRONMENT", ""));
    }
}
