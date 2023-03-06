package uk.gov.hmcts.sptribs.ciccase.model.categories;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.ConfigBuilderHelper;
import uk.gov.hmcts.sptribs.ciccase.MentalHealth;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.MentalHealthData;

@Component
@Slf4j
public class CategoriesMH extends MentalHealth {

    @Override
    public void configure(final ConfigBuilder<MentalHealthData, State, UserRole> configBuilder) {
        ConfigBuilderHelper.configureWithMandatoryConfig(configBuilder);

        configBuilder.categories(UserRole.SUPER_USER)
            .categoryID("A")
            .categoryLabel("Application (A Docs)")
            .displayOrder(1)
            .build();
    }
}
