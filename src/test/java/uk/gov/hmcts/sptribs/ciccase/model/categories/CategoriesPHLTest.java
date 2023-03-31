package uk.gov.hmcts.sptribs.ciccase.model.categories;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.PrimaryHealthListsData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createPrimaryHealthListsDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getCategories;

public class CategoriesPHLTest {

    @Test
    void shouldConfigureCategoriesForPHL() throws IllegalAccessException {
        //Given
        final ConfigBuilder<PrimaryHealthListsData, State, UserRole> configBuilder = createPrimaryHealthListsDataConfigBuilder();

        //When
        new CategoriesPHL().configure(configBuilder);

        //Then
        assertThat(getCategories(configBuilder)).hasSize(9);
    }
}
