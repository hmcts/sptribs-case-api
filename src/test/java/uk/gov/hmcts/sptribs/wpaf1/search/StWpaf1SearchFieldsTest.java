package uk.gov.hmcts.sptribs.wpaf1.search;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.SearchField;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.wpaf1.model.StWpaf1CaseData;
import uk.gov.hmcts.sptribs.wpaf1.model.StWpaf1State;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getSearchResultFields;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getWorkBasketInputFields;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getWorkBasketResultFields;
import static uk.gov.hmcts.sptribs.wpaf1.StWpaf1TestUtil.ccdFieldIds;
import static uk.gov.hmcts.sptribs.wpaf1.StWpaf1TestUtil.configBuilder;

class StWpaf1SearchFieldsTest {

    /** Metadata fields CCD provides; they are not questions on the case data. */
    private static final Set<String> METADATA = Set.of("[CASE_REFERENCE]", "[STATE]");

    @Test
    void shouldDeclareAllFourLayouts() throws IllegalAccessException {
        final ConfigBuilderImpl<StWpaf1CaseData, StWpaf1State, UserRole> builder = configure();

        assertThat(getSearchResultFields(builder).getFields())
            .describedAs("the definition store refuses to import a case type with an empty "
                + "layout sheet, so all four have to be declared")
            .isNotEmpty();
        assertThat(getWorkBasketInputFields(builder).getFields()).isNotEmpty();
        assertThat(getWorkBasketResultFields(builder).getFields()).isNotEmpty();
    }

    @Test
    void shouldOnlyReferenceFieldsThatExistOnTheCaseData() throws IllegalAccessException {
        final Set<String> known = ccdFieldIds();

        for (SearchField<UserRole> field : getSearchResultFields(configure()).getFields()) {
            if (METADATA.contains(field.getId())) {
                continue;
            }
            assertThat(known)
                .describedAs("search layouts address fields by id, so '%s' would silently "
                    + "produce an empty column", field.getId())
                .contains(field.getId());
        }
    }

    private ConfigBuilderImpl<StWpaf1CaseData, StWpaf1State, UserRole> configure() {
        final ConfigBuilderImpl<StWpaf1CaseData, StWpaf1State, UserRole> builder = configBuilder();
        new StWpaf1SearchFields().configure(builder);
        return builder;
    }
}
