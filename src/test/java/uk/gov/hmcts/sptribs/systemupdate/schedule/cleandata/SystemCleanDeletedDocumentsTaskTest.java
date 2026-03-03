package uk.gov.hmcts.sptribs.systemupdate.schedule.cleandata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.systemupdate.repository.CaseEventRepository;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdUpdateService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class SystemCleanDeletedDocumentsTaskTest {

    @Mock
    private CcdUpdateService ccdUpdateService;
    @Mock
    private IdamService idamService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CaseEventRepository caseEventRepository;
    @InjectMocks
    private SystemCleanDeletedDocumentsTask systemCleanDeletedDocumentsTask;

    private User user;
    private final Long caseId1 = 101L;

    private static final String CASE_EVENT_ID = "caseworker-remove-document";
    private static final LocalDate DELETE_FROM_DATE = LocalDate.of(2025,10,1);
    public static final String SYSTEM_CLEAN_DELETED_DOCUMENTS = "system-clean-deleted-documents";


    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    void whenCleanDeletedDocumentsTask_thenSuccessfullyCleanDocs() {

        //given
        Long caseId2 = 202L;
        when(caseEventRepository.getListOfCasesByEventTypeAndDate(CASE_EVENT_ID, DELETE_FROM_DATE))
            .thenReturn(List.of(caseId1, caseId2));
        //when
        systemCleanDeletedDocumentsTask.run();
        //then
        verify(ccdUpdateService).submitEvent(caseId1, SYSTEM_CLEAN_DELETED_DOCUMENTS, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(caseId2, SYSTEM_CLEAN_DELETED_DOCUMENTS, user, SERVICE_AUTHORIZATION);

    }

    @Test
    void whenCleanDeletedDocumentsTask_thenEmptyListFromRepositoryAndDoNothing() {

        //given
        when(caseEventRepository.getListOfCasesByEventTypeAndDate(CASE_EVENT_ID, DELETE_FROM_DATE))
            .thenReturn(Collections.emptyList());
        //when
        systemCleanDeletedDocumentsTask.run();
        //then
        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void whenCleanDeletedDocumentsTask_thenHandleRuntimeException() {

        // given
        doThrow(new RuntimeException("exception"))
            .when(caseEventRepository)
            .getListOfCasesByEventTypeAndDate(CASE_EVENT_ID, DELETE_FROM_DATE);

        // when + then
        assertDoesNotThrow(() -> systemCleanDeletedDocumentsTask.run());
        verifyNoInteractions(ccdUpdateService);

    }

    @Test
    void whenCleanDeletedDocumentsTask_thenHandleCcdManagementException() {

        //given
        when(caseEventRepository.getListOfCasesByEventTypeAndDate(CASE_EVENT_ID, DELETE_FROM_DATE))
            .thenReturn(List.of(caseId1));

        doThrow(new CcdManagementException("exception", new RuntimeException()))
            .when(ccdUpdateService)
                .submitEvent(caseId1, SYSTEM_CLEAN_DELETED_DOCUMENTS, user, SERVICE_AUTHORIZATION);

        //when + then
        assertDoesNotThrow(() -> systemCleanDeletedDocumentsTask.run());

    }

    @Test
    void whenCleanDeletedDocumentsTask_thenHandleIllegalArgumentException() {

        //given
        when(caseEventRepository.getListOfCasesByEventTypeAndDate(CASE_EVENT_ID, DELETE_FROM_DATE))
            .thenReturn(List.of(caseId1));

        doThrow(new IllegalArgumentException("exception"))
            .when(ccdUpdateService)
            .submitEvent(caseId1, SYSTEM_CLEAN_DELETED_DOCUMENTS, user, SERVICE_AUTHORIZATION);

        //when + then
        assertDoesNotThrow(() -> systemCleanDeletedDocumentsTask.run());

    }


}
