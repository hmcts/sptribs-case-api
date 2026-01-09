package uk.gov.hmcts.sptribs.ciccase.model.categories;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.CriminalInjuriesCompensationData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCICCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getCategories;

public class CategoriesCICTest {

    @Test
    void shouldConfigureCategoriesForCIC() throws IllegalAccessException {
        //Given
        final ConfigBuilder<CriminalInjuriesCompensationData, State, UserRole> configBuilder = createCICCaseDataConfigBuilder();

        //When
        new CategoriesCIC().configure(configBuilder);

        //Then
        assertThat(getCategories(configBuilder)).hasSize(11);
    }
}
