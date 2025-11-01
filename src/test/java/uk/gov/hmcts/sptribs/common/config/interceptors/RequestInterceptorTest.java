package uk.gov.hmcts.sptribs.common.config.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.BEARER;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CCD_DATA;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;

@ExtendWith(MockitoExtension.class)
class RequestInterceptorTest {

    private static final String AUTH_TOKEN_WITH_BEARER_PREFIX = BEARER + TEST_AUTHORIZATION_TOKEN;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthTokenValidator validator;

    @InjectMocks
    private RequestInterceptor requestInterceptor;

    @BeforeEach
    public void setUp() {
        setField(requestInterceptor, "authorisedServices", List.of("ccd_data", "test_service"));
        lenient().when(request.getRequestURI()).thenReturn("");
    }

    @Test
    void shouldThrowExceptionWhenAuthorisedServiceListIsNull() {
        //Given
        setField(requestInterceptor, "authorisedServices",null);

        //When
        assertThatThrownBy(() -> requestInterceptor.preHandle(request, response, new Object()))
            .isExactlyInstanceOf(UnAuthorisedServiceException.class)
            .hasMessageContaining("List of authorised services is not yet configured");
    }

    @Test
    void shouldThrowExceptionWhenAuthorisedServiceListIsEmpty() {
        //Given
        setField(requestInterceptor, "authorisedServices", new ArrayList<>());

        //When
        assertThatThrownBy(() -> requestInterceptor.preHandle(request, response, new Object()))
            .isExactlyInstanceOf(UnAuthorisedServiceException.class)
            .hasMessageContaining("List of authorised services is not yet configured");
    }

    @Test
    void shouldThrowExceptionWhenServiceAuthTokenIsMissing() {
        //Given
        when(request.getHeader(SERVICE_AUTHORIZATION)).thenReturn(null);

        //When
        assertThatThrownBy(() -> requestInterceptor.preHandle(request, response, new Object()))
            .isExactlyInstanceOf(UnAuthorisedServiceException.class)
            .hasMessageContaining("Service authorization token is missing");
    }

    @Test
    void shouldThrowExceptionWhenTheCallingServiceIsNotAuthorised() {
        //Given
        when(validator.getServiceName(AUTH_TOKEN_WITH_BEARER_PREFIX)).thenReturn("fake_service");
        when(request.getHeader(SERVICE_AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);

        //When
        assertThatThrownBy(() -> requestInterceptor.preHandle(request, response, new Object()))
            .isExactlyInstanceOf(UnAuthorisedServiceException.class)
            .hasMessageContaining("Service fake_service not in configured list for accessing callback");
    }

    @Test
    void shouldAppendBearerPrefixWhenServiceAuthDoesNotIncludeBearerPrefix() {
        //Given
        when(validator.getServiceName(AUTH_TOKEN_WITH_BEARER_PREFIX)).thenReturn("ccd_data");
        when(request.getHeader(SERVICE_AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);

        //When
        Boolean result = requestInterceptor.preHandle(request, response, new Object());

        //Then
        assertThat(result).isTrue();
        verify(validator).getServiceName(AUTH_TOKEN_WITH_BEARER_PREFIX);
    }

    @Test
    void shouldThrowExceptionWhenNonCcdDataServiceCallsCcdPersistenceEndpoint() {
        //Given
        when(validator.getServiceName(AUTH_TOKEN_WITH_BEARER_PREFIX)).thenReturn("test_service");
        when(request.getHeader(SERVICE_AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(request.getRequestURI()).thenReturn("/ccd-persistence/cases");

        //When / Then
        assertThatThrownBy(() -> requestInterceptor.preHandle(request, response, new Object()))
            .isExactlyInstanceOf(UnAuthorisedServiceException.class)
            .hasMessage("Service not authorised to access ccd-persistence endpoints");
    }

    @Test
    void shouldAllowCcdDataServiceToCallCcdPersistenceEndpoint() {
        //Given
        when(validator.getServiceName(AUTH_TOKEN_WITH_BEARER_PREFIX)).thenReturn(CCD_DATA);
        when(request.getHeader(SERVICE_AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(request.getRequestURI()).thenReturn("/ccd-persistence/cases");

        //When
        Boolean result = requestInterceptor.preHandle(request, response, new Object());

        //Then
        assertThat(result).isTrue();
        verify(validator).getServiceName(AUTH_TOKEN_WITH_BEARER_PREFIX);
    }

    @Test
    void shouldNotAppendBearerPrefixWhenServiceAuthIncludesBearerPrefix() {
        //Given
        when(validator.getServiceName(AUTH_TOKEN_WITH_BEARER_PREFIX)).thenReturn("ccd_data");
        when(request.getHeader(SERVICE_AUTHORIZATION)).thenReturn(AUTH_TOKEN_WITH_BEARER_PREFIX);

        //When
        Boolean result = requestInterceptor.preHandle(request, response, new Object());

        //Then
        assertThat(result).isTrue();
        verify(validator).getServiceName(AUTH_TOKEN_WITH_BEARER_PREFIX);
    }
}
