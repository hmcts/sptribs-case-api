package uk.gov.hmcts.sptribs.systemupdate.schedule.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.sptribs.common.repositories.CaseEventRepository;
import uk.gov.hmcts.sptribs.common.repositories.exception.CaseEventRepositoryException;
import uk.gov.hmcts.sptribs.idam.CICUser;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdUpdateService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.RESPONDENT_DOCUMENT_MANAGEMENT;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemMigrateInitialCaseDocuments.SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SYSTEM_UPDATE_USER_EMAIL;

@ExtendWith(MockitoExtension.class)
class SystemMigrateInitialCaseDocumentsTaskTest {

    public static final LocalDate AFFECTED_START_DATE = LocalDate.of(2025, 10, 16);

    @InjectMocks
    private SystemMigrateInitialCaseDocumentsTask systemMigrateInitialCaseDocumentsTask;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CaseEventRepository caseEventRepository;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private IdamService idamService;

    @Nested
    class WhenTaskIsDisabled {

        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(systemMigrateInitialCaseDocumentsTask, "migrateInitialDocsEnabled", false);
            ReflectionTestUtils.setField(systemMigrateInitialCaseDocumentsTask, "migrateInitialDocsCaseRef", "");
            ReflectionTestUtils.setField(systemMigrateInitialCaseDocumentsTask, "skipMigrated", false);
            ReflectionTestUtils.setField(systemMigrateInitialCaseDocumentsTask, "batchSize", 10);
        }

        @Test
        void shouldDoNothingWhenTaskIsDisabled() {
            systemMigrateInitialCaseDocumentsTask.run();

            verifyNoInteractions(idamService, authTokenGenerator, caseEventRepository, ccdUpdateService);
        }
    }

    @Nested
    class WhenTaskIsEnabled {

        @Captor
        private ArgumentCaptor<String> caseEventIdCaptor;

        @Captor
        private ArgumentCaptor<LocalDate> startDateCaptor;

        @Captor
        private ArgumentCaptor<LocalDate> endDateCaptor;

        @Captor
        private ArgumentCaptor<String> skipEventIdCaptor;

        private CICUser user;

        @BeforeEach
        void setUp() {
            user = new CICUser(SYSTEM_UPDATE_AUTH_TOKEN,
                UserInfo.builder()
                    .uid("test-id-123")
                    .sub(TEST_SYSTEM_UPDATE_USER_EMAIL)
                    .roles(List.of("caseworker"))
                    .build());

            when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
            when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);

            ReflectionTestUtils.setField(systemMigrateInitialCaseDocumentsTask, "migrateInitialDocsEnabled", true);
            ReflectionTestUtils.setField(systemMigrateInitialCaseDocumentsTask, "migrateInitialDocsCaseRef", "");
            ReflectionTestUtils.setField(systemMigrateInitialCaseDocumentsTask, "skipMigrated", false);
            ReflectionTestUtils.setField(systemMigrateInitialCaseDocumentsTask, "batchSize", 10);
        }

        @Test
        void shouldDoNothingWhenNoCasesFound() {
            when(caseEventRepository.getListOfCasesByEventIdDuringDateRange(
                RESPONDENT_DOCUMENT_MANAGEMENT, AFFECTED_START_DATE, LocalDate.now()))
                .thenReturn(List.of());

            systemMigrateInitialCaseDocumentsTask.run();

            verifyNoInteractions(ccdUpdateService);
        }

        @Test
        void shouldTriggerEventForEachCaseFound() {
            when(caseEventRepository.getListOfCasesByEventIdDuringDateRange(
                RESPONDENT_DOCUMENT_MANAGEMENT, AFFECTED_START_DATE, LocalDate.now()))
                .thenReturn(List.of(111L, 222L, 333L));

            systemMigrateInitialCaseDocumentsTask.run();

            verify(ccdUpdateService).submitEvent(111L, SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(222L, SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(333L, SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS, user, SERVICE_AUTHORIZATION);
        }

        @Test
        void shouldUseSingleTestCaseReferenceWhenConfigured() {
            ReflectionTestUtils.setField(systemMigrateInitialCaseDocumentsTask, "migrateInitialDocsCaseRef", "12345");

            systemMigrateInitialCaseDocumentsTask.run();

            verify(ccdUpdateService).submitEvent(12345L, SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS, user, SERVICE_AUTHORIZATION);
            verifyNoInteractions(caseEventRepository);
        }

        @Test
        void shouldUseListOfReferencesWhenConfigured() {
            ReflectionTestUtils.setField(systemMigrateInitialCaseDocumentsTask, "migrateInitialDocsCaseRef", "12345,54321");

            systemMigrateInitialCaseDocumentsTask.run();

            verify(ccdUpdateService).submitEvent(12345L, SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(54321L, SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS, user, SERVICE_AUTHORIZATION);
            verifyNoInteractions(caseEventRepository);
        }

        @Test
        void shouldContinueToNextCaseWhenCcdManagementExceptionThrown() {
            when(caseEventRepository.getListOfCasesByEventIdDuringDateRange(
                RESPONDENT_DOCUMENT_MANAGEMENT, AFFECTED_START_DATE, LocalDate.now()))
                .thenReturn(List.of(111L, 222L, 333L));

            lenient().doThrow(new CcdManagementException("CCD error", new RuntimeException()))
                .when(ccdUpdateService).submitEvent(222L, SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS, user, SERVICE_AUTHORIZATION);

            systemMigrateInitialCaseDocumentsTask.run();

            verify(ccdUpdateService).submitEvent(111L, SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(222L, SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(333L, SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS, user, SERVICE_AUTHORIZATION);
        }

        @Test
        void shouldContinueToNextCaseWhenIllegalArgumentExceptionThrown() {
            when(caseEventRepository.getListOfCasesByEventIdDuringDateRange(
                RESPONDENT_DOCUMENT_MANAGEMENT, AFFECTED_START_DATE, LocalDate.now()))
                .thenReturn(List.of(111L, 222L));

            lenient().doThrow(new IllegalArgumentException("Deserialisation error"))
                .when(ccdUpdateService).submitEvent(111L, SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS, user, SERVICE_AUTHORIZATION);

            systemMigrateInitialCaseDocumentsTask.run();

            verify(ccdUpdateService).submitEvent(111L, SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(222L, SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS, user, SERVICE_AUTHORIZATION);
        }

        @Test
        void shouldStopAndLogWhenRepositoryThrowsRuntimeException() {
            when(caseEventRepository.getListOfCasesByEventIdDuringDateRange(
                RESPONDENT_DOCUMENT_MANAGEMENT, AFFECTED_START_DATE, LocalDate.now()))
                .thenThrow(new CaseEventRepositoryException("DB error", new RuntimeException()));

            assertThatNoException().isThrownBy(() -> systemMigrateInitialCaseDocumentsTask.run());

            verifyNoInteractions(ccdUpdateService);
        }

        @Test
        void shouldSkipAlreadyMigratedCases() {
            ReflectionTestUtils.setField(systemMigrateInitialCaseDocumentsTask, "skipMigrated", true);

            when(caseEventRepository.getListOfCasesByEventIdDuringDateRange(
                anyString(), any(LocalDate.class), any(LocalDate.class), any()))
                .thenReturn(List.of(111L, 222L, 333L));

            systemMigrateInitialCaseDocumentsTask.run();

            verify(caseEventRepository).getListOfCasesByEventIdDuringDateRange(
                caseEventIdCaptor.capture(),
                startDateCaptor.capture(),
                endDateCaptor.capture(),
                skipEventIdCaptor.capture());

            assertThat(caseEventIdCaptor.getValue()).isEqualTo(RESPONDENT_DOCUMENT_MANAGEMENT);
            assertThat(startDateCaptor.getValue()).isEqualTo(AFFECTED_START_DATE);
            assertThat(skipEventIdCaptor.getValue()).isEqualTo(SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS);

            verify(ccdUpdateService).submitEvent(111L, SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(222L, SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(333L, SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS, user, SERVICE_AUTHORIZATION);
        }

        @Test
        void shouldProcessAllCasesInBatches() {
            ReflectionTestUtils.setField(systemMigrateInitialCaseDocumentsTask, "batchSize", 2);
            ReflectionTestUtils.setField(systemMigrateInitialCaseDocumentsTask, "batchPauseMs", 100L);
            ReflectionTestUtils.setField(systemMigrateInitialCaseDocumentsTask, "skipMigrated", true);

            when(caseEventRepository.getListOfCasesByEventIdDuringDateRange(
                anyString(), any(LocalDate.class), any(LocalDate.class), any()))
                .thenReturn(List.of(111L, 222L, 333L, 444L, 555L));

            systemMigrateInitialCaseDocumentsTask.run();

            verify(ccdUpdateService).submitEvent(111L, SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(222L, SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(333L, SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(444L, SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(555L, SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS, user, SERVICE_AUTHORIZATION);
            verifyNoMoreInteractions(ccdUpdateService);
        }

        @Test
        void shouldContinueProcessingWhenCaseFails() {
            ReflectionTestUtils.setField(systemMigrateInitialCaseDocumentsTask, "batchSize", 2);
            ReflectionTestUtils.setField(systemMigrateInitialCaseDocumentsTask, "batchPauseMs", 100L);
            ReflectionTestUtils.setField(systemMigrateInitialCaseDocumentsTask, "skipMigrated", true);

            when(caseEventRepository.getListOfCasesByEventIdDuringDateRange(
                anyString(), any(LocalDate.class), any(LocalDate.class), any()))
                .thenReturn(List.of(111L, 222L, 333L));

            doThrow(new RuntimeException("service error"))
                .when(ccdUpdateService).submitEvent(eq(222L), any(), any(), any());

            systemMigrateInitialCaseDocumentsTask.run();

            verify(ccdUpdateService).submitEvent(111L, SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(222L, SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(333L, SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS, user, SERVICE_AUTHORIZATION);
        }

        @Test
        void shouldNotPauseAfterFinalBatch() {
            ReflectionTestUtils.setField(systemMigrateInitialCaseDocumentsTask, "batchSize", 3);
            ReflectionTestUtils.setField(systemMigrateInitialCaseDocumentsTask, "batchPauseMs", 100L);
            ReflectionTestUtils.setField(systemMigrateInitialCaseDocumentsTask, "skipMigrated", true);

            when(caseEventRepository.getListOfCasesByEventIdDuringDateRange(
                anyString(), any(LocalDate.class), any(LocalDate.class), any()))
                .thenReturn(List.of(111L, 222L, 333L));

            long start = System.currentTimeMillis();
            systemMigrateInitialCaseDocumentsTask.run();
            long elapsed = System.currentTimeMillis() - start;

            assertThat(elapsed).isLessThan(100L);

            verify(ccdUpdateService, times(3)).submitEvent(anyLong(), any(), any(), any());
        }

        @Test
        void shouldStopProcessingWhenInterrupted() {
            ReflectionTestUtils.setField(systemMigrateInitialCaseDocumentsTask, "batchSize", 2);
            ReflectionTestUtils.setField(systemMigrateInitialCaseDocumentsTask, "batchPauseMs", 5000L);
            ReflectionTestUtils.setField(systemMigrateInitialCaseDocumentsTask, "skipMigrated", true);

            when(caseEventRepository.getListOfCasesByEventIdDuringDateRange(
                anyString(), any(LocalDate.class), any(LocalDate.class), any()))
                .thenReturn(List.of(111L, 222L, 333L, 444L));

            doAnswer(invocation -> {
                Thread.currentThread().interrupt();
                return null;
            }).when(ccdUpdateService).submitEvent(eq(222L), any(), any(), any());

            systemMigrateInitialCaseDocumentsTask.run();

            verify(ccdUpdateService).submitEvent(111L, SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(222L, SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS, user, SERVICE_AUTHORIZATION);

            verify(ccdUpdateService, never()).submitEvent(eq(333L), any(), any(), any());
            verify(ccdUpdateService, never()).submitEvent(eq(444L), any(), any(), any());

            Thread.interrupted();
        }
    }
}
