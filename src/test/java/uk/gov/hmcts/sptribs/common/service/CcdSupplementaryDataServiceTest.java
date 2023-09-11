package uk.gov.hmcts.sptribs.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.config.CaseFlagsConfiguration;
import uk.gov.hmcts.sptribs.idam.IdamService;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
public class CcdSupplementaryDataServiceTest {
    @InjectMocks
    private CcdSupplementaryDataService coreCaseApiService;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private CaseFlagsConfiguration caseFlagsConfiguration;

    @Mock
    private  AuthTokenGenerator authTokenGenerator;

    @Mock
    private IdamService idamService;

    @Mock
    private User user;

    @Mock
    private HttpServletRequest request;

    @Test
    void shouldSubmitSupplementaryDataToCcd() {
        //Given
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        //When
        coreCaseApiService.submitSupplementaryDataToCcd(TEST_CASE_ID.toString());

        //Then
        verify(coreCaseDataApi).submitSupplementaryData(any(),any(),any(),any());
    }

    @Test
    void shouldSubmitSupplementaryUpdateDataToCcd() {
        //Given
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamService.retrieveUser(any())).thenReturn(user);
        when(user.getAuthToken()).thenReturn("token");
        //When
        coreCaseApiService.submitSupplementaryDataRequestToCcd(TEST_CASE_ID.toString());

        //Then
        verify(coreCaseDataApi).submitSupplementaryData(any(),any(),any(),any());
    }
}
