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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class CcdSupplementaryDataServiceTest {

    @InjectMocks
    private CcdSupplementaryDataService ccdSupplementaryDataService;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private CaseFlagsConfiguration caseFlagsConfiguration;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private  AuthTokenGenerator authTokenGenerator;

    @Mock
    private IdamService idamService;

    @Mock
    private User user;

    @Test
    void shouldSubmitSupplementaryDataToCcd() {
        //Given
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        //When
        ccdSupplementaryDataService.submitSupplementaryDataToCcd(TEST_CASE_ID.toString());

        //Then
        verify(coreCaseDataApi).submitSupplementaryData(any(),any(),any(),any());
    }

    @Test
    void shouldSubmitSupplementaryUpdateDataToCcd() {
        //Given
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(user.getAuthToken()).thenReturn(TEST_AUTHORIZATION_TOKEN);

        //When
        ccdSupplementaryDataService.submitSupplementaryDataRequestToCcd(TEST_CASE_ID.toString());

        //Then
        verify(coreCaseDataApi).submitSupplementaryData(any(),any(),any(),any());
    }
}
