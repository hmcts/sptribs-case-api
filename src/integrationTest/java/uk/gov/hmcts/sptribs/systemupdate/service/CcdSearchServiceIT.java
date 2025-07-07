package uk.gov.hmcts.sptribs.systemupdate.service;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.sptribs.common.ccd.CcdCaseType;

import java.time.LocalDate;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.elasticsearch.search.sort.SortOrder.ASC;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;
import static uk.gov.hmcts.sptribs.systemupdate.service.CcdSearchService.DUE_DATE;
import static uk.gov.hmcts.sptribs.systemupdate.service.CcdSearchService.MIGRATION_PAGE_SIZE;
import static uk.gov.hmcts.sptribs.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CcdSearchServiceIT {

    @Autowired
    private CcdSearchService ccdSearchService;

    @MockitoBean
    private CoreCaseDataApi coreCaseDataApi;

    @Test
    void shouldSearchForCases() {
        final List<String> caseReferences = singletonList(TEST_CASE_ID.toString());

        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());

        final List<CaseDetails> caseDetailsList = singletonList(CaseDetails.builder().build());

        final SearchResult expectedSearchResult = SearchResult.builder()
            .total(1)
            .cases(caseDetailsList)
            .build();

        final SearchSourceBuilder sourceBuilder = SearchSourceBuilder
            .searchSource()
            .query(
                boolQuery().must(termsQuery("reference", caseReferences))
            )
            .from(0)
            .size(50);

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            CcdCaseType.CIC.getCaseTypeName(),
            sourceBuilder.toString()
        )).thenReturn(expectedSearchResult);

        final List<CaseDetails> searchResult = ccdSearchService.searchForCases(caseReferences, user, SERVICE_AUTHORIZATION);

        assertThat(searchResult).isNotNull();
        assertThat(searchResult.size()).isEqualTo(1);
        assertThat(searchResult.get(0)).isEqualTo(caseDetailsList.get(0));
    }

    @Test
    void shouldSearchForAllCasesWithQuery() {
        final BoolQueryBuilder query = boolQuery()
            .must(matchQuery(STATE, Submitted))
            .filter(rangeQuery(DUE_DATE).lte(LocalDate.now()));

        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());

        final List<CaseDetails> caseDetailsList = singletonList(CaseDetails.builder().build());

        final SearchResult expectedSearchResult = SearchResult.builder()
            .total(1)
            .cases(caseDetailsList)
            .build();

        final SearchSourceBuilder sourceBuilder = SearchSourceBuilder
            .searchSource()
            .sort(DUE_DATE, ASC)
            .query(query)
            .from(0)
            .size(100);

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            CcdCaseType.CIC.getCaseTypeName(),
            sourceBuilder.toString()
        )).thenReturn(expectedSearchResult);

        final List<CaseDetails> searchResult = ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, Submitted);

        assertThat(searchResult).isNotNull();
        assertThat(searchResult.size()).isEqualTo(1);
        assertThat(searchResult.get(0)).isEqualTo(caseDetailsList.get(0));
    }

    @Test
    void shouldSearchForCasesWithLessThanVersion() {
        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());

        final List<CaseDetails> caseDetailsList = singletonList(CaseDetails.builder().build());

        final SearchResult expectedSearchResult = SearchResult.builder()
            .total(1)
            .cases(caseDetailsList)
            .build();

        final SearchSourceBuilder sourceBuilder = SearchSourceBuilder
            .searchSource()
            .query(
                boolQuery().must(boolQuery().must(rangeQuery("data.dataVersion").lt(1)))
            )
            .from(0)
            .size(MIGRATION_PAGE_SIZE);

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            CcdCaseType.CIC.getCaseTypeName(),
            sourceBuilder.toString()))
            .thenReturn(expectedSearchResult);

        final List<CaseDetails> searchResult = ccdSearchService
            .searchForCasesWithVersionLessThan(1, user, SERVICE_AUTHORIZATION);

        assertThat(searchResult).isNotNull();
        assertThat(searchResult.size()).isEqualTo(1);
        assertThat(searchResult.get(0)).isEqualTo(caseDetailsList.get(0));
    }
}
