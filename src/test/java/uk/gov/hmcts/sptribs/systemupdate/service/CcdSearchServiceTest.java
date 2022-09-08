package uk.gov.hmcts.sptribs.systemupdate.service;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.sptribs.systemupdate.convert.CaseDetailsListConverter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.elasticsearch.search.sort.SortOrder.ASC;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.sptribs.ciccase.CriminalInjuriesCompensation.CASE_TYPE;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Holding;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;
import static uk.gov.hmcts.sptribs.systemupdate.service.CcdSearchService.DUE_DATE;
import static uk.gov.hmcts.sptribs.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.feignException;

@ExtendWith(MockitoExtension.class)
class CcdSearchServiceTest {

    public static final int PAGE_SIZE = 100;
    public static final int BULK_LIST_MAX_PAGE_SIZE = 50;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private CaseDetailsListConverter caseDetailsListConverter;

    @InjectMocks
    private CcdSearchService ccdSearchService;

    @BeforeEach
    void setPageSize() {
        setField(ccdSearchService, "pageSize", PAGE_SIZE);
        setField(ccdSearchService, "bulkActionPageSize", BULK_LIST_MAX_PAGE_SIZE);
    }

    @Test
    void shouldReturnCasesWithGivenStateBeforeTodayWithoutFlag() {
        //Given
        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        final int from = 0;
        final int pageSize = 100;
        final SearchResult expected = SearchResult.builder().total(PAGE_SIZE).cases(createCaseDetailsList(PAGE_SIZE)).build();
        final BoolQueryBuilder query = boolQuery()
            .must(matchQuery(STATE, AwaitingApplicant2Response))
            .filter(rangeQuery(DUE_DATE).lte(LocalDate.now()))
            .mustNot(matchQuery("data.applicant2ReminderSent", YesOrNo.YES));

        SearchSourceBuilder sourceBuilder = SearchSourceBuilder
            .searchSource()
            .sort(DUE_DATE, ASC)
            .query(query)
            .from(from)
            .size(pageSize);

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            CASE_TYPE,
            sourceBuilder.toString()))
            .thenReturn(expected);

        //When
        final SearchResult result = ccdSearchService.searchForCasesWithQuery(from, pageSize, query, user, SERVICE_AUTHORIZATION);

        //Then
        assertThat(result.getTotal()).isEqualTo(100);
        assertThat(result).isEqualTo(expected);
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
            CASE_TYPE,
            getSourceBuilder(0, PAGE_SIZE).toString()))
            .thenReturn(expected1);
        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            CASE_TYPE,
            getSourceBuilder(PAGE_SIZE, PAGE_SIZE).toString()))
            .thenReturn(expected2);

        //When
        final List<CaseDetails> searchResult = ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, Submitted);

        //Then
        assertThat(searchResult.size()).isEqualTo(101);
    }

    @Test
    void shouldReturnAllCasesInHolding() {
        //Given
        final BoolQueryBuilder query = boolQuery()
            .must(matchQuery(STATE, Holding))
            .filter(rangeQuery(DUE_DATE).lte(LocalDate.now()));

        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        final SearchResult expected1 = SearchResult.builder().total(PAGE_SIZE).cases(createCaseDetailsList(PAGE_SIZE)).build();
        final SearchResult expected2 = SearchResult.builder().total(1).cases(createCaseDetailsList(1)).build();

        SearchSourceBuilder sourceBuilder1 = SearchSourceBuilder
            .searchSource()
            .sort(DUE_DATE, ASC)
            .query(
                boolQuery()
                    .must(matchQuery(STATE, Holding))
                    .filter(rangeQuery(DUE_DATE).lte(LocalDate.now()))
            )
            .from(0)
            .size(PAGE_SIZE);

        SearchSourceBuilder sourceBuilder2 = SearchSourceBuilder
            .searchSource()
            .sort(DUE_DATE, ASC)
            .query(
                boolQuery()
                    .must(matchQuery(STATE, Holding))
                    .filter(rangeQuery(DUE_DATE).lte(LocalDate.now()))
            )
            .from(PAGE_SIZE)
            .size(PAGE_SIZE);

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            CASE_TYPE,
            sourceBuilder1.toString()))
            .thenReturn(expected1);
        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            CASE_TYPE,
            sourceBuilder2.toString()))
            .thenReturn(expected2);

        //When
        final List<CaseDetails> searchResult = ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, Holding);

        //Then
        assertThat(searchResult.size()).isEqualTo(101);
    }

    @Test
    void shouldReturnAllCasesWithGivenStateWhenFlagIsPassed() {
        //Given
        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        final int from = 0;
        final SearchResult expected = SearchResult.builder().total(PAGE_SIZE).cases(createCaseDetailsList(PAGE_SIZE)).build();
        final SearchResult expected2 = SearchResult.builder().total(1).cases(createCaseDetailsList(1)).build();
        final BoolQueryBuilder query = boolQuery()
            .must(matchQuery("state", AwaitingApplicant2Response))
            .filter(rangeQuery(DUE_DATE).lte(LocalDate.now()))
            .mustNot(matchQuery("data.applicant2ReminderSent", YesOrNo.YES));

        SearchSourceBuilder sourceBuilder = SearchSourceBuilder
            .searchSource()
            .sort(DUE_DATE, ASC)
            .query(query)
            .from(from)
            .size(PAGE_SIZE);

        SearchSourceBuilder sourceBuilder2 = SearchSourceBuilder
            .searchSource()
            .sort(DUE_DATE, ASC)
            .query(query)
            .from(PAGE_SIZE)
            .size(PAGE_SIZE);

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            CASE_TYPE,
            sourceBuilder.toString()))
            .thenReturn(expected);

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            CASE_TYPE,
            sourceBuilder2.toString()))
            .thenReturn(expected2);

        //When
        final List<CaseDetails> searchResult = ccdSearchService.searchForAllCasesWithQuery(
            query, user, SERVICE_AUTHORIZATION, AwaitingApplicant2Response);

        //Then
        assertThat(searchResult.size()).isEqualTo(101);
    }

    @Test
    void shouldReturnCasesWithVersionOlderThan() {
        //Given
        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        final SearchResult expected1 = SearchResult.builder().total(PAGE_SIZE).cases(createCaseDetailsList(PAGE_SIZE)).build();

        SearchSourceBuilder sourceBuilder = SearchSourceBuilder
            .searchSource()
            .query(
                boolQuery()
                    .must(boolQuery()
                        .mustNot(matchQuery("data.dataVersion", 0))
                    )
                    .must(boolQuery()
                        .should(boolQuery().mustNot(existsQuery("data.dataVersion")))
                        .should(boolQuery().must(rangeQuery("data.dataVersion").lt(1)))
                    )
            )
            .from(0)
            .size(500);

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            CASE_TYPE,
            sourceBuilder.toString()))
            .thenReturn(expected1);

        //When
        final List<CaseDetails> searchResult = ccdSearchService.searchForCasesWithVersionLessThan(1, user, SERVICE_AUTHORIZATION);

        //Then
        assertThat(searchResult.size()).isEqualTo(100);
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
                CASE_TYPE,
                getSourceBuilder(0, PAGE_SIZE).toString());

        //When&Then
        final CcdSearchCaseException exception = assertThrows(
            CcdSearchCaseException.class,
            () -> ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, Submitted));

        assertThat(exception.getMessage()).contains("Failed to complete search for Cases with state of [Submitted]");
    }

    @Test
    void shouldReturnAllPagesOfCasesInStateAwaitingPronouncement() {
        //Given
        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        final SearchResult searchResult1 = SearchResult.builder().total(PAGE_SIZE)
            .cases(createCaseDetailsList(PAGE_SIZE)).build();
        final SearchResult searchResult2 = SearchResult.builder().total(1)
            .cases(createCaseDetailsList(1)).build();
        final List<CaseDetails> expectedCases = concat(searchResult1.getCases().stream(), searchResult2.getCases().stream())
            .collect(toList());

        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            CASE_TYPE,
            searchSourceBuilderForAwaitingPronouncementCases(0).toString()))
            .thenReturn(searchResult1);
        when(coreCaseDataApi.searchCases(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            CASE_TYPE,
            searchSourceBuilderForAwaitingPronouncementCases(100).toString()))
            .thenReturn(searchResult2);
        when(caseDetailsListConverter.convertToListOfValidCaseDetails(expectedCases)).thenReturn(createConvertedCaseDetailsList(101));

        //When
        final Deque<List<uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State>>> allPages =
            ccdSearchService.searchAwaitingPronouncementCasesAllPages(user, SERVICE_AUTHORIZATION);

        //Then
        assertThat(allPages.size()).isEqualTo(3);
        assertThat(allPages.poll().size()).isEqualTo(BULK_LIST_MAX_PAGE_SIZE);
        assertThat(allPages.poll().size()).isEqualTo(BULK_LIST_MAX_PAGE_SIZE);
        assertThat(allPages.poll().size()).isEqualTo(1);
    }

    @Test
    void shouldThrowCcdSearchFailedExceptionIfSearchingCasesInAwaitingPronouncementAllPagesFails() {
        //Given
        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());

        doThrow(feignException(422, "some error")).when(coreCaseDataApi)
            .searchCases(
                SYSTEM_UPDATE_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                CASE_TYPE,
                searchSourceBuilderForAwaitingPronouncementCases(0).toString());

        //When&Then
        final CcdSearchCaseException exception = assertThrows(
            CcdSearchCaseException.class,
            () -> ccdSearchService.searchAwaitingPronouncementCasesAllPages(user, SERVICE_AUTHORIZATION));

        assertThat(exception.getMessage()).contains("Failed to complete search for Cases with state of [AwaitingPronouncement]");
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
            CASE_TYPE,
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

    @SuppressWarnings("unchecked")
    private List<uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State>> createConvertedCaseDetailsList(final int size) {

        final List<uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State>> caseDetails = new ArrayList<>();

        for (int index = 0; index < size; index++) {
            caseDetails.add(mock(uk.gov.hmcts.ccd.sdk.api.CaseDetails.class));
        }

        return caseDetails;
    }

    private SearchSourceBuilder getSourceBuilder(final int from, final int pageSize) {
        return SearchSourceBuilder
            .searchSource()
            .sort(DUE_DATE, ASC)
            .query(boolQuery()
                .must(matchQuery(STATE, Submitted))
                .filter(rangeQuery(DUE_DATE).lte(LocalDate.now())))
            .from(from)
            .size(pageSize);
    }

    private SearchSourceBuilder searchSourceBuilderForAwaitingPronouncementCases(final int from) {
        QueryBuilder stateQuery = matchQuery(STATE, AwaitingPronouncement);
        QueryBuilder bulkListingCaseId = existsQuery("data.bulkListCaseReference");

        QueryBuilder query = boolQuery()
            .must(stateQuery)
            .mustNot(bulkListingCaseId);

        return SearchSourceBuilder
            .searchSource()
            .sort(DUE_DATE, ASC)
            .query(query)
            .from(from)
            .size(PAGE_SIZE);
    }
}
