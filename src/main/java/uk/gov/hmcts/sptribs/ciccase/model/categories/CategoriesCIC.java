package uk.gov.hmcts.sptribs.ciccase.model.categories;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.ConfigBuilderHelper;
import uk.gov.hmcts.sptribs.ciccase.CriminalInjuriesCompensation;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.CriminalInjuriesCompensationData;

@Component
@Slf4j
public class CategoriesCIC extends CriminalInjuriesCompensation {

    @Override
    public void configure(final ConfigBuilder<CriminalInjuriesCompensationData, State, UserRole> configBuilder) {
        ConfigBuilderHelper.configureWithMandatoryConfig(configBuilder);

        configBuilder.categories(UserRole.SUPER_USER)
            .categoryID("A")
            .categoryLabel("Application (A Docs)")
            .displayOrder(1)
            .build();
        configBuilder.categories(UserRole.SUPER_USER)
            .categoryID("TD")
            .categoryLabel("Tribunal direction / decision notices (TD)")
            .displayOrder(2)
            .build();
        configBuilder.categories(UserRole.SUPER_USER)
            .categoryID("B")
            .categoryLabel("Police evidence (B Docs)")
            .displayOrder(3)
            .build();
        configBuilder.categories(UserRole.SUPER_USER)
            .categoryID("C")
            .categoryLabel("Medical evidence (C Docs)")
            .displayOrder(4)
            .build();
        configBuilder.categories(UserRole.SUPER_USER)
            .categoryID("D")
            .categoryLabel("Loss of earnings / financial information (D docs)")
            .displayOrder(5)
            .build();
        configBuilder.categories(UserRole.SUPER_USER)
            .categoryID("E")
            .categoryLabel("Care documents (E docs)")
            .displayOrder(6)
            .build();
        configBuilder.categories(UserRole.SUPER_USER)
            .categoryID("L")
            .categoryLabel("Linked documents from a linked / past case (L docs)")
            .displayOrder(7)
            .build();
        configBuilder.categories(UserRole.SUPER_USER)
            .categoryID("S")
            .categoryLabel("Statements (S docs)")
            .displayOrder(8)
            .build();
        configBuilder.categories(UserRole.SUPER_USER)
            .categoryID("TG")
            .categoryLabel("General evidence (TG)")
            .displayOrder(9)
            .build();

    }
}
