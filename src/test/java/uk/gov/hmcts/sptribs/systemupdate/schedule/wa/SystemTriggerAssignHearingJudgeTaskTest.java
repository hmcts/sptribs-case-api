package uk.gov.hmcts.sptribs.systemupdate.schedule.wa;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
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

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.caseworker.model.YesNo.YES;
import static uk.gov.hmcts.sptribs.ciccase.model.HearingState.Cancelled;
import static uk.gov.hmcts.sptribs.ciccase.model.HearingState.Listed;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemTriggerAssignHearingJudge.SYSTEM_TRIGGER_ASSIGN_HEARING_JUDGE;

@ExtendWith(MockitoExtension.class)
class SystemTriggerAssignHearingJudgeTaskTest {

    @Mock
    private CcdSearchService searchService;
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

    @InjectMocks
    private SystemTriggerAssignHearingJudgeTask task;

    private final LocalDate today = LocalDate.of(2026, 10, 21);
    private final BoolQueryBuilder query = boolQuery().must(matchQuery("state", "AwaitingHearing"))
        .filter(rangeQuery("data.hearingList.value.date").from(today));
    private CICUser user;

    @BeforeEach
    void setUp() {
        user = mock(CICUser.class);
        when(dates.today()).thenReturn(today);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn("service-auth");
    }

    @Test
    void requestsAnEventForUpcomingHearingDespiteHistoricCancelledHearing() {
        LocalDate hearingDate = today.plusDays(7);
        CaseDetails rawCase = mock(CaseDetails.class);
        when(rawCase.getId()).thenReturn(123L);
        when(searchService.searchForAllCasesWithQuery(query, user, "service-auth")).thenReturn(List.of(rawCase));
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
        when(searchService.searchForAllCasesWithQuery(query, user, "service-auth")).thenReturn(List.of(rawCase));

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
        when(searchService.searchForAllCasesWithQuery(query, user, "service-auth")).thenReturn(List.of(rawCase));

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
        when(searchService.searchForAllCasesWithQuery(query, user, "service-auth")).thenReturn(List.of(rawCase));
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
        when(searchService.searchForAllCasesWithQuery(query, user, "service-auth")).thenReturn(List.of(rawCase));
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
    void handlesCcdSearchCaseExceptionWhenSearchingCases() {
        when(searchService.searchForAllCasesWithQuery(query, user, "service-auth"))
            .thenThrow(new CcdSearchCaseException("Search failed", null));

        task.run();

        verifyNoInteractions(updateService);
    }

    @Test
    void handlesCcdConflictExceptionWhenSearchingCases() {
        when(searchService.searchForAllCasesWithQuery(query, user, "service-auth"))
            .thenThrow(new CcdConflictException("Task conflict", null));

        task.run();

        verifyNoInteractions(updateService);
    }

    @Test
    void doesNothingWhenSearchReturnsEmptyList() {
        when(searchService.searchForAllCasesWithQuery(query, user, "service-auth"))
            .thenReturn(List.of());

        task.run();

        verifyNoInteractions(updateService);
    }
}
