package uk.gov.hmcts.sptribs.common.config.advice;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.sptribs.common.config.interceptors.UnAuthorisedServiceException;
import uk.gov.hmcts.sptribs.notification.exception.NotificationException;
import uk.gov.service.notify.NotificationClientException;

import java.util.Collection;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GlobalExceptionHandlerTest {

    static class CustomFeignException extends FeignException {
        protected CustomFeignException(int status,
                                          String message,
                                          byte[] responseBody,
                                          Map<String, Collection<String>> responseHeaders) {
            super(status, message, responseBody, responseHeaders);
        }
    }

    @Test
    void shouldHandleNotificationException() {
        //Given
        final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
        final NotificationException notificationException = mock(NotificationException.class);

        when(notificationException.getCause()).thenReturn(new NotificationClientException("some exception"));

        //When
        final ResponseEntity<Object> actualResponse = exceptionHandler.handleNotificationException(notificationException);

        //Then
        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(actualResponse.getBody()).isEqualTo("some exception");
    }

    @Test
    void shouldHandleUnAuthorisedServiceException() {
        //Given
        final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
        final UnAuthorisedServiceException unAuthorisedServiceException = new UnAuthorisedServiceException("error");

        //When
        final ResponseEntity<Object> actualResponse = exceptionHandler.handleUnAuthorisedServiceException(unAuthorisedServiceException);

        //Then
        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(actualResponse.getBody()).isEqualTo("error");
    }

    @Test
    void shouldHandleFeignException() {
        //Given
        final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
        final FeignException feignException = new CustomFeignException(500, "internal error", "content".getBytes(), null);

        //When
        final ResponseEntity<Object> actualResponse = exceptionHandler.handleFeignException(feignException);

        //Then
        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(actualResponse.getBody()).isEqualTo("internal error - content");
    }

    @Test
    void shouldHandleInvalidTokenException() {
        //Given
        final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

        //When
        final ResponseEntity<Object> actualResponse = exceptionHandler.handleInvalidTokenException();

        //Then
        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(actualResponse.getBody()).isNull();
    }
}
