package uk.gov.hmcts.sptribs.systemupdate.schedule.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.sptribs.common.repositories.CaseEventRepository;
import uk.gov.hmcts.sptribs.idam.CICUser;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdUpdateService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemMigrateFirstHearingDateCase.SYSTEM_MIGRATE_FIRST_HEARING_DATE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;


@ExtendWith(MockitoExtension.class)
class SystemMigrateFirstHearingDateTaskTest {

    @Mock
    private CcdUpdateService ccdUpdateService;
    @Mock
    private IdamService idamService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CaseEventRepository caseEventRepository;
    @InjectMocks
    private SystemMigrateFirstHearingDateTask systemMigrateFirstHearingDateTask;

    private CICUser user;
    private final Long caseId1 = 101L;
    private final Long caseId2 = 202L;


    @Test
    void whenFixFirstHearingDateTask_thenSuccessfullyFixesCases() {

        //Given
        initMocks();
        when(caseEventRepository.getListOfCasesWithFirstHearingDateAsString())
            .thenReturn(List.of(caseId1, caseId2));
        //When
        systemMigrateFirstHearingDateTask.run();
        //Then
        verify(ccdUpdateService).submitEvent(caseId1, SYSTEM_MIGRATE_FIRST_HEARING_DATE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(caseId2, SYSTEM_MIGRATE_FIRST_HEARING_DATE, user, SERVICE_AUTHORIZATION);

    }

    @Test
    void whenFixFirstHearingDateTaskAndRepositoryReturnsNoCases_thenNothingFixed() {

        //Given
        initMocks();
        when(caseEventRepository.getListOfCasesWithFirstHearingDateAsString())
            .thenReturn(Collections.emptyList());
        //When
        systemMigrateFirstHearingDateTask.run();
        //Then
        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void whenFixFirstHearingDateTaskAndFlagIsFalse_thenNothingFixed() {

        //Given
        ReflectionTestUtils.setField(systemMigrateFirstHearingDateTask, "fixFirstHearingDateEnabled", false);
        //When
        systemMigrateFirstHearingDateTask.run();
        //Then
        verifyNoInteractions(caseEventRepository);
        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void whenFixFirstHearingDateTaskWithTestCaseId_thenSuccessfullyFixesTestCaseId() {

        //Given
        initMocks();
        ReflectionTestUtils.setField(systemMigrateFirstHearingDateTask, "fixFirstHearingDateTestCaseReference", "12345");
        //When
        systemMigrateFirstHearingDateTask.run();
        //Then
        verify(ccdUpdateService).submitEvent(12345L, SYSTEM_MIGRATE_FIRST_HEARING_DATE, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void whenFixFirstHearingDateTaskAndRepositoryThrowsRuntimeException_thenHandleExceptionAndNoCasesFixed() {

        //Given
        initMocks();
        doThrow(new RuntimeException("exception"))
            .when(caseEventRepository)
            .getListOfCasesWithFirstHearingDateAsString();

        //When + then
        assertDoesNotThrow(() -> systemMigrateFirstHearingDateTask.run());
        verifyNoInteractions(ccdUpdateService);

    }

    @Test
    void whenFixFirstHearingDateTaskAndUpdateServiceThrowsCCDManagementException_thenHandleExceptionAndProcessNextCase() {

        //Given
        initMocks();
        when(caseEventRepository.getListOfCasesWithFirstHearingDateAsString())
            .thenReturn(List.of(caseId1, caseId2));

        doThrow(new CcdManagementException("exception", new RuntimeException()))
            .when(ccdUpdateService)
            .submitEvent(caseId1, SYSTEM_MIGRATE_FIRST_HEARING_DATE, user, SERVICE_AUTHORIZATION);

        //When + then
        assertDoesNotThrow(() -> systemMigrateFirstHearingDateTask.run());
        verify(ccdUpdateService).submitEvent(caseId1, SYSTEM_MIGRATE_FIRST_HEARING_DATE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(caseId2, SYSTEM_MIGRATE_FIRST_HEARING_DATE, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void whenFixFirstHearingDateTaskAndUpdateServiceThrowsIllegalArgumentException_thenHandleExceptionAndMoveToNextCase() {

        //Given
        initMocks();
        when(caseEventRepository.getListOfCasesWithFirstHearingDateAsString())
            .thenReturn(List.of(caseId1, caseId2));

        doThrow(new IllegalArgumentException("exception"))
            .when(ccdUpdateService)
            .submitEvent(caseId1, SYSTEM_MIGRATE_FIRST_HEARING_DATE, user, SERVICE_AUTHORIZATION);

        //When + then
        assertDoesNotThrow(() -> systemMigrateFirstHearingDateTask.run());
        verify(ccdUpdateService).submitEvent(caseId1, SYSTEM_MIGRATE_FIRST_HEARING_DATE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(caseId2, SYSTEM_MIGRATE_FIRST_HEARING_DATE, user, SERVICE_AUTHORIZATION);
    }

    private void initMocks() {
        user = new CICUser(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        ReflectionTestUtils.setField(systemMigrateFirstHearingDateTask, "fixFirstHearingDateTestCaseReference", "");
        ReflectionTestUtils.setField(systemMigrateFirstHearingDateTask, "fixFirstHearingDateEnabled", true);
    }


}
