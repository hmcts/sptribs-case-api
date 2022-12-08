package uk.gov.hmcts.sptribs.ciccase;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.RetiredFields;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import java.util.List;

import static uk.gov.hmcts.sptribs.ciccase.search.SearchInputFields.SEARCH_FIELD_LIST;
import static uk.gov.hmcts.sptribs.ciccase.search.SearchResultFields.SEARCH_RESULT_FIELD_LIST;

public final class ConfigBuilderHelper {

    private ConfigBuilderHelper() {

    }

    public static void configureWithMandatoryConfig(ConfigBuilder<? extends CaseData, State, UserRole> configBuilder) {
        // Each case type must define these mandatory bits of config.
        configBuilder.searchInputFields().fields(SEARCH_FIELD_LIST);
        configBuilder.searchResultFields().fields(SEARCH_RESULT_FIELD_LIST);
        configBuilder.workBasketResultFields().fields(SEARCH_RESULT_FIELD_LIST);

        configBuilder.addPreEventHook(RetiredFields::migrate);
        configBuilder.setCallbackHost(System.getenv().getOrDefault("CASE_API_URL", "http://localhost:4013"));
    }

    public static void configure(ConfigBuilder<? extends CaseData, State, UserRole> configBuilder,
                                 List<CCDConfig<CaseData, State, UserRole>> configs) {
        // Apply the configuration of our base case type to our derived type.
        // TODO: Make CCDConfig APIs covariant to avoid this unchecked cast.
        @SuppressWarnings("unchecked")
        var upcast = (ConfigBuilder<CaseData, State, UserRole>)configBuilder;
        for (var config : configs) {
            config.configure(upcast);
        }
    }

    public static void configureWithTestEvent(ConfigBuilder<? extends CaseData, State, UserRole> configBuilder) {
        configBuilder.event("test")
            .forState(State.AwaitingApplicant1Response)
            .name("Test event")
            .fields()
            .mandatory(CaseData::getHearingDate);
    }
}
