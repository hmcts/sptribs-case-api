package uk.gov.hmcts.sptribs.ciccase.search;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.SearchCriteriaField;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import java.util.List;

import static java.util.List.of;

@Component
public class SearchCriteria implements CCDConfig<CaseData, State, UserRole> {

    private static final List<SearchCriteriaField> SEARCH_CRITERIA_LIST = of(
        SearchCriteriaField.builder().otherCaseReference("cicCaseCicaReferenceNumber").build()
    );

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.searchCriteria()
            .fields(SEARCH_CRITERIA_LIST)
            .build();
    }
}
