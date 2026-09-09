package uk.gov.hmcts.sptribs.send35.search;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.SearchField;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.send35.model.StSend35CaseData;
import uk.gov.hmcts.sptribs.send35.model.StSend35State;

import java.util.List;

/**
 * Search and work-basket layouts for the SEND35 case type.
 *
 * <p>All four are required: the definition store refuses to import a case type with an empty
 * layout sheet.
 *
 * <p>The columns are what identifies an appeal in a list — who it is about, who brought it,
 * and the decision-letter date, which is what the two-month time limit runs from.
 */
@Component
public class StSend35SearchFields implements CCDConfig<StSend35CaseData, StSend35State, UserRole> {

    private static final List<SearchField<UserRole>> FIELDS = List.of(
        SearchField.<UserRole>builder().id("[CASE_REFERENCE]").label("Case number").build(),
        SearchField.<UserRole>builder().id("[STATE]").label("Appeal status").build(),
        SearchField.<UserRole>builder().id("cypLastName").label("Child or young person's last name").build(),
        SearchField.<UserRole>builder().id("cypFirstName").label("Child or young person's first name").build(),
        SearchField.<UserRole>builder().id("appellantLastName").label("Appellant's last name").build(),
        SearchField.<UserRole>builder().id("timeDecisionLetterDate").label("Date of decision letter").build()
    );

    @Override
    public void configure(final ConfigBuilder<StSend35CaseData, StSend35State, UserRole> configBuilder) {
        configBuilder.searchInputFields().fields(FIELDS);
        configBuilder.searchResultFields().fields(FIELDS);
        configBuilder.workBasketInputFields().fields(FIELDS);
        configBuilder.workBasketResultFields().fields(FIELDS);
    }
}
