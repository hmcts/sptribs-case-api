package uk.gov.hmcts.sptribs.idam;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

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

    @Test
    public void shouldRetrieveUserWhenValidAuthorizationTokenIsPassed() {
        //Given
        when(idamClient.getUserDetails(SYSTEM_UPDATE_AUTH_TOKEN))
            .thenReturn(userDetails());

        //When&Then
        assertThatCode(() -> idamService.retrieveUser(SYSTEM_UPDATE_AUTH_TOKEN))
            .doesNotThrowAnyException();

        verify(idamClient).getUserDetails(SYSTEM_UPDATE_AUTH_TOKEN);
        verifyNoMoreInteractions(idamClient);
    }

    @Test
    public void shouldThrowFeignUnauthorizedExceptionWhenInValidAuthorizationTokenIsPassed() {
        //When&Then
        doThrow(feignException(401, "Failed to retrieve Idam user"))
            .when(idamClient).getUserDetails("Bearer invalid_token");

        assertThatThrownBy(() -> idamService.retrieveUser(INVALID_AUTH_TOKEN))
            .isExactlyInstanceOf(FeignException.Unauthorized.class)
            .hasMessageContaining("Failed to retrieve Idam user");
    }

    @Test
    public void shouldNotThrowExceptionAndRetrieveSystemUpdateUserSuccessfully() {
        //Given
        setSystemUserCredentials();

        when(idamClient.getAccessToken(TEST_SYSTEM_UPDATE_USER_EMAIL, TEST_SYSTEM_USER_PASSWORD))
            .thenReturn(SYSTEM_UPDATE_AUTH_TOKEN);

        when(idamClient.getUserDetails(SYSTEM_UPDATE_AUTH_TOKEN))
            .thenReturn(userDetails());

        //When&Then
        assertThatCode(() -> idamService.retrieveSystemUpdateUserDetails())
            .doesNotThrowAnyException();

        verify(idamClient).getAccessToken(TEST_SYSTEM_UPDATE_USER_EMAIL, TEST_SYSTEM_USER_PASSWORD);
        verify(idamClient).getUserDetails(SYSTEM_UPDATE_AUTH_TOKEN);
        verifyNoMoreInteractions(idamClient);
    }

    @Test
    public void shouldThrowFeignUnauthorizedExceptionWhenSystemUpdateUserCredentialsAreInvalid() {
        //Given
        setSystemUserCredentials();

        doThrow(feignException(401, "Failed to retrieve Idam user"))
            .when(idamClient).getAccessToken(TEST_SYSTEM_UPDATE_USER_EMAIL, TEST_SYSTEM_USER_PASSWORD);

        //When&Then
        assertThatThrownBy(() -> idamService.retrieveSystemUpdateUserDetails())
            .isExactlyInstanceOf(FeignException.Unauthorized.class)
            .hasMessageContaining("Failed to retrieve Idam user");
    }

    @Test
    void shouldRetrieveUserDetails() {
        // Given
        final String bearerToken = TEST_SERVICE_AUTH_TOKEN;
        final UserDetails userDetails = userDetails();

        // When
        when(idamClient.getUserDetails(bearerToken)).thenReturn(userDetails());

        final User result = idamService.retrieveUser(bearerToken);

        //Then
        assertEquals(userDetails, result.getUserDetails());
        assertEquals(bearerToken, result.getAuthToken());
    }

    private void setSystemUserCredentials() {
        ReflectionTestUtils.setField(idamService, "systemUpdateUserName", TEST_SYSTEM_UPDATE_USER_EMAIL);
        ReflectionTestUtils.setField(idamService, "systemUpdatePassword", TEST_SYSTEM_USER_PASSWORD);
    }

    private UserDetails userDetails() {
        return UserDetails
            .builder()
            .id(SYSTEM_USER_USER_ID)
            .email(TEST_SYSTEM_UPDATE_USER_EMAIL)
            .build();
    }
}
