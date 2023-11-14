package uk.gov.hmcts.sptribs.judicialrefdata;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.judicialrefdata.model.UserProfileRefreshResponse;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ACCEPT_VALUE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;


@ExtendWith(MockitoExtension.class)
class JudicialServiceTest {

    @InjectMocks
    private JudicialService judicialService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private JudicialClient judicialClient;

    @Test
    void shouldPopulateUserDynamicList() {
        //Given
        ReflectionTestUtils.setField(judicialService, "enableJrdApiV2", false);

        var userResponse1 = UserProfileRefreshResponse
            .builder()
            .fullName("John Smith")
            .personalCode("12345")
            .build();
        var userResponse2 = UserProfileRefreshResponse
            .builder()
            .fullName("John Doe")
            .personalCode("98765")
            .build();

        List<UserProfileRefreshResponse> responseEntity = List.of(userResponse1, userResponse2);

        //When
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(judicialClient.getUserProfiles(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_AUTHORIZATION_TOKEN,
            new JudicialUsersRequest("ST_CIC"))).thenReturn(responseEntity);

        DynamicList userList = judicialService.getAllUsers();

        //Then
        assertThat(userList).isNotNull();
        verify(judicialClient).getUserProfiles(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_AUTHORIZATION_TOKEN,
            new JudicialUsersRequest("ST_CIC")
        );
    }

    @Test
    void shouldReturnEmptyDynamicListWhenExceptionFromJudicialRefDataCall() {
        //When
        ReflectionTestUtils.setField(judicialService, "enableJrdApiV2", false);

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(judicialClient.getUserProfiles(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_AUTHORIZATION_TOKEN,
            new JudicialUsersRequest("ST_CIC"))).thenReturn(null);

        DynamicList regionList = judicialService.getAllUsers();

        //Then
        assertThat(regionList.getListItems()).isEmpty();
        verify(judicialClient).getUserProfiles(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_AUTHORIZATION_TOKEN,
            new JudicialUsersRequest("ST_CIC")
        );
    }

    @Test
    void shouldPopulateUserDynamicListUsingRefDataV2API() {
        //Given
        ReflectionTestUtils.setField(judicialService, "enableJrdApiV2", true);

        var userResponse1 = UserProfileRefreshResponse
            .builder()
            .fullName("John Smith")
            .personalCode("12345")
            .build();
        var userResponse2 = UserProfileRefreshResponse
            .builder()
            .fullName("John Doe")
            .personalCode("98765")
            .build();
        List<UserProfileRefreshResponse> responseEntity = List.of(userResponse1, userResponse2);

        //When
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(judicialClient.getUserProfilesV2(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_AUTHORIZATION_TOKEN,
            ACCEPT_VALUE,
            new JudicialUsersRequest("ST_CIC"))).thenReturn(responseEntity);

        DynamicList userList = judicialService.getAllUsers();

        //Then
        assertThat(userList).isNotNull();
        verify(judicialClient).getUserProfilesV2(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_AUTHORIZATION_TOKEN,
            ACCEPT_VALUE,
            new JudicialUsersRequest("ST_CIC")
        );
    }

    @Test
    void shouldReturnEmptyDynamicListWhenExceptionFromJudicialRefDataV2Call() {
        //When
        ReflectionTestUtils.setField(judicialService, "enableJrdApiV2", true);

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(judicialClient.getUserProfilesV2(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_AUTHORIZATION_TOKEN,
            ACCEPT_VALUE,
            new JudicialUsersRequest("ST_CIC"))).thenReturn(null);

        DynamicList regionList = judicialService.getAllUsers();

        //Then
        assertThat(regionList.getListItems()).isEmpty();
        verify(judicialClient).getUserProfilesV2(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_AUTHORIZATION_TOKEN,
            ACCEPT_VALUE,
            new JudicialUsersRequest("ST_CIC")
        );
    }
}
