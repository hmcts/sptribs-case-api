package uk.gov.hmcts.sptribs.common.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.idam.IdamService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.BEARER;

@ExtendWith(MockitoExtension.class)
class AuthorisationServiceTest {
    @InjectMocks
    private AuthorisationService authorisationService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private IdamService idamService;

    @Mock
    private User user;

    private static final String TEST_TOKEN = "token";
    private static final String TEST_BEARER_TOKEN = BEARER + TEST_TOKEN;

    @Test
    void shouldAddBearerTokenToAuthorisation() {
        //Given
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_TOKEN);
        when(idamService.retrieveUser(any())).thenReturn(user);
        when(user.getAuthToken()).thenReturn(TEST_TOKEN);

        //When
        String response = authorisationService.getAuthorisation();

        //Then
        assertThat(response).isEqualTo(TEST_BEARER_TOKEN);
    }

    @Test
    void shouldNotAddBearerTokenToAuthorisation() {
        //Given
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_BEARER_TOKEN);
        when(idamService.retrieveUser(any())).thenReturn(user);
        when(user.getAuthToken()).thenReturn(TEST_BEARER_TOKEN);

        //When
        String response = authorisationService.getAuthorisation();

        //Then
        assertThat(response).isEqualTo(TEST_BEARER_TOKEN);
    }
}
