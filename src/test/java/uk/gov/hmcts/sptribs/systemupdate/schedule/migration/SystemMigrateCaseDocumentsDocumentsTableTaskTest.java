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
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.sptribs.common.repositories.exception.CaseEventRepositoryException;
import uk.gov.hmcts.sptribs.common.repositories.impl.CaseDataRepositoryImpl;
import uk.gov.hmcts.sptribs.idam.CICUser;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdUpdateService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
import static uk.gov.hmcts.sptribs.ciccase.model.State.DSS_Draft;
import static uk.gov.hmcts.sptribs.ciccase.model.State.DSS_Expired;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Draft;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemMigrateCaseDocumentsToDocTable.SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SYSTEM_UPDATE_USER_EMAIL;

@ExtendWith(MockitoExtension.class)
class SystemMigrateCaseDocumentsDocumentsTableTaskTest {

    @InjectMocks
    private SystemMigrateCaseDocumentsDocumentTableTask systemMigrateCaseDocumentsDocumentsTableTask;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CaseDataRepositoryImpl caseDataRepository;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private IdamService idamService;

    @Nested
    class WhenTaskIsDisabled {

        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(systemMigrateCaseDocumentsDocumentsTableTask, "documentTableMigrationEnabled", false);
            ReflectionTestUtils.setField(systemMigrateCaseDocumentsDocumentsTableTask, "documentTableTestCaseReference", "");
            ReflectionTestUtils.setField(systemMigrateCaseDocumentsDocumentsTableTask, "batchSize", 10);
        }

        @Test
        void shouldDoNothingWhenTaskIsDisabled() {
            systemMigrateCaseDocumentsDocumentsTableTask.run();

            verifyNoInteractions(idamService, authTokenGenerator, caseDataRepository, ccdUpdateService);
        }
    }

    @Nested
    class WhenTaskIsEnabled {

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

            ReflectionTestUtils.setField(systemMigrateCaseDocumentsDocumentsTableTask, "documentTableMigrationEnabled", true);
            ReflectionTestUtils.setField(systemMigrateCaseDocumentsDocumentsTableTask, "documentTableTestCaseReference", "");
            ReflectionTestUtils.setField(systemMigrateCaseDocumentsDocumentsTableTask, "batchSize", 10);
        }

        @Test
        void shouldDoNothingWhenNoCasesFound() {
            when(caseDataRepository.returnAllCasesExlcudingStates(
                List.of(DSS_Draft.name(), Draft.name(), DSS_Expired.name())))
                .thenReturn(List.of());

            systemMigrateCaseDocumentsDocumentsTableTask.run();

            verifyNoInteractions(ccdUpdateService);
        }

        @Test
        void shouldTriggerEventForEachCaseFound() {
            when(caseDataRepository.returnAllCasesExlcudingStates(
                List.of(DSS_Draft.name(), Draft.name(), DSS_Expired.name())))
                .thenReturn(List.of(111L, 222L, 333L));

            systemMigrateCaseDocumentsDocumentsTableTask.run();

            verify(ccdUpdateService).submitEvent(111L, SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(222L, SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(333L, SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE, user, SERVICE_AUTHORIZATION);
        }

        @Test
        void shouldUseSingleTestCaseReferenceWhenConfigured() {
            ReflectionTestUtils.setField(systemMigrateCaseDocumentsDocumentsTableTask, "documentTableTestCaseReference", "12345");

            systemMigrateCaseDocumentsDocumentsTableTask.run();

            verify(ccdUpdateService).submitEvent(12345L, SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE, user, SERVICE_AUTHORIZATION);
            verifyNoInteractions(caseDataRepository);
        }

        @Test
        void shouldUseListOfReferencesWhenConfigured() {
            ReflectionTestUtils.setField(systemMigrateCaseDocumentsDocumentsTableTask, "documentTableTestCaseReference", "12345,54321");

            systemMigrateCaseDocumentsDocumentsTableTask.run();

            verify(ccdUpdateService).submitEvent(12345L, SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(54321L, SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE, user, SERVICE_AUTHORIZATION);
            verifyNoInteractions(caseDataRepository);
        }

        @Test
        void shouldContinueToNextCaseWhenCcdManagementExceptionThrown() {
            when(caseDataRepository.returnAllCasesExlcudingStates(
                List.of(DSS_Draft.name(), Draft.name(), DSS_Expired.name())))
                .thenReturn(List.of(111L, 222L, 333L));

            lenient().doThrow(new CcdManagementException("CCD error", new RuntimeException()))
                .when(ccdUpdateService).submitEvent(222L, SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE, user, SERVICE_AUTHORIZATION);

            systemMigrateCaseDocumentsDocumentsTableTask.run();

            verify(ccdUpdateService).submitEvent(111L, SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(222L, SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(333L, SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE, user, SERVICE_AUTHORIZATION);
        }

        @Test
        void shouldContinueToNextCaseWhenIllegalArgumentExceptionThrown() {
            when(caseDataRepository.returnAllCasesExlcudingStates(
                List.of(DSS_Draft.name(), Draft.name(), DSS_Expired.name())))
                .thenReturn(List.of(111L, 222L));

            lenient().doThrow(new IllegalArgumentException("Deserialisation error"))
                .when(ccdUpdateService).submitEvent(111L, SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE, user, SERVICE_AUTHORIZATION);

            systemMigrateCaseDocumentsDocumentsTableTask.run();

            verify(ccdUpdateService).submitEvent(111L, SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(222L, SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE, user, SERVICE_AUTHORIZATION);
        }

        @Test
        void shouldStopAndLogWhenRepositoryThrowsRuntimeException() {
            when(caseDataRepository.returnAllCasesExlcudingStates(
                List.of(DSS_Draft.name(), Draft.name(), DSS_Expired.name())))
                .thenThrow(new CaseEventRepositoryException("DB error", new RuntimeException()));

            assertThatNoException().isThrownBy(() -> systemMigrateCaseDocumentsDocumentsTableTask.run());

            verifyNoInteractions(ccdUpdateService);
        }

        @Test
        void shouldProcessAllCasesInBatches() {
            ReflectionTestUtils.setField(systemMigrateCaseDocumentsDocumentsTableTask, "batchSize", 2);
            ReflectionTestUtils.setField(systemMigrateCaseDocumentsDocumentsTableTask, "batchPauseMs", 100L);

            when(caseDataRepository.returnAllCasesExlcudingStates(
                List.of(DSS_Draft.name(), Draft.name(), DSS_Expired.name())))
                .thenReturn(List.of(111L, 222L, 333L, 444L, 555L));

            systemMigrateCaseDocumentsDocumentsTableTask.run();

            verify(ccdUpdateService).submitEvent(111L, SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(222L, SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(333L, SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(444L, SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(555L, SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE, user, SERVICE_AUTHORIZATION);
            verifyNoMoreInteractions(ccdUpdateService);
        }

        @Test
        void shouldContinueProcessingWhenCaseFails() {
            ReflectionTestUtils.setField(systemMigrateCaseDocumentsDocumentsTableTask, "batchSize", 2);
            ReflectionTestUtils.setField(systemMigrateCaseDocumentsDocumentsTableTask, "batchPauseMs", 100L);

            when(caseDataRepository.returnAllCasesExlcudingStates(
                List.of(DSS_Draft.name(), Draft.name(), DSS_Expired.name())))
                .thenReturn(List.of(111L, 222L, 333L));

            doThrow(new RuntimeException("service error"))
                .when(ccdUpdateService).submitEvent(eq(222L), any(), any(), any());

            systemMigrateCaseDocumentsDocumentsTableTask.run();

            verify(ccdUpdateService).submitEvent(111L, SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(222L, SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(333L, SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE, user, SERVICE_AUTHORIZATION);
        }

        @Test
        void shouldNotPauseAfterFinalBatch() {
            ReflectionTestUtils.setField(systemMigrateCaseDocumentsDocumentsTableTask, "batchSize", 3);
            ReflectionTestUtils.setField(systemMigrateCaseDocumentsDocumentsTableTask, "batchPauseMs", 100L);

            when(caseDataRepository.returnAllCasesExlcudingStates(
                List.of(DSS_Draft.name(), Draft.name(), DSS_Expired.name())))
                .thenReturn(List.of(111L, 222L, 333L));

            long start = System.currentTimeMillis();
            systemMigrateCaseDocumentsDocumentsTableTask.run();
            long elapsed = System.currentTimeMillis() - start;

            assertThat(elapsed).isLessThan(100L);

            verify(ccdUpdateService, times(3)).submitEvent(anyLong(), any(), any(), any());
        }

        @Test
        void shouldStopProcessingWhenInterrupted() {
            ReflectionTestUtils.setField(systemMigrateCaseDocumentsDocumentsTableTask, "batchSize", 2);
            ReflectionTestUtils.setField(systemMigrateCaseDocumentsDocumentsTableTask, "batchPauseMs", 5000L);

            when(caseDataRepository.returnAllCasesExlcudingStates(
                List.of(DSS_Draft.name(), Draft.name(), DSS_Expired.name())))
                .thenReturn(List.of(111L, 222L, 333L, 444L));

            doAnswer(invocation -> {
                Thread.currentThread().interrupt();
                return null;
            }).when(ccdUpdateService).submitEvent(eq(222L), any(), any(), any());

            systemMigrateCaseDocumentsDocumentsTableTask.run();

            verify(ccdUpdateService).submitEvent(111L, SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE, user, SERVICE_AUTHORIZATION);
            verify(ccdUpdateService).submitEvent(222L, SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE, user, SERVICE_AUTHORIZATION);

            verify(ccdUpdateService, never()).submitEvent(eq(333L), any(), any(), any());
            verify(ccdUpdateService, never()).submitEvent(eq(444L), any(), any(), any());

            Thread.interrupted();
        }
    }
}
