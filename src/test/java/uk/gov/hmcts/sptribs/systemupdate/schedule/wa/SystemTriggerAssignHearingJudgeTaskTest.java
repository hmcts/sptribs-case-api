package uk.gov.hmcts.sptribs.systemupdate.schedule.wa;

import org.elasticsearch.index.query.BoolQueryBuilder;
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
import uk.gov.hmcts.sptribs.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdUpdateService;

import java.time.LocalDate;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.caseworker.model.YesNo.YES;
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

    @Test
    void requestsOneEventPerDueHearingButNotForAnAlreadyRequestedHearing() {
        LocalDate today = LocalDate.of(2026, 10, 21);
        LocalDate hearingDate = today.plusDays(7);
        BoolQueryBuilder query = boolQuery().must(matchQuery("state", "AwaitingHearing"))
            .filter(rangeQuery("data.hearingList.value.date").from(today));
        CICUser user = mock(CICUser.class);
        CaseDetails rawCase = mock(CaseDetails.class);
        when(rawCase.getId()).thenReturn(123L);
        when(dates.today()).thenReturn(today);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn("service-auth");
        when(searchService.searchForAllCasesWithQuery(query, user, "service-auth")).thenReturn(List.of(rawCase));
        when(dates.releaseDate(hearingDate)).thenReturn(today.minusDays(7));
        when(dates.isDue(hearingDate, today.minusDays(7))).thenReturn(true);

        Listing due = Listing.builder().date(hearingDate).hearingStatus(Listed).build();
        Listing alreadyRequested = Listing.builder().date(hearingDate).hearingStatus(Listed)
            .hearingJudgeTaskRequested(YES).build();
        CaseData data = CaseData.builder().hearingList(List.of(
            ListValue.<Listing>builder().value(due).build(),
            ListValue.<Listing>builder().value(alreadyRequested).build())).build();
        uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> sdkCase = new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        sdkCase.setData(data);
        when(converter.convertToCaseDetailsFromReformModel(rawCase)).thenReturn(sdkCase);

        task.run();

        verify(updateService, times(1)).submitEvent(123L, SYSTEM_TRIGGER_ASSIGN_HEARING_JUDGE, user, "service-auth");
    }
}
