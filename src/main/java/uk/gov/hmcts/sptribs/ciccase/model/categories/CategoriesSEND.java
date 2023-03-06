package uk.gov.hmcts.sptribs.ciccase.model.categories;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.ConfigBuilderHelper;
import uk.gov.hmcts.sptribs.ciccase.SpecialEducationalNeeds;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.SpecialEducationalNeedsData;

@Component
@Slf4j
public class CategoriesSEND extends SpecialEducationalNeeds {

    @Override
    public void configure(final ConfigBuilder<SpecialEducationalNeedsData, State, UserRole> configBuilder) {
        ConfigBuilderHelper.configureWithMandatoryConfig(configBuilder);

        configBuilder.categories(UserRole.SUPER_USER)
            .categoryID("A")
            .categoryLabel("Application (A Docs)")
            .displayOrder(1)
            .build();
    }
}
