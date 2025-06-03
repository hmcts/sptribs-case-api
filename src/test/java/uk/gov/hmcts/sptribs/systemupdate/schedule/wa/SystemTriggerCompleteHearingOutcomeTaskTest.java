package uk.gov.hmcts.sptribs.systemupdate.schedule.wa;

import feign.FeignException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.sptribs.idam.IdamService;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemTriggerCompleteHearingOutcome.SYSTEM_TRIGGER_COMPLETE_HEARING_OUTCOME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class SystemTriggerCompleteHearingOutcomeTaskTest {

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private SystemTriggerCompleteHearingOutcomeTask task;

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private CcdUpdateService ccdUpdateService;

    private User user;

    private static final BoolQueryBuilder query = boolQuery()
        .must(matchQuery("state", "AwaitingHearing"))
        .mustNot(matchQuery("data.completeHearingOutcomeTask", "Yes"))
        .filter(rangeQuery("data.hearingList.value.date").to(LocalDate.now()).from(LocalDate.now()));

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldTriggerSystemTriggerCompleteHearingOutcome() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(caseDetails1.getId()).thenReturn(TEST_CASE_ID);
        when(caseDetails2.getId()).thenReturn(2L);
        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        task.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_TRIGGER_COMPLETE_HEARING_OUTCOME, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(2L, SYSTEM_TRIGGER_COMPLETE_HEARING_OUTCOME, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldHandleCcdManagementExceptionWhenTriggerSystemTriggerCompleteHearingOutcomeFails() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(caseDetails1.getId()).thenReturn(TEST_CASE_ID);
        when(caseDetails2.getId()).thenReturn(2L);
        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);
        doThrow(new CcdManagementException(GATEWAY_TIMEOUT.value(), "Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_TRIGGER_COMPLETE_HEARING_OUTCOME, user, SERVICE_AUTHORIZATION);

        task.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_TRIGGER_COMPLETE_HEARING_OUTCOME, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(2L, SYSTEM_TRIGGER_COMPLETE_HEARING_OUTCOME, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldHandleIllegalArgumentExceptionWhenTriggerSystemTriggerCompleteHearingOutcomeFails() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(caseDetails1.getId()).thenReturn(TEST_CASE_ID);
        when(caseDetails2.getId()).thenReturn(2L);
        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);
        doThrow(new IllegalArgumentException())
            .when(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_TRIGGER_COMPLETE_HEARING_OUTCOME, user, SERVICE_AUTHORIZATION);

        task.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_TRIGGER_COMPLETE_HEARING_OUTCOME, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(2L, SYSTEM_TRIGGER_COMPLETE_HEARING_OUTCOME, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldHandleCcdSearchCaseExceptionWhenSearchingForCases() {
        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION))
            .thenThrow(CcdSearchCaseException.class);

        task.run();

        verify(ccdUpdateService, never()).submitEvent(TEST_CASE_ID, SYSTEM_TRIGGER_COMPLETE_HEARING_OUTCOME, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldHandleCcdConflictExceptionWhenSearchingForCases() {
        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION))
            .thenThrow(CcdConflictException.class);

        task.run();

        verify(ccdUpdateService, never()).submitEvent(TEST_CASE_ID, SYSTEM_TRIGGER_COMPLETE_HEARING_OUTCOME, user, SERVICE_AUTHORIZATION);
    }
}
