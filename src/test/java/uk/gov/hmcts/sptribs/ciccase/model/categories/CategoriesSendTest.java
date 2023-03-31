package uk.gov.hmcts.sptribs.ciccase.model.categories;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.SpecialEducationalNeedsData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createSpecialEducationalNeedsDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getCategories;

public class CategoriesSendTest {

    @Test
    void shouldConfigureCategoriesForSEND() throws IllegalAccessException {
        //Given
        final ConfigBuilder<SpecialEducationalNeedsData, State, UserRole> configBuilder = createSpecialEducationalNeedsDataConfigBuilder();

        //When
        new CategoriesSEND().configure(configBuilder);

        //Then
        assertThat(getCategories(configBuilder)).hasSize(9);
    }
}
