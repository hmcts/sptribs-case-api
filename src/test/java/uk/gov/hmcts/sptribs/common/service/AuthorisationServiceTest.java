package uk.gov.hmcts.sptribs.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.idam.IdamService;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
public class AuthorisationServiceTest {
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

    @Test
    void shouldGetAuthorisation() {
        //Given

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(idamService.retrieveUser(any())).thenReturn(user);
        when(user.getAuthToken()).thenReturn("token");
        //When
        String response = authorisationService.getAuthorisation();

        //Then
        assertThat(response).isNotNull();
    }

    @Test
    void shouldGetAuthorisationStartingBearer() {
        //Given

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(idamService.retrieveUser(any())).thenReturn(user);
        when(user.getAuthToken()).thenReturn("Bearer token");
        //When
        String response = authorisationService.getAuthorisation();

        //Then
        assertThat(response).isNotNull();
    }

    @Test
    void shouldGetServiceAuth() {
        //Given

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        //When
        String response = authorisationService.getServiceAuthorization();

        //Then
        assertThat(response).isNotNull();
    }
}
