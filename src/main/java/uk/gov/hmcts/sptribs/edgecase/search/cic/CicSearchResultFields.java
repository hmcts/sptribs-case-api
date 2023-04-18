package uk.gov.hmcts.sptribs.edgecase.search.cic;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.edgecase.model.CaseData;
import uk.gov.hmcts.sptribs.edgecase.model.State;
import uk.gov.hmcts.sptribs.edgecase.model.UserRole;

@Component
public class CicSearchResultFields implements CCDConfig<CaseData, State, UserRole> {

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .searchResultFields()
            .caseReferenceField();
    }
}
