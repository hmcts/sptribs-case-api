package uk.gov.hmcts.sptribs.ciccase;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import java.util.List;

import static uk.gov.hmcts.sptribs.ciccase.search.SearchInputFields.SEARCH_FIELD_LIST;
import static uk.gov.hmcts.sptribs.ciccase.search.SearchResultFields.SEARCH_RESULT_FIELD_LIST;

public final class ConfigBuilderHelper {

    private ConfigBuilderHelper() {

    }

    public static void configureCategories(ConfigBuilder<? extends CaseData, State, UserRole> configBuilder) {
        configBuilder.categories(UserRole.SUPER_USER)
            .categoryID("A")
            .categoryLabel("Application (A Docs)")
            .displayOrder(1)
            .build();
        configBuilder.categories(UserRole.SUPER_USER)
            .categoryID("TD")
            .categoryLabel("Tribunal direction / decision notices (TD)")
            .displayOrder(2)
            .build();
        configBuilder.categories(UserRole.SUPER_USER)
            .categoryID("B")
            .categoryLabel("Police evidence (B Docs)")
            .displayOrder(3)
            .build();
        configBuilder.categories(UserRole.SUPER_USER)
            .categoryID("C")
            .categoryLabel("Medical evidence (C Docs)")
            .displayOrder(4)
            .build();
        configBuilder.categories(UserRole.SUPER_USER)
            .categoryID("D")
            .categoryLabel("Loss of earnings / financial information (D docs)")
            .displayOrder(5)
            .build();
        configBuilder.categories(UserRole.SUPER_USER)
            .categoryID("E")
            .categoryLabel("Care documents (E docs)")
            .displayOrder(6)
            .build();
        configBuilder.categories(UserRole.SUPER_USER)
            .categoryID("L")
            .categoryLabel("Linked documents from a linked / past case (L docs)")
            .displayOrder(7)
            .build();
        configBuilder.categories(UserRole.SUPER_USER)
            .categoryID("S")
            .categoryLabel("Statements (S docs)")
            .displayOrder(8)
            .build();
        configBuilder.categories(UserRole.SUPER_USER)
            .categoryID("TG")
            .categoryLabel("General evidence (TG)")
            .displayOrder(9)
            .build();
        configBuilder.categories(UserRole.SUPER_USER)
            .categoryID("DSS")
            .categoryLabel("Citizen generated case (DSS)")
            .displayOrder(10)
            .build();
        configBuilder.categories(UserRole.SUPER_USER)
            .categoryID("CD")
            .categoryLabel("Correspondence documents (CD)")
            .displayOrder(11)
            .build();
    }

    public static void configureWithMandatoryConfig(ConfigBuilder<? extends CaseData, State, UserRole> configBuilder) {
        // Each case type must define these mandatory bits of config.
        configBuilder.searchInputFields().fields(SEARCH_FIELD_LIST);
        configBuilder.searchResultFields().fields(SEARCH_RESULT_FIELD_LIST);
        configBuilder.workBasketResultFields().fields(SEARCH_RESULT_FIELD_LIST);

        configBuilder.setCallbackHost(System.getenv().getOrDefault("CASE_API_URL", "http://localhost:4013"));
    }

    public static void configure(ConfigBuilder<? extends CaseData, State, UserRole> configBuilder,
                                 List<CCDConfig<CaseData, State, UserRole>> configs) {
        // Apply the configuration of our base case type to our derived type.
        // CCDGenerator to do in future: Make CCDConfig APIs covariant to avoid this unchecked cast.
        @SuppressWarnings("unchecked")
        final ConfigBuilder<CaseData, State, UserRole> upcast = (ConfigBuilder<CaseData, State, UserRole>) configBuilder;
        for (CCDConfig<CaseData, State, UserRole> config : configs) {
            config.configure(upcast);
        }
    }

    public static void configureWithTestEvent(ConfigBuilder<? extends CaseData, State, UserRole> configBuilder) {
        configBuilder.event("test")
            .forState(State.Draft)
            .name("Test event")
            .fields()
            .mandatory(CaseData::getHearingDate);
    }
}
