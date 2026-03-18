package uk.gov.hmcts.sptribs.systemupdate.schedule.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.sptribs.common.repositories.CaseEventRepository;
import uk.gov.hmcts.sptribs.common.repositories.exception.CaseEventRepositoryException;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdUpdateService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemRestoreOrders.SYSTEM_RESTORE_ORDERS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SYSTEM_UPDATE_USER_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE;

@ExtendWith(MockitoExtension.class)
class SystemRestoreOrdersTaskTest {

    @InjectMocks
    private SystemRestoreOrdersTask systemRestoreOrdersTask;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CaseEventRepository caseEventRepository;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private IdamService idamService;

    private static final LocalDate START_FROM_DATE = LocalDate.of(2026, 2, 24);

    private static final LocalDate END_TO_DATE = LocalDate.of(2026, 3, 6);

    @Nested
    class WhenTaskIsDisabled {
        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(systemRestoreOrdersTask, "restoreOrdersTaskEnabled", false);
            ReflectionTestUtils.setField(systemRestoreOrdersTask, "restoreOrdersTestCaseReference", "");
        }

        @Test
        void shouldDoNothingWhenTaskIsDisabled() {
            ReflectionTestUtils.setField(systemRestoreOrdersTask, "restoreOrdersTaskEnabled", false);

            systemRestoreOrdersTask.run();

            verifyNoInteractions(idamService, authTokenGenerator, caseEventRepository, ccdUpdateService);
        }
    }

    @Nested
    class WhenTaskIsEnabled {
        private User user;

        @BeforeEach
        void setUp() {
            user = new User(SYSTEM_UPDATE_AUTH_TOKEN,
                UserDetails.builder().id("test-id-123").email(TEST_SYSTEM_UPDATE_USER_EMAIL).roles(List.of("caseworker")).build());
            when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
            when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
            ReflectionTestUtils.setField(systemRestoreOrdersTask, "restoreOrdersTaskEnabled", true);
            ReflectionTestUtils.setField(systemRestoreOrdersTask, "restoreOrdersTestCaseReference", "");
        }

        @Test
        void shouldDoNothingWhenNoCasesFound() {
            when(caseEventRepository.getListOfCasesByEventIdDuringDateRange(
                CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE, START_FROM_DATE, END_TO_DATE))
                .thenReturn(List.of());

            systemRestoreOrdersTask.run();

            verifyNoInteractions(ccdUpdateService);
        }

        @Test
        void shouldTriggerEventForEachCaseFound() {
            when(caseEventRepository.getListOfCasesByEventIdDuringDateRange(
                CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE, START_FROM_DATE, END_TO_DATE))
                .thenReturn(List.of(111L, 222L, 333L));

            systemRestoreOrdersTask.run();

            verify(ccdUpdateService).submitEvent(111L, SYSTEM_RESTORE_ORDERS, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(222L, SYSTEM_RESTORE_ORDERS, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(333L, SYSTEM_RESTORE_ORDERS, user, SERVICE_AUTHORIZATION);
        }

        @Test
        void shouldUseSingleTestCaseReferenceWhenConfigured() {
            ReflectionTestUtils.setField(systemRestoreOrdersTask, "restoreOrdersTestCaseReference", "12345");

            systemRestoreOrdersTask.run();

            verify(ccdUpdateService).submitEvent(12345L, SYSTEM_RESTORE_ORDERS, user, SERVICE_AUTHORIZATION);
            verifyNoInteractions(caseEventRepository);
        }

        @Test
        void shouldContinueToNextCaseWhenCcdManagementExceptionThrown() {
            when(caseEventRepository.getListOfCasesByEventIdDuringDateRange(
                CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE, START_FROM_DATE, END_TO_DATE))
                .thenReturn(List.of(111L, 222L, 333L));

            System.out.println("222L = " + 222L);
            System.out.println("SYSTEM_RESTORE_ORDERS = " + SYSTEM_RESTORE_ORDERS);
            System.out.println("user = " + user);
            System.out.println("SERVICE_AUTHORIZATION = " + SERVICE_AUTHORIZATION);

            lenient().doThrow(new CcdManagementException("CCD error", new RuntimeException()))
                .when(ccdUpdateService).submitEvent(222L,
                    SYSTEM_RESTORE_ORDERS,
                    user,
                    SERVICE_AUTHORIZATION);

            systemRestoreOrdersTask.run();

            verify(ccdUpdateService).submitEvent(111L, SYSTEM_RESTORE_ORDERS, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(222L, SYSTEM_RESTORE_ORDERS, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(333L, SYSTEM_RESTORE_ORDERS, user, SERVICE_AUTHORIZATION);
        }

        @Test
        void shouldContinueToNextCaseWhenIllegalArgumentExceptionThrown() {
            when(caseEventRepository.getListOfCasesByEventIdDuringDateRange(
                CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE, START_FROM_DATE, END_TO_DATE))
                .thenReturn(List.of(111L, 222L));

            doThrow(new IllegalArgumentException("Deserialization error"))
                .when(ccdUpdateService).submitEvent(eq(111L), any(), any(), any());

            systemRestoreOrdersTask.run();

            verify(ccdUpdateService).submitEvent(111L, SYSTEM_RESTORE_ORDERS, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(222L, SYSTEM_RESTORE_ORDERS, user, SERVICE_AUTHORIZATION);
        }

        @Test
        void shouldStopAndLogWhenRepositoryThrowsRuntimeException() {
            when(caseEventRepository.getListOfCasesByEventIdDuringDateRange(
                CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE, START_FROM_DATE, END_TO_DATE))
                .thenThrow(new CaseEventRepositoryException("DB error", new RuntimeException()));

            assertThatNoException().isThrownBy(() -> systemRestoreOrdersTask.run());

            verifyNoInteractions(ccdUpdateService);
        }
    }
}
