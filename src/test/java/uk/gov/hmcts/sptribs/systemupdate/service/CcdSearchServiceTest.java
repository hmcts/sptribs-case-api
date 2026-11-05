package uk.gov.hmcts.sptribs.systemupdate.service;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.sptribs.common.ccd.CcdCaseType;
import uk.gov.hmcts.sptribs.idam.CICUser;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
        final CICUser user = new CICUser(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        final SearchResult expected1 = SearchResult.builder().total(PAGE_SIZE + 1).cases(createCaseDetailsList(PAGE_SIZE)).build();
        final SearchResult expected2 = SearchResult.builder().total(PAGE_SIZE + 1).cases(createCaseDetailsList(1)).build();

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

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 99, 100, 101, 200, 250})
    void shouldRetrieveAllPagesUsingTotalMatchingCases(int total) {
        final CICUser user = new CICUser(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        final List<CaseDetails> expectedCases = createCaseDetailsList(total);
        final int pageCount = Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
        for (int page = 0; page < pageCount; page++) {
            final int from = page * PAGE_SIZE;
            when(coreCaseDataApi.searchCases(
                SYSTEM_UPDATE_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                CcdCaseType.CIC.getCaseTypeName(),
                getSourceBuilder(from).toString()))
                .thenReturn(SearchResult.builder().total(total)
                    .cases(expectedCases.subList(from, Math.min(from + PAGE_SIZE, total))).build());
        }

        final List<CaseDetails> result = ccdSearchService.searchForAllCasesWithQuery(
            getQuery(), user, SERVICE_AUTHORIZATION, Submitted);

        assertThat(result).containsExactlyElementsOf(expectedCases);
        for (int page = 0; page < pageCount; page++) {
            verify(coreCaseDataApi).searchCases(
                SYSTEM_UPDATE_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                CcdCaseType.CIC.getCaseTypeName(),
                getSourceBuilder(page * PAGE_SIZE).toString());
        }
        verifyNoMoreInteractions(coreCaseDataApi);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 50})
    void shouldContinueAfterShortOrEmptyPages(int returnedPageSize) {
        final CICUser user = new CICUser(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        final int total = 2 * PAGE_SIZE + 1;
        final List<CaseDetails> expectedCases = new ArrayList<>();
        for (int page = 0; page < 3; page++) {
            final List<CaseDetails> cases = createCaseDetailsList(page < 2 ? returnedPageSize : 1);
            expectedCases.addAll(cases);
            when(coreCaseDataApi.searchCases(
                SYSTEM_UPDATE_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                CcdCaseType.CIC.getCaseTypeName(),
                getSourceBuilder(page * PAGE_SIZE).toString()))
                .thenReturn(SearchResult.builder().total(total).cases(cases).build());
        }

        final List<CaseDetails> result = ccdSearchService.searchForAllCasesWithQuery(
            getQuery(), user, SERVICE_AUTHORIZATION, Submitted);

        assertThat(result).containsExactlyElementsOf(expectedCases);
        for (int page = 0; page < 3; page++) {
            verify(coreCaseDataApi).searchCases(
                SYSTEM_UPDATE_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                CcdCaseType.CIC.getCaseTypeName(),
                getSourceBuilder(page * PAGE_SIZE).toString());
        }
        verifyNoMoreInteractions(coreCaseDataApi);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1000})
    void shouldBoundSearchByFirstTotalWhenLaterTotalsChange(int laterTotal) {
        final CICUser user = new CICUser(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        final int total = 2 * PAGE_SIZE + 1;
        final List<CaseDetails> expectedCases = createCaseDetailsList(total);
        for (int page = 0; page < 3; page++) {
            final int from = page * PAGE_SIZE;
            when(coreCaseDataApi.searchCases(
                SYSTEM_UPDATE_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                CcdCaseType.CIC.getCaseTypeName(),
                getSourceBuilder(from).toString()))
                .thenReturn(SearchResult.builder().total(page == 0 ? total : laterTotal)
                    .cases(expectedCases.subList(from, Math.min(from + PAGE_SIZE, total))).build());
        }

        final List<CaseDetails> result = ccdSearchService.searchForAllCasesWithQuery(
            getQuery(), user, SERVICE_AUTHORIZATION, Submitted);

        assertThat(result).containsExactlyElementsOf(expectedCases);
        for (int page = 0; page < 3; page++) {
            verify(coreCaseDataApi).searchCases(
                SYSTEM_UPDATE_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                CcdCaseType.CIC.getCaseTypeName(),
                getSourceBuilder(page * PAGE_SIZE).toString());
        }
        verifyNoMoreInteractions(coreCaseDataApi);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void shouldRejectNonPositivePageSize(int pageSize) {
        setField(ccdSearchService, "pageSize", pageSize);
        final CICUser user = new CICUser(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> ccdSearchService.searchForAllCasesWithQuery(getQuery(), user, SERVICE_AUTHORIZATION, Submitted));

        assertThat(exception).hasMessage("Case search page size must be positive");
        verifyNoInteractions(coreCaseDataApi);
    }

    @Test
    void shouldReturnCasesWithVersionOlderThan() {
        //Given
        final CICUser user = new CICUser(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
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

    @ParameterizedTest
    @ValueSource(ints = {0, PAGE_SIZE})
    void shouldThrowCcdSearchFailedExceptionIfSearchFails(int failingOffset) {
        //Given
        final BoolQueryBuilder query = boolQuery()
            .must(matchQuery("state", Submitted))
            .filter(rangeQuery(DUE_DATE).lte(LocalDate.now()));
        final CICUser user = new CICUser(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());

        if (failingOffset > 0) {
            when(coreCaseDataApi.searchCases(
                SYSTEM_UPDATE_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                CcdCaseType.CIC.getCaseTypeName(),
                getSourceBuilder(0).toString()))
                .thenReturn(SearchResult.builder().total(PAGE_SIZE + 1)
                    .cases(createCaseDetailsList(PAGE_SIZE)).build());
        }
        doThrow(feignException(422, "A reason")).when(coreCaseDataApi)
            .searchCases(
                SYSTEM_UPDATE_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                CcdCaseType.CIC.getCaseTypeName(),
                getSourceBuilder(failingOffset).toString());

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
        final CICUser user = new CICUser(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
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
            .sort("reference", ASC)
            .query(getQuery())
            .trackTotalHits(true)
            .from(from)
            .size(CcdSearchServiceTest.PAGE_SIZE);
    }

    private BoolQueryBuilder getQuery() {
        return boolQuery()
            .must(matchQuery(STATE, Submitted))
            .filter(rangeQuery(DUE_DATE).lte(LocalDate.now()));
    }

}
