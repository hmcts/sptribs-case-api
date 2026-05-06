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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.RESPONDENT_DOCUMENT_MANAGEMENT;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemMigrateInitialCaseDocuments.SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SYSTEM_UPDATE_USER_EMAIL;

@ExtendWith(MockitoExtension.class)
class SystemMigrateInitialCaseDocumentsTaskTest {

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
        }

        @Test
        void shouldDoNothingWhenTaskIsDisabled() {
            systemMigrateInitialCaseDocumentsTask.run();

            verifyNoInteractions(idamService, authTokenGenerator, caseEventRepository, ccdUpdateService);
        }
    }

    @Nested
    class WhenTaskIsEnabled {

        private User user;

        @BeforeEach
        void setUp() {
            user = new User(SYSTEM_UPDATE_AUTH_TOKEN,
                UserDetails.builder()
                    .id("test-id-123")
                    .email(TEST_SYSTEM_UPDATE_USER_EMAIL)
                    .roles(List.of("caseworker"))
                    .build());

            when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
            when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);

            ReflectionTestUtils.setField(systemMigrateInitialCaseDocumentsTask, "migrateInitialDocsEnabled", true);
            ReflectionTestUtils.setField(systemMigrateInitialCaseDocumentsTask, "migrateInitialDocsCaseRef", "");
        }

        @Test
        void shouldDoNothingWhenNoCasesFound() {
            when(caseEventRepository.getListOfCasesByEventIdDuringDateRange(
                RESPONDENT_DOCUMENT_MANAGEMENT, LocalDate.of(2025, 10, 16), LocalDate.now()))
                .thenReturn(List.of());

            systemMigrateInitialCaseDocumentsTask.run();

            verifyNoInteractions(ccdUpdateService);
        }

        @Test
        void shouldTriggerEventForEachCaseFound() {
            when(caseEventRepository.getListOfCasesByEventIdDuringDateRange(
                RESPONDENT_DOCUMENT_MANAGEMENT, LocalDate.of(2025, 10, 16), LocalDate.now()))
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
        void shouldContinueToNextCaseWhenCcdManagementExceptionThrown() {
            when(caseEventRepository.getListOfCasesByEventIdDuringDateRange(
                RESPONDENT_DOCUMENT_MANAGEMENT, LocalDate.of(2025, 10, 16), LocalDate.now()))
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
                RESPONDENT_DOCUMENT_MANAGEMENT, LocalDate.of(2025, 10, 16), LocalDate.now()))
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
                RESPONDENT_DOCUMENT_MANAGEMENT, LocalDate.of(2025, 10, 16), LocalDate.now()))
                .thenThrow(new CaseEventRepositoryException("DB error", new RuntimeException()));

            assertThatNoException().isThrownBy(() -> systemMigrateInitialCaseDocumentsTask.run());

            verifyNoInteractions(ccdUpdateService);
        }
    }
}