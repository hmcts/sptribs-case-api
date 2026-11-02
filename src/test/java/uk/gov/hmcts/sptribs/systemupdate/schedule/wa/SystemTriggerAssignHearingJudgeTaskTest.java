package uk.gov.hmcts.sptribs.systemupdate.schedule.wa;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.caseworker.service.HearingJudgeTaskDateService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.idam.CICUser;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdUpdateService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.search.sort.SortOrder.ASC;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.sptribs.caseworker.model.YesNo.YES;
import static uk.gov.hmcts.sptribs.ciccase.model.HearingState.Cancelled;
import static uk.gov.hmcts.sptribs.ciccase.model.HearingState.Listed;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_CASE_TYPE;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemTriggerAssignHearingJudge.SYSTEM_TRIGGER_ASSIGN_HEARING_JUDGE;
import static uk.gov.hmcts.sptribs.systemupdate.service.CcdSearchService.DUE_DATE;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.feignException;

@ExtendWith(MockitoExtension.class)
class SystemTriggerAssignHearingJudgeTaskTest {

    private static final int PAGE_SIZE = 2;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    private CcdUpdateService updateService;
    @Mock
    private CaseDetailsConverter converter;
    @Mock
    private HearingJudgeTaskDateService dates;
    @Mock
    private IdamService idamService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;

    private SystemTriggerAssignHearingJudgeTask task;

    private final LocalDate today = LocalDate.of(2026, 10, 21);
    private final BoolQueryBuilder query = boolQuery().must(matchQuery("state", "AwaitingHearing"))
        .filter(rangeQuery("data.hearingList.value.date").from(today));
    private CICUser user;

    @BeforeEach
    void setUp() {
        user = new CICUser("system-auth", UserInfo.builder().build());
        CcdSearchService searchService = new CcdSearchService();
        setField(searchService, "coreCaseDataApi", coreCaseDataApi);
        setField(searchService, "pageSize", PAGE_SIZE);
        task = new SystemTriggerAssignHearingJudgeTask(searchService, updateService, converter, dates,
            idamService, authTokenGenerator);
        when(dates.today()).thenReturn(today);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn("service-auth");
    }

    @Test
    void requestsAnEventForUpcomingHearingDespiteHistoricCancelledHearing() {
        LocalDate hearingDate = today.plusDays(7);
        CaseDetails rawCase = mock(CaseDetails.class);
        when(rawCase.getId()).thenReturn(123L);
        givenPage(0, 1, rawCase);
        when(dates.releaseDate(hearingDate)).thenReturn(today.minusDays(7));
        when(dates.isDue(hearingDate, today.minusDays(7))).thenReturn(true);

        Listing due = Listing.builder().date(hearingDate).hearingStatus(Listed).build();
        Listing historic = Listing.builder().date(hearingDate.minusDays(60)).hearingStatus(Cancelled)
            .hearingJudgeTaskRequested(YES).build();
        CaseData data = CaseData.builder().hearingList(List.of(
            ListValue.<Listing>builder().value(historic).build(),
            ListValue.<Listing>builder().value(due).build())).build();
        uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> sdkCase = new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        sdkCase.setData(data);
        when(converter.convertToCaseDetailsFromReformModel(rawCase)).thenReturn(sdkCase);

        task.run();

        verify(updateService, times(1)).submitEvent(123L, SYSTEM_TRIGGER_ASSIGN_HEARING_JUDGE, user, "service-auth");
    }

    @Test
    void doesNotRequestEventWhenHearingListIsNull() {
        CaseDetails rawCase = mock(CaseDetails.class);
        givenPage(0, 1, rawCase);

        CaseData data = CaseData.builder().hearingList(null).build();
        uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> sdkCase = new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        sdkCase.setData(data);
        when(converter.convertToCaseDetailsFromReformModel(rawCase)).thenReturn(sdkCase);

        task.run();

        verifyNoInteractions(updateService);
    }

    @Test
    void doesNotRequestEventWhenHearingsAreNotDueOrAlreadyRequestedOrNull() {
        LocalDate hearingDate = today.plusDays(7);
        CaseDetails rawCase = mock(CaseDetails.class);
        givenPage(0, 1, rawCase);

        Listing nullListing = null;
        Listing alreadyRequested = Listing.builder().date(hearingDate).hearingStatus(Listed)
            .hearingJudgeTaskRequested(YES).build();
        Listing notDue = Listing.builder().date(hearingDate).hearingStatus(Listed).build();

        when(dates.releaseDate(hearingDate)).thenReturn(today.minusDays(7));
        when(dates.isDue(hearingDate, today.minusDays(7))).thenReturn(false);

        CaseData data = CaseData.builder().hearingList(List.of(
            ListValue.<Listing>builder().value(nullListing).build(),
            ListValue.<Listing>builder().value(alreadyRequested).build(),
            ListValue.<Listing>builder().value(notDue).build())).build();
        uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> sdkCase = new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        sdkCase.setData(data);
        when(converter.convertToCaseDetailsFromReformModel(rawCase)).thenReturn(sdkCase);

        task.run();

        verifyNoInteractions(updateService);
    }

    @Test
    void handlesCcdManagementExceptionWhenSubmittingEvent() {
        LocalDate hearingDate = today.plusDays(7);
        CaseDetails rawCase = mock(CaseDetails.class);
        when(rawCase.getId()).thenReturn(123L);
        givenPage(0, 1, rawCase);
        when(dates.releaseDate(hearingDate)).thenReturn(today.minusDays(7));
        when(dates.isDue(hearingDate, today.minusDays(7))).thenReturn(true);

        Listing due = Listing.builder().date(hearingDate).hearingStatus(Listed).build();
        CaseData data = CaseData.builder().hearingList(List.of(
            ListValue.<Listing>builder().value(due).build())).build();
        uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> sdkCase = new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        sdkCase.setData(data);
        when(converter.convertToCaseDetailsFromReformModel(rawCase)).thenReturn(sdkCase);
        doThrow(new CcdManagementException("Submit event failed", null))
            .when(updateService).submitEvent(123L, SYSTEM_TRIGGER_ASSIGN_HEARING_JUDGE, user, "service-auth");

        task.run();

        verify(updateService).submitEvent(123L, SYSTEM_TRIGGER_ASSIGN_HEARING_JUDGE, user, "service-auth");
    }

    @Test
    void handlesIllegalArgumentExceptionWhenSubmittingEvent() {
        LocalDate hearingDate = today.plusDays(7);
        CaseDetails rawCase = mock(CaseDetails.class);
        when(rawCase.getId()).thenReturn(123L);
        givenPage(0, 1, rawCase);
        when(dates.releaseDate(hearingDate)).thenReturn(today.minusDays(7));
        when(dates.isDue(hearingDate, today.minusDays(7))).thenReturn(true);

        Listing due = Listing.builder().date(hearingDate).hearingStatus(Listed).build();
        CaseData data = CaseData.builder().hearingList(List.of(
            ListValue.<Listing>builder().value(due).build())).build();
        uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> sdkCase = new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        sdkCase.setData(data);
        when(converter.convertToCaseDetailsFromReformModel(rawCase)).thenReturn(sdkCase);
        doThrow(new IllegalArgumentException("Invalid argument"))
            .when(updateService).submitEvent(123L, SYSTEM_TRIGGER_ASSIGN_HEARING_JUDGE, user, "service-auth");

        task.run();

        verify(updateService).submitEvent(123L, SYSTEM_TRIGGER_ASSIGN_HEARING_JUDGE, user, "service-auth");
    }

    @Test
    void failsTheRunWhenSearchingCasesFails() {
        var failure = feignException(500, "Search failed");
        when(coreCaseDataApi.searchCases("system-auth", "service-auth", ST_CIC_CASE_TYPE, searchQuery(0)))
            .thenThrow(failure);

        CcdSearchCaseException exception = assertThrows(CcdSearchCaseException.class, task::run);

        assertThat(exception).hasCause(failure);
        verifyNoInteractions(updateService);
    }

    @Test
    void continuesToTheNextCaseWhenSubmittingAnEventConflicts() {
        givenHearingIsDue();
        givenPage(0, 2, dueCase(123L), dueCase(456L));
        doThrow(new CcdConflictException("Task conflict", null))
            .when(updateService).submitEvent(123L, SYSTEM_TRIGGER_ASSIGN_HEARING_JUDGE, user, "service-auth");

        task.run();

        verify(updateService).submitEvent(123L, SYSTEM_TRIGGER_ASSIGN_HEARING_JUDGE, user, "service-auth");
        verify(updateService).submitEvent(456L, SYSTEM_TRIGGER_ASSIGN_HEARING_JUDGE, user, "service-auth");
    }

    @Test
    void doesNothingWhenSearchReturnsEmptyList() {
        givenPage(0, 0);

        task.run();

        verifyNoInteractions(updateService, converter);
        verify(coreCaseDataApi).searchCases("system-auth", "service-auth", ST_CIC_CASE_TYPE, searchQuery(0));
        verifyNoMoreInteractions(coreCaseDataApi);
    }

    @Test
    void processesEveryPageUsingTheOverallTotal() {
        givenHearingIsDue();
        givenPage(0, 5, dueCase(1L), dueCase(2L));
        givenPage(2, 5, dueCase(3L), dueCase(4L));
        givenPage(4, 5, dueCase(5L));

        task.run();

        for (long id = 1; id <= 5; id++) {
            verify(updateService).submitEvent(id, SYSTEM_TRIGGER_ASSIGN_HEARING_JUDGE, user, "service-auth");
        }
        verify(coreCaseDataApi).searchCases("system-auth", "service-auth", ST_CIC_CASE_TYPE, searchQuery(0));
        verify(coreCaseDataApi).searchCases("system-auth", "service-auth", ST_CIC_CASE_TYPE, searchQuery(2));
        verify(coreCaseDataApi).searchCases("system-auth", "service-auth", ST_CIC_CASE_TYPE, searchQuery(4));
        verifyNoMoreInteractions(coreCaseDataApi, updateService);
    }

    @Test
    void stopsAfterExactlyOneFullPage() {
        givenHearingIsDue();
        givenPage(0, 2, dueCase(1L), dueCase(2L));

        task.run();

        verify(updateService).submitEvent(1L, SYSTEM_TRIGGER_ASSIGN_HEARING_JUDGE, user, "service-auth");
        verify(updateService).submitEvent(2L, SYSTEM_TRIGGER_ASSIGN_HEARING_JUDGE, user, "service-auth");
        verify(coreCaseDataApi).searchCases("system-auth", "service-auth", ST_CIC_CASE_TYPE, searchQuery(0));
        verifyNoMoreInteractions(coreCaseDataApi, updateService);
    }

    @Test
    void stopsAfterAnExactMultipleOfThePageSize() {
        givenHearingIsDue();
        givenPage(0, 4, dueCase(1L), dueCase(2L));
        givenPage(2, 4, dueCase(3L), dueCase(4L));

        task.run();

        for (long id = 1; id <= 4; id++) {
            verify(updateService).submitEvent(id, SYSTEM_TRIGGER_ASSIGN_HEARING_JUDGE, user, "service-auth");
        }
        verify(coreCaseDataApi).searchCases("system-auth", "service-auth", ST_CIC_CASE_TYPE, searchQuery(0));
        verify(coreCaseDataApi).searchCases("system-auth", "service-auth", ST_CIC_CASE_TYPE, searchQuery(2));
        verifyNoMoreInteractions(coreCaseDataApi, updateService);
    }

    @Test
    void continuesPastAnEmptyFilteredPage() {
        givenHearingIsDue();
        givenPage(0, 3);
        givenPage(2, 3, dueCase(3L));

        task.run();

        verify(updateService).submitEvent(3L, SYSTEM_TRIGGER_ASSIGN_HEARING_JUDGE, user, "service-auth");
        verify(coreCaseDataApi).searchCases("system-auth", "service-auth", ST_CIC_CASE_TYPE, searchQuery(0));
        verify(coreCaseDataApi).searchCases("system-auth", "service-auth", ST_CIC_CASE_TYPE, searchQuery(2));
        verifyNoMoreInteractions(coreCaseDataApi, updateService);
    }

    @Test
    void advancesByTheRequestedPageSizeAfterShortFilteredPages() {
        givenHearingIsDue();
        givenPage(0, 5, dueCase(1L));
        givenPage(2, 5, dueCase(3L));
        givenPage(4, 5, dueCase(5L));

        task.run();

        verify(updateService).submitEvent(1L, SYSTEM_TRIGGER_ASSIGN_HEARING_JUDGE, user, "service-auth");
        verify(updateService).submitEvent(3L, SYSTEM_TRIGGER_ASSIGN_HEARING_JUDGE, user, "service-auth");
        verify(updateService).submitEvent(5L, SYSTEM_TRIGGER_ASSIGN_HEARING_JUDGE, user, "service-auth");
        verify(coreCaseDataApi).searchCases("system-auth", "service-auth", ST_CIC_CASE_TYPE, searchQuery(0));
        verify(coreCaseDataApi).searchCases("system-auth", "service-auth", ST_CIC_CASE_TYPE, searchQuery(2));
        verify(coreCaseDataApi).searchCases("system-auth", "service-auth", ST_CIC_CASE_TYPE, searchQuery(4));
        verifyNoMoreInteractions(coreCaseDataApi, updateService);
    }

    @Test
    void boundsTheRunByTheFirstTotalWhenNewCasesAppear() {
        givenHearingIsDue();
        givenPage(0, 3, dueCase(1L), dueCase(2L));
        givenPage(2, 100, dueCase(3L));

        task.run();

        verify(coreCaseDataApi).searchCases("system-auth", "service-auth", ST_CIC_CASE_TYPE, searchQuery(0));
        verify(coreCaseDataApi).searchCases("system-auth", "service-auth", ST_CIC_CASE_TYPE, searchQuery(2));
        verifyNoMoreInteractions(coreCaseDataApi);
    }

    @Test
    void failsTheRunWhenALaterSearchPageFails() {
        givenPage(0, 3, CaseDetails.builder().id(1L).build(), CaseDetails.builder().id(2L).build());
        var failure = feignException(500, "Search failed");
        when(coreCaseDataApi.searchCases("system-auth", "service-auth", ST_CIC_CASE_TYPE, searchQuery(2)))
            .thenThrow(failure);

        CcdSearchCaseException exception = assertThrows(CcdSearchCaseException.class, task::run);

        assertThat(exception).hasCause(failure).hasMessageContaining("[AwaitingHearing]");
        verifyNoInteractions(updateService, converter);
    }

    @Test
    void continuesToTheNextCaseWhenSubmittingAnEventFails() {
        givenHearingIsDue();
        givenPage(0, 2, dueCase(1L), dueCase(2L));
        doThrow(new CcdManagementException("Submit event failed", null))
            .when(updateService).submitEvent(1L, SYSTEM_TRIGGER_ASSIGN_HEARING_JUDGE, user, "service-auth");

        task.run();

        verify(updateService).submitEvent(1L, SYSTEM_TRIGGER_ASSIGN_HEARING_JUDGE, user, "service-auth");
        verify(updateService).submitEvent(2L, SYSTEM_TRIGGER_ASSIGN_HEARING_JUDGE, user, "service-auth");
    }

    @Test
    void doesNotRequestAgainOnALaterRunOnceTheHearingIsMarkedRequested() {
        givenHearingIsDue();
        CaseDetails rawCase = dueCase(1L);
        givenPage(0, 1, rawCase);

        task.run();
        converter.convertToCaseDetailsFromReformModel(rawCase).getData().getHearingList().getFirst().getValue()
            .setHearingJudgeTaskRequested(YES);
        task.run();

        verify(updateService).submitEvent(1L, SYSTEM_TRIGGER_ASSIGN_HEARING_JUDGE, user, "service-auth");
        verifyNoMoreInteractions(updateService);
    }

    private void givenPage(int from, int total, CaseDetails... cases) {
        when(coreCaseDataApi.searchCases("system-auth", "service-auth", ST_CIC_CASE_TYPE, searchQuery(from)))
            .thenReturn(SearchResult.builder().total(total).cases(List.of(cases)).build());
    }

    private String searchQuery(int from) {
        return SearchSourceBuilder.searchSource()
            .sort(DUE_DATE, ASC)
            .sort("reference", ASC)
            .query(query)
            .trackTotalHits(true)
            .from(from)
            .size(PAGE_SIZE)
            .toString();
    }

    private void givenHearingIsDue() {
        when(dates.releaseDate(today.plusDays(7))).thenReturn(today.minusDays(7));
        when(dates.isDue(today.plusDays(7), today.minusDays(7))).thenReturn(true);
    }

    private CaseDetails dueCase(long id) {
        CaseDetails rawCase = CaseDetails.builder().id(id).build();
        Listing listing = Listing.builder().date(today.plusDays(7)).hearingStatus(Listed).build();
        CaseData data = CaseData.builder().hearingList(List.of(
            ListValue.<Listing>builder().value(listing).build())).build();
        uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> sdkCase = new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        sdkCase.setData(data);
        when(converter.convertToCaseDetailsFromReformModel(rawCase)).thenReturn(sdkCase);
        return rawCase;
    }
}
