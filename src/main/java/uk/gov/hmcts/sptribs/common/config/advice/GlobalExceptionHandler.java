package uk.gov.hmcts.sptribs.common.config.advice;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.sptribs.common.config.interceptors.UnAuthorisedServiceException;
import uk.gov.hmcts.sptribs.exception.DocumentDownloadException;
import uk.gov.hmcts.sptribs.notification.exception.NotificationException;
import uk.gov.service.notify.NotificationClientException;

import static org.springframework.http.ResponseEntity.status;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = NotificationException.class)
    public ResponseEntity<Object> handleNotificationException(NotificationException notificationException) {
        log.error(notificationException.getMessage(), notificationException);
        NotificationClientException notificationClientException = (NotificationClientException) notificationException.getCause();

        return new ResponseEntity<>(
            notificationClientException.getMessage(),
            new HttpHeaders(),
            notificationClientException.getHttpResult()
        );
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Object> handleInvalidTokenException() {
        return status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(UnAuthorisedServiceException.class)
    public ResponseEntity<Object> handleUnAuthorisedServiceException(UnAuthorisedServiceException unAuthorisedServiceException) {
        return new ResponseEntity<>(
            unAuthorisedServiceException.getMessage(),
            HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Object> handleFeignException(FeignException exception) {
        log.error(exception.getMessage(), exception);

        return status(exception.status()).body(
            String.format("%s - %s", exception.getMessage(), exception.contentUTF8())
        );
    }

    @ExceptionHandler(DocumentDownloadException.class)
    public ResponseEntity<Object> handleDocumentDownloadException(DocumentDownloadException exception) {
        log.error("Document download error: {}", exception.getMessage(), exception);
        return status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
    }
}
