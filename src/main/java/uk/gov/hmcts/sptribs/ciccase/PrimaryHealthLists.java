package uk.gov.hmcts.sptribs.ciccase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.RetiredFields;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.PrimaryHealthListsData;
import uk.gov.hmcts.sptribs.common.ccd.CcdCaseType;
import uk.gov.hmcts.sptribs.common.ccd.CcdServiceCode;

import static uk.gov.hmcts.sptribs.ciccase.search.SearchInputFields.SEARCH_FIELD_LIST;
import static uk.gov.hmcts.sptribs.ciccase.search.SearchResultFields.SEARCH_RESULT_FIELD_LIST;


@Component
@Slf4j
public class PrimaryHealthLists implements CCDConfig<PrimaryHealthListsData, State, UserRole> {

    public static final String CASE_TYPE = "PHL";
    public static final String JURISDICTION = "DIVORCE";

    @Override
    public void configure(final ConfigBuilder<PrimaryHealthListsData, State, UserRole> configBuilder) {
        // Each case type must define these mandatory bits of config.
        configBuilder.searchInputFields().fields(SEARCH_FIELD_LIST);
        configBuilder.searchResultFields().fields(SEARCH_RESULT_FIELD_LIST);
        configBuilder.workBasketResultFields().fields(SEARCH_RESULT_FIELD_LIST);

        configBuilder.addPreEventHook(RetiredFields::migrate);
        configBuilder.setCallbackHost(System.getenv().getOrDefault("CASE_API_URL", "http://localhost:4013"));

        configBuilder.caseType(CcdCaseType.PHL.name(), "PHL Case Type", CcdCaseType.PHL.getDescription());
        configBuilder.jurisdiction(JURISDICTION, CASE_TYPE, CcdServiceCode.ST_PHL.getCcdServiceDescription());

        configBuilder.event("test")
            .forState(State.AwaitingApplicant1Response)
            .name("Test event")
            .fields()
            .mandatory(PrimaryHealthListsData::getHearingDate);


        // to shutter the service within xui uncomment this line
        // configBuilder.shutterService();
        log.info("Building definition for " + System.getenv().getOrDefault("ENVIRONMENT", ""));
    }
}
