package uk.gov.hmcts.sptribs.send35.search;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.SearchField;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.send35.model.StSend35CaseData;
import uk.gov.hmcts.sptribs.send35.model.StSend35State;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.send35.StSend35TestUtil.ccdFieldIds;
import static uk.gov.hmcts.sptribs.send35.StSend35TestUtil.configBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getSearchResultFields;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getWorkBasketInputFields;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getWorkBasketResultFields;

class StSend35SearchFieldsTest {

    /** Metadata fields CCD provides; they are not questions on the case data. */
    private static final Set<String> METADATA = Set.of("[CASE_REFERENCE]", "[STATE]");

    @Test
    void shouldDeclareAllFourLayouts() throws IllegalAccessException {
        final ConfigBuilderImpl<StSend35CaseData, StSend35State, UserRole> builder = configure();

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

    private ConfigBuilderImpl<StSend35CaseData, StSend35State, UserRole> configure() {
        final ConfigBuilderImpl<StSend35CaseData, StSend35State, UserRole> builder = configBuilder();
        new StSend35SearchFields().configure(builder);
        return builder;
    }
}
