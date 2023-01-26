package uk.gov.hmcts.sptribs.ciccase.tab;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

@Component
public class CaseAccessAdministratorSearchTab implements CCDConfig<CaseData, State, UserRole> {

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .searchCasesFields()
            .field("[CASE_REFERENCE]", "Case number", null, null, "1:ASC")
            .field("applicationType", "Application type")
            .field("dueDate","Due Date")
            .createdDateField()
            .lastModifiedDate()
            .stateField();
    }
}
