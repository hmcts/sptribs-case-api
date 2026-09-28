package uk.gov.hmcts.sptribs.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.sptribs.idam.CICUser;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdUpdateService;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemCreateTestCase.SYSTEM_CREATE_TEST_CASE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class SystemCreateTestCaseTaskTest {

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @InjectMocks
    private SystemCreateTestCaseTask task;

    private CICUser user;

    @BeforeEach
    void setUp() {
        user = new CICUser(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        setField(task, "createTestCaseEnabled", true);
        setField(task, "testCaseCount", 2);
    }

    @Test
    void shouldContinueCreatingCasesWhenOneCreationFails() {
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        doThrow(new CcdManagementException("Create failed", new RuntimeException()))
            .doNothing()
            .when(ccdUpdateService)
            .createCase(SYSTEM_CREATE_TEST_CASE, user, SERVICE_AUTHORIZATION);

        task.run();

        verify(ccdUpdateService, times(2))
            .createCase(SYSTEM_CREATE_TEST_CASE, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldPropagateCreationFailureForAccounting() {
        CcdManagementException exception = new CcdManagementException("Create failed", new RuntimeException());
        doThrow(exception)
            .when(ccdUpdateService)
            .createCase(SYSTEM_CREATE_TEST_CASE, user, SERVICE_AUTHORIZATION);

        assertThatThrownBy(() -> task.triggerSystemCreateTestCase(user, SERVICE_AUTHORIZATION))
            .isSameAs(exception);
    }
}
