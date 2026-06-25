package uk.gov.hmcts.sptribs.idam;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.*;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.INVALID_AUTH_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SYSTEM_UPDATE_USER_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SYSTEM_USER_PASSWORD;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.feignException;

@ExtendWith(MockitoExtension.class)
public class IdamServiceTest {

    @InjectMocks
    private IdamService idamService;

    @Mock
    private IdamClient idamClient;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(idamService, "systemUpdateUserName", TEST_SYSTEM_UPDATE_USER_EMAIL);
        ReflectionTestUtils.setField(idamService, "systemUpdatePassword", TEST_SYSTEM_USER_PASSWORD);
    }

    @Test
    void shouldRetrieveUserDetails() {
        final String bearerToken = TEST_SERVICE_AUTH_TOKEN;
        final UserInfo userInfo = userInfo();

        when(idamClient.getUserInfo(bearerToken)).thenReturn(userInfo());

        final CICUser result = idamService.retrieveUser(bearerToken);

        assertEquals(userInfo, result.getUserInfo());
        assertEquals(bearerToken, result.getAuthToken());
    }

    @Test
    public void shouldRetrieveUserWhenValidAuthorizationTokenIsPassed() {
        when(idamClient.getUserInfo(SYSTEM_UPDATE_AUTH_TOKEN))
            .thenReturn(userInfo());

        assertThatCode(() -> idamService.retrieveUser(SYSTEM_UPDATE_AUTH_TOKEN))
            .doesNotThrowAnyException();

        verify(idamClient).getUserInfo(SYSTEM_UPDATE_AUTH_TOKEN);
        verifyNoMoreInteractions(idamClient);
    }

    @Test
    public void shouldThrowFeignUnauthorizedExceptionWhenInValidAuthorizationTokenIsPassed() {
        doThrow(feignException(401, "Failed to retrieve Idam user"))
            .when(idamClient).getUserInfo("Bearer invalid_token");

        assertThatThrownBy(() -> idamService.retrieveUser(INVALID_AUTH_TOKEN))
            .isExactlyInstanceOf(FeignException.Unauthorized.class)
            .hasMessageContaining("Failed to retrieve Idam user");
    }

    @Test
    public void shouldNotThrowExceptionAndRetrieveSystemUpdateUserSuccessfully() {
        when(idamClient.getAccessToken(TEST_SYSTEM_UPDATE_USER_EMAIL, TEST_SYSTEM_USER_PASSWORD))
            .thenReturn(SYSTEM_UPDATE_AUTH_TOKEN);

        when(idamClient.getUserInfo(SYSTEM_UPDATE_AUTH_TOKEN))
            .thenReturn(userInfo());

        assertThatCode(() -> idamService.retrieveSystemUpdateUserDetails())
            .doesNotThrowAnyException();

        verify(idamClient).getAccessToken(TEST_SYSTEM_UPDATE_USER_EMAIL, TEST_SYSTEM_USER_PASSWORD);
        verify(idamClient).getUserInfo(SYSTEM_UPDATE_AUTH_TOKEN);
        verifyNoMoreInteractions(idamClient);
    }

    @Test
    public void shouldThrowFeignUnauthorizedExceptionWhenSystemUpdateUserCredentialsAreInvalid() {
        doThrow(feignException(401, "Failed to retrieve Idam user"))
            .when(idamClient).getAccessToken(TEST_SYSTEM_UPDATE_USER_EMAIL, TEST_SYSTEM_USER_PASSWORD);

        assertThatThrownBy(() -> idamService.retrieveSystemUpdateUserDetails())
            .isExactlyInstanceOf(FeignException.Unauthorized.class)
            .hasMessageContaining("Failed to retrieve Idam user");
    }

    private UserInfo userInfo() {
        return UserInfo
            .builder()
            .uid(SYSTEM_USER_USER_ID)
            .sub(TEST_SYSTEM_UPDATE_USER_EMAIL)
            .build();
    }
}
