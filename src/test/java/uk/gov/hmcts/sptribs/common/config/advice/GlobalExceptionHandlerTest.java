package uk.gov.hmcts.sptribs.common.config.advice;

import feign.FeignException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.sptribs.common.config.interceptors.UnAuthorisedServiceException;
import uk.gov.hmcts.sptribs.common.exception.InvalidDataException;
import uk.gov.hmcts.sptribs.common.exception.UnsupportedFormTypeException;
import uk.gov.hmcts.sptribs.notification.exception.NotificationException;
import uk.gov.service.notify.NotificationClientException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GlobalExceptionHandlerTest {

    @Test
    public void shouldHandleNotificationException() {
        //Given
        final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
        final NotificationException notificationException = mock(NotificationException.class);

        when(notificationException.getCause()).thenReturn(new NotificationClientException("some exception"));

        //When
        final ResponseEntity<Object> actualResponse =
            exceptionHandler.handleNotificationException(notificationException);

        //Then
        assertThat(actualResponse.getStatusCode(), is(HttpStatus.BAD_REQUEST));

        assertThat(actualResponse.getBody(), is("some exception"));
    }

    @Test
    public void shouldHandleUnAuthorisedServiceException() {
        //Given
        final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
        final UnAuthorisedServiceException unAuthorisedServiceException = new UnAuthorisedServiceException("error");

        //When
        final ResponseEntity<Object> actualResponse =
            exceptionHandler.handleUnAuthorisedServiceException(unAuthorisedServiceException);

        //Then
        assertThat(actualResponse.getStatusCode(), is(HttpStatus.FORBIDDEN));

    }

    @Test
    public void shouldHandleFeignException() {
        //Given
        final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
        final FeignException feignException = mock(FeignException.class);

        //When
        final ResponseEntity<Object> actualResponse =
            exceptionHandler.handleFeignException(feignException);

        //Then
        Assertions.assertThat(actualResponse.getBody()).isNotNull();

    }

    @Test
    public void shouldHandleInvalidTokenException() {
        //Given
        final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

        //When
        final ResponseEntity<Object> actualResponse =
            exceptionHandler.handleInvalidTokenException();

        //Then
        assertThat(actualResponse.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void shouldHandleUnsupportedFormTypeException() {
        //Given
        final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
        final UnsupportedFormTypeException unsupportedFormTypeException = mock(UnsupportedFormTypeException.class);

        //When
        final ResponseEntity<Object> actualResponse =
            exceptionHandler.handleUnsupportedFormTypeException(unsupportedFormTypeException);

        //Then
        Assertions.assertThat(actualResponse.getBody()).isNotNull();
        assertThat(actualResponse.getStatusCode(), is(HttpStatus.UNPROCESSABLE_ENTITY));

    }

    @Test
    public void shouldHandleInvalidResourceException() {
        //Given
        final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

        //When
        final ResponseEntity<Object> actualResponse =
            exceptionHandler.handleInvalidResourceException();

        //Then
        Assertions.assertThat(actualResponse).isNotNull();

    }

    @Test
    public void shouldHandleInvalidDataException() {
        //Given
        final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
        final InvalidDataException invalidDataException = mock(InvalidDataException.class);

        //When
        final ResponseEntity<Object> actualResponse =
            exceptionHandler.handleInvalidDataException(invalidDataException);

        //Then
        Assertions.assertThat(actualResponse.getBody()).isNotNull();
        assertThat(actualResponse.getStatusCode(), is(HttpStatus.UNPROCESSABLE_ENTITY));

    }
}
