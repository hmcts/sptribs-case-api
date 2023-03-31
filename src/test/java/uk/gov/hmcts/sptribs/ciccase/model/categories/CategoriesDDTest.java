package uk.gov.hmcts.sptribs.ciccase.model.categories;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.DisabilityDiscriminationData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createDisabilityDiscriminationDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getCategories;

public class CategoriesDDTest {

    @Test
    void shouldConfigureCategoriesForDD() throws IllegalAccessException {
        //Given
        final ConfigBuilder<DisabilityDiscriminationData, State, UserRole> configBuilder = createDisabilityDiscriminationDataConfigBuilder();

        //When
        new CategoriesDD().configure(configBuilder);

        //Then
        assertThat(getCategories(configBuilder)).hasSize(9);
    }
}
