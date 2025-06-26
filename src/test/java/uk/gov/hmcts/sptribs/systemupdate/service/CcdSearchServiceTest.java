package uk.gov.hmcts.sptribs.systemupdate.service;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.sptribs.common.ccd.CcdCaseType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.elasticsearch.search.sort.SortOrder.ASC;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;
import static uk.gov.hmcts.sptribs.systemupdate.service.CcdSearchService.DUE_DATE;
import static uk.gov.hmcts.sptribs.systemupdate.service.CcdSearchService.MIGRATION_PAGE_SIZE;
import static uk.gov.hmcts.sptribs.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.feignException;

@ExtendWith(MockitoExtension.class)
class CcdSearchServiceTest {

    public static final int PAGE_SIZE = 100;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @InjectMocks
    private CcdSearchService ccdSearchService;

    @BeforeEach
    void setPageSize() {
        setField(ccdSearchService, "pageSize", PAGE_SIZE);
    }

    @Test
    void shouldReturnAllCasesWithGivenState() {
        //Given
        final BoolQueryBuilder query = boolQuery()
            .must(matchQuery(STATE, Submitted))
            .filter(rangeQuery(DUE_DATE).lte(LocalDate.now()));
        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        final SearchResult expected1 = SearchResult.builder().total(PAGE_SIZE).cases(createCaseDetailsList(PAGE_SIZE)).build();
        final SearchResult expected2 = SearchResult.builder().total(1).cases(createCaseDetailsList(1)).build();

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            CcdCaseType.CIC.getCaseTypeName(),
            getSourceBuilder(0).toString()))
            .thenReturn(expected1);
        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            CcdCaseType.CIC.getCaseTypeName(),
            getSourceBuilder(PAGE_SIZE).toString()))
            .thenReturn(expected2);

        //When
        final List<CaseDetails> searchResult = ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, Submitted);

        //Then
        assertThat(searchResult.size()).isEqualTo(101);
        assertNotNull(searchResult.listIterator(0));
    }

    @Test
    void shouldReturnCasesWithVersionOlderThan() {
        //Given
        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        final SearchResult expected1 = SearchResult.builder().total(PAGE_SIZE).cases(createCaseDetailsList(PAGE_SIZE)).build();
        final int latestVersion = 1;

        final SearchSourceBuilder sourceBuilder = SearchSourceBuilder
                .searchSource()
                .query(
                        boolQuery()
                                .must(boolQuery()
                                        .must(rangeQuery("data.dataVersion").lt(latestVersion)))
                )
                .from(0)
                .size(MIGRATION_PAGE_SIZE);

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            CcdCaseType.CIC.getCaseTypeName(),
            sourceBuilder.toString()))
            .thenReturn(expected1);

        //When
        final List<CaseDetails> searchResult =
            ccdSearchService.searchForCasesWithVersionLessThan(latestVersion, user, SERVICE_AUTHORIZATION);

        //Then
        assertThat(searchResult.size()).isEqualTo(100);
        assertNotNull(searchResult.listIterator(0));
    }

    @Test
    void shouldThrowCcdSearchFailedExceptionIfSearchFails() {
        //Given
        final BoolQueryBuilder query = boolQuery()
            .must(matchQuery("state", Submitted))
            .filter(rangeQuery(DUE_DATE).lte(LocalDate.now()));
        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());

        doThrow(feignException(422, "A reason")).when(coreCaseDataApi)
            .searchCases(
                SYSTEM_UPDATE_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                CcdCaseType.CIC.getCaseTypeName(),
                getSourceBuilder(0).toString());

        //When&Then
        final CcdSearchCaseException exception = assertThrows(
            CcdSearchCaseException.class,
            () -> ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, Submitted));

        assertNotNull(exception.getCause());
        assertThat(exception.getMessage()).contains("Failed to complete search for Cases with state of [Submitted]");
    }

    @Test
    void shouldReturnCasesFromCcdWithMatchingCaseReferences() {
        //Given
        final List<String> caseReferences = List.of(
            "1643192250866023",
            "1627308042786515",
            "1627504115236368",
            "1627568021302127"
        );
        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        final SearchResult expected = SearchResult.builder()
            .total(caseReferences.size())
            .cases(createCaseDetailsList(caseReferences.size()))
            .build();

        SearchSourceBuilder sourceBuilder = SearchSourceBuilder
            .searchSource()
            .query(
                boolQuery()
                    .must(termsQuery("reference", caseReferences))
            )
            .from(0)
            .size(50);

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            CcdCaseType.CIC.getCaseTypeName(),
            sourceBuilder.toString()))
            .thenReturn(expected);

        //When
        final List<CaseDetails> searchResult = ccdSearchService.searchForCases(caseReferences, user, SERVICE_AUTHORIZATION);

        //Then
        assertThat(searchResult.size()).isEqualTo(4);
    }

    private List<CaseDetails> createCaseDetailsList(final int size) {

        final List<CaseDetails> caseDetails = new ArrayList<>();

        for (int index = 0; index < size; index++) {
            caseDetails.add(mock(CaseDetails.class));
        }

        return caseDetails;
    }


    private SearchSourceBuilder getSourceBuilder(final int from) {
        return SearchSourceBuilder
            .searchSource()
            .sort(DUE_DATE, ASC)
            .query(boolQuery()
                .must(matchQuery(STATE, Submitted))
                .filter(rangeQuery(DUE_DATE).lte(LocalDate.now())))
            .from(from)
            .size(CcdSearchServiceTest.PAGE_SIZE);
    }

}
