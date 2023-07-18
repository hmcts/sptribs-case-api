package uk.gov.hmcts.sptribs.notifyproxy;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.sptribs.notifyproxy.dtos.NotificationEmailRequest;
import uk.gov.hmcts.sptribs.notifyproxy.service.NotificationService;

import javax.validation.Valid;


@RestController
@Api(tags = {"Notification Journey "})
@SuppressWarnings({"PMD.AvoidUncheckedExceptionsInSignatures", "PMD.AvoidDuplicateLiterals"})
public class NotificationController {

/*    @Autowired
    private NotificationService notificationService;*/
    private static Logger log = LoggerFactory.getLogger(NotificationController.class);


    @ApiOperation(value = "Create a email notification for a refund", notes = "Create email notification for a refund")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Notification sent successfully via email"),
        @ApiResponse(code = 400, message = "Bad request. Notification creation failed"),
        @ApiResponse(code = 403, message = "AuthError"),
        @ApiResponse(code = 422, message = "Invalid Template ID"),
        @ApiResponse(code = 429, message = "Too Many Requests Error"),
        @ApiResponse(code = 500, message = "Internal Server Error"),
        @ApiResponse(code = 504, message = "Unable to connect to notification provider, please try again late")
    })
    @PostMapping("/notifications/email")
    public ResponseEntity emailNotification(
         @RequestHeader("Authorization") String authorization,
         @RequestHeader(required = false) MultiValueMap<String, String> headers,
         @Valid @RequestBody NotificationEmailRequest request) {
        log.info("recipientEmailAddress in request  for /email endpoint {}",request.getRecipientEmailAddress());
        log.info("reference in request  for /email endpoint {}",request.getReference());
//        TODO
        /*notificationService.sendEmailNotification(request, headers);*/
            return new ResponseEntity<>(
                    "Notification sent successfully via email", HttpStatus.CREATED);
    }

}

