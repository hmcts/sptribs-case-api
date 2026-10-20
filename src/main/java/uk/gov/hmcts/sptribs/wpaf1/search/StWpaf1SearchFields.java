package uk.gov.hmcts.sptribs.wpaf1.search;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.SearchField;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.wpaf1.model.StWpaf1CaseData;
import uk.gov.hmcts.sptribs.wpaf1.model.StWpaf1State;

import java.util.List;

/**
 * Search and work-basket layouts for the WPAF1 case type.
 *
 * <p>All four are required: the definition store refuses to import a case type with an empty
 * layout sheet.
 *
 * <p>The columns are what identifies an appeal in a list — the appellant's name, their
 * National Insurance number, which is the decision reference in practice, and the
 * decision-letter date, which the 12-month time limit runs from.
 */
@Component
public class StWpaf1SearchFields implements CCDConfig<StWpaf1CaseData, StWpaf1State, UserRole> {

    private static final List<SearchField<UserRole>> FIELDS = List.of(
        SearchField.<UserRole>builder().id("[CASE_REFERENCE]").label("Case number").build(),
        SearchField.<UserRole>builder().id("[STATE]").label("Appeal status").build(),
        SearchField.<UserRole>builder().id("appLastName").label("Appellant's last name").build(),
        SearchField.<UserRole>builder().id("appFirstName").label("Appellant's first name").build(),
        SearchField.<UserRole>builder().id("appNationalInsuranceNumber").label("National Insurance number").build(),
        SearchField.<UserRole>builder().id("dsnDecisionLetterDate").label("Date of decision letter").build()
    );

    @Override
    public void configure(final ConfigBuilder<StWpaf1CaseData, StWpaf1State, UserRole> configBuilder) {
        configBuilder.searchInputFields().fields(FIELDS);
        configBuilder.searchResultFields().fields(FIELDS);
        configBuilder.workBasketInputFields().fields(FIELDS);
        configBuilder.workBasketResultFields().fields(FIELDS);
    }
}
