package uk.gov.hmcts.sptribs.ciccase.model.categories;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.MentalHealthData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createMentalHealthDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getCategories;

public class CategoriesMHtest {

    @Test
    void shouldConfigureCategoriesForMH() throws IllegalAccessException {
        //Given
        final ConfigBuilder<MentalHealthData, State, UserRole> configBuilder = createMentalHealthDataConfigBuilder();

        //When
        new CategoriesMH().configure(configBuilder);

        //Then
        assertThat(getCategories(configBuilder)).hasSize(9);
    }
}
