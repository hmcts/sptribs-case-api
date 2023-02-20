package uk.gov.hmcts.sptribs.judicialrefdata;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.judicialrefdata.model.UserProfileRefreshResponse;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;


@ExtendWith(MockitoExtension.class)
class JudicialServiceTest {

    @InjectMocks
    private JudicialService judicialService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private ResponseEntity<UserProfileRefreshResponse[]> responseEntity;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private JudicialClient judicialClient;

    @Test
    void shouldPopulateUserDynamicList() {
        //Given
        var userResponse = UserProfileRefreshResponse
            .builder()
            .build();

        //When
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(judicialClient.getUserProfiles(TEST_SERVICE_AUTH_TOKEN, TEST_AUTHORIZATION_TOKEN,
            new JudicialUsersRequest("ST_CIC")))
            .thenReturn(responseEntity);
        when(responseEntity.getBody()).thenReturn(new UserProfileRefreshResponse[]{userResponse});
        DynamicList userList = judicialService.getAllUsers();

        //Then
        assertThat(userList).isNotNull();
    }

    @Test
    void shouldReturnEmptyDynamicListWhenExceptionFromJudicialRefDataCall() {
        //When
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(judicialClient.getUserProfiles(TEST_SERVICE_AUTH_TOKEN, TEST_AUTHORIZATION_TOKEN,
            new JudicialUsersRequest("ST_CIC")))
            .thenReturn(null);
        DynamicList regionList = judicialService.getAllUsers();

        //Then
        assertThat(regionList.getListItems()).isEmpty();
    }
}
