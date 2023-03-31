package uk.gov.hmcts.sptribs.ciccase.model.categories;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.CareStandardsData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCareStandardsDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getCategories;

public class CategoriesCSTest {

    @Test
    void shouldConfigureCategoriesForCS() throws IllegalAccessException {
        //Given
        final ConfigBuilder<CareStandardsData, State, UserRole> configBuilder = createCareStandardsDataConfigBuilder();

        //When
        new CategoriesCS().configure(configBuilder);

        //Then
        assertThat(getCategories(configBuilder)).hasSize(9);
    }
}
