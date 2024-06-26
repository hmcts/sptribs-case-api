package uk.gov.hmcts.sptribs.systemupdate.schedule.migration;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.sptribs.systemupdate.schedule.migration.task.MigrateRetiredFields;
import uk.gov.hmcts.sptribs.systemupdate.schedule.migration.task.SetFailedMigrationVersionToZero;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdUpdateService;

import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uk.gov.hmcts.sptribs.ciccase.model.RetiredFields.getVersion;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemMigrateCase.SYSTEM_MIGRATE_CASE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class BaseMigrationTest {

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private MigrateRetiredFields migrateRetiredFields;

    @Mock
    private SetFailedMigrationVersionToZero setFailedMigrationVersionToZero;

    @Mock
    private FeignException feignException;

    @InjectMocks
    private BaseMigration baseMigration;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
    }

    @Test
    void shouldNotSubmitEventIfSearchFailsForBaseMigration() {
        when(ccdSearchService.searchForCasesWithVersionLessThan(getVersion(), user, SERVICE_AUTHORIZATION))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", feignException));

        baseMigration.apply(user, SERVICE_AUTHORIZATION);

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldContinueProcessingIfThereIsConflictDuringSubmissionForBaseMigration() {
        final CaseDetails caseDetails1 =
            CaseDetails.builder()
                .data(new HashMap<>())
                .id(TEST_CASE_ID)
                .build();
        final CaseDetails caseDetails2 =
            CaseDetails.builder()
                .data(new HashMap<>())
                .id(1616591401473379L)
                .build();

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForCasesWithVersionLessThan(getVersion(), user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new CcdConflictException("Case is modified by another transaction", feignException))
            .when(ccdUpdateService).submitEventWithRetry(
                caseDetails1.getId().toString(),
                SYSTEM_MIGRATE_CASE,
                migrateRetiredFields,
                user,
                SERVICE_AUTHORIZATION);

        doNothing()
            .when(ccdUpdateService).submitEventWithRetry(
                caseDetails2.getId().toString(),
                SYSTEM_MIGRATE_CASE,
                migrateRetiredFields,
                user,
                SERVICE_AUTHORIZATION);

        baseMigration.apply(user, SERVICE_AUTHORIZATION);

        verify(ccdUpdateService).submitEventWithRetry(
            caseDetails1.getId().toString(),
            SYSTEM_MIGRATE_CASE,
            migrateRetiredFields,
            user,
            SERVICE_AUTHORIZATION);

        verify(ccdUpdateService).submitEventWithRetry(
            caseDetails2.getId().toString(),
            SYSTEM_MIGRATE_CASE,
            migrateRetiredFields,
            user,
            SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldSetDataVersionToZeroIfExceptionIsThrownWhileDeserializingCaseForBaseMigration() {
        final CaseDetails caseDetails1 =
            CaseDetails.builder()
                .data(new HashMap<>())
                .id(TEST_CASE_ID)
                .build();

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1);

        when(ccdSearchService.searchForCasesWithVersionLessThan(getVersion(), user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new IllegalArgumentException("Failed to deserialize"), feignException)
            .doNothing()
            .when(ccdUpdateService).submitEventWithRetry(
                caseDetails1.getId().toString(),
                SYSTEM_MIGRATE_CASE,
                migrateRetiredFields,
                user,
                SERVICE_AUTHORIZATION);

        baseMigration.apply(user, SERVICE_AUTHORIZATION);

        verify(ccdUpdateService).submitEventWithRetry(
            caseDetails1.getId().toString(),
            SYSTEM_MIGRATE_CASE,
            migrateRetiredFields,
            user,
            SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEventWithRetry(
            caseDetails1.getId().toString(),
            SYSTEM_MIGRATE_CASE,
            setFailedMigrationVersionToZero,
            user,
            SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldSetDataVersionToZeroIfExceptionIsThrownWhilstSubmittingCcdUpdateEventForBaseMigration() {
        final CaseDetails caseDetails1 =
            CaseDetails.builder()
                .data(new HashMap<>())
                .id(TEST_CASE_ID)
                .build();

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1);

        when(ccdSearchService.searchForCasesWithVersionLessThan(getVersion(), user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new CcdManagementException(GATEWAY_TIMEOUT.value(), "Failed processing of case", feignException))
            .doNothing()
            .when(ccdUpdateService).submitEventWithRetry(
                caseDetails1.getId().toString(),
                SYSTEM_MIGRATE_CASE,
                migrateRetiredFields,
                user,
                SERVICE_AUTHORIZATION);

        baseMigration.apply(user, SERVICE_AUTHORIZATION);

        verify(ccdUpdateService).submitEventWithRetry(
            caseDetails1.getId().toString(),
            SYSTEM_MIGRATE_CASE,
            migrateRetiredFields,
            user,
            SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEventWithRetry(
            caseDetails1.getId().toString(),
            SYSTEM_MIGRATE_CASE,
            setFailedMigrationVersionToZero,
            user,
            SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldNotSetDataVersionToZeroIfExceptionIsThrownWhilstSubmittingCcdUpdateEventAndStatusIsNotFoundForBaseMigration() {
        final CaseDetails caseDetails1 =
            CaseDetails.builder()
                .data(new HashMap<>())
                .id(TEST_CASE_ID)
                .build();

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1);
        final int latestVersion = getVersion();

        when(ccdSearchService.searchForCasesWithVersionLessThan(latestVersion, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new CcdManagementException(NOT_FOUND.value(), "Failed processing of case", feignException))
            .doNothing()
            .when(ccdUpdateService).submitEventWithRetry(
                caseDetails1.getId().toString(),
                SYSTEM_MIGRATE_CASE,
                migrateRetiredFields,
                user,
                SERVICE_AUTHORIZATION);

        baseMigration.apply(user, SERVICE_AUTHORIZATION);

        verify(ccdUpdateService).submitEventWithRetry(
            caseDetails1.getId().toString(),
            SYSTEM_MIGRATE_CASE,
            migrateRetiredFields,
            user,
            SERVICE_AUTHORIZATION);

        verifyNoMoreInteractions(ccdUpdateService);
    }
}
