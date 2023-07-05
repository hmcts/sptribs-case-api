package uk.gov.hmcts.sptribs.notification;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationType;
import uk.gov.hmcts.sptribs.common.config.EmailTemplatesConfigCIC;
import uk.gov.hmcts.sptribs.document.DocumentClient;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.notification.exception.NotificationException;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;
import uk.gov.service.notify.SendLetterResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.common.CommonConstants.DOC_AVAILABLE;

@Service
@Slf4j
public class NotificationServiceCIC {

    @Autowired
    private NotificationClient notificationClient;

    @Autowired
    private EmailTemplatesConfigCIC emailTemplatesConfig;

    @Autowired
    private IdamService idamService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private DocumentClient caseDocumentClient;

    public NotificationResponse sendEmail(NotificationRequest notificationRequest) {
        SendEmailResponse sendEmailResponse;
        String destinationAddress = notificationRequest.getDestinationAddress();
        TemplateName template = notificationRequest.getTemplate();
        Map<String, Object> templateVars = notificationRequest.getTemplateVars();

        String referenceId = UUID.randomUUID().toString();

        try {
            if (notificationRequest.isHasFileAttachments()) {
                addAttachmentsToTemplateVars(templateVars, notificationRequest.getUploadedDocuments());
            }

            String templateId = emailTemplatesConfig.getTemplatesCIC().get(template.name());

            log.info("Sending email for reference id : {} using template : {}", referenceId, templateId);

            sendEmailResponse =
                notificationClient.sendEmail(
                    templateId,
                    destinationAddress,
                    templateVars,
                    referenceId
                );

            log.info("Successfully sent email with notification id {} and reference {}",
                sendEmailResponse.getNotificationId(),
                sendEmailResponse.getReference().orElse(referenceId)
            );

            return getNotificationResponse(sendEmailResponse);
        } catch (NotificationClientException notificationClientException) {
            log.error("Failed to send email. Reference ID: {}. Reason: {}",
                referenceId,
                notificationClientException.getMessage(),
                notificationClientException
            );
            throw new NotificationException(notificationClientException);
        } catch (IOException ioException) {
            log.error("Issue with attach documents to Notification. Failed to send email. Reference ID: {}. Reason: {}",
                referenceId,
                ioException.getMessage(),
                ioException
            );
            throw new NotificationException(ioException);
        }
    }

    public NotificationResponse sendLetter(NotificationRequest notificationRequest) {
        TemplateName template = notificationRequest.getTemplate();
        Map<String, Object> templateVars = notificationRequest.getTemplateVars();

        String referenceId = UUID.randomUUID().toString();

        try {
            String templateId = emailTemplatesConfig.getTemplatesCIC().get(template.name());

            log.info("Sending letter for reference id : {} using template : {}", referenceId, templateId);

            SendLetterResponse sendLetterResponse =
                notificationClient.sendLetter(
                    templateId,
                    templateVars,
                    referenceId
                );

            log.info("Successfully sent letter with notification id {} and reference {}",
                sendLetterResponse.getNotificationId(),
                sendLetterResponse.getReference().orElse(referenceId)
            );
            return getLetterNotificationResponse(sendLetterResponse);
        } catch (NotificationClientException notificationClientException) {
            log.error("Failed to send letter. Reference ID: {}. Reason: {}",
                referenceId,
                notificationClientException.getMessage(),
                notificationClientException
            );
            throw new NotificationException(notificationClientException);
        }
    }

    private void addAttachmentsToTemplateVars(Map<String, Object> templateVars, Map<String, String> uploadedDocuments) throws IOException {

        final User user = idamService.retrieveUser(request.getHeader(AUTHORIZATION));
        log.info("User: {}, User Details: {}", user, user.getUserDetails());
        final String authorisation = "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiJaNEJjalZnZnZ1NVpleEt6QkVFbE1TbTQzTHM9IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJzdC1jaWNzZW5pb3ItbGVnYWwtb2ZmaWNlckBtYWlsaW5hdG9yLmNvbSIsImN0cyI6Ik9BVVRIMl9TVEFURUxFU1NfR1JBTlQiLCJhdXRoX2xldmVsIjowLCJhdWRpdFRyYWNraW5nSWQiOiI2Zjk1ZGE1MC00MDY0LTRkNmEtYTAxNC0wYjk4MGQwYjUxNTktNDY5MjYzNTAiLCJzdWJuYW1lIjoic3QtY2ljc2VuaW9yLWxlZ2FsLW9mZmljZXJAbWFpbGluYXRvci5jb20iLCJpc3MiOiJodHRwczovL2Zvcmdlcm9jay1hbS5zZXJ2aWNlLmNvcmUtY29tcHV0ZS1pZGFtLWRlbW8uaW50ZXJuYWw6ODQ0My9vcGVuYW0vb2F1dGgyL3JlYWxtcy9yb290L3JlYWxtcy9obWN0cyIsInRva2VuTmFtZSI6ImFjY2Vzc190b2tlbiIsInRva2VuX3R5cGUiOiJCZWFyZXIiLCJhdXRoR3JhbnRJZCI6Imo3VGRpMTZkN0FqamI3V3huOGZjNWgyNzMwRSIsIm5vbmNlIjoid3lmS2Q3M1ZDWW1Wcmc0STBSUjcySjVRc085U3F6NWFkb0NYVmg0QWdlQSIsImF1ZCI6Inh1aXdlYmFwcCIsIm5iZiI6MTY4ODU4MzM0OSwiZ3JhbnRfdHlwZSI6ImF1dGhvcml6YXRpb25fY29kZSIsInNjb3BlIjpbIm9wZW5pZCIsInByb2ZpbGUiLCJyb2xlcyIsImNyZWF0ZS11c2VyIiwibWFuYWdlLXVzZXIiLCJzZWFyY2gtdXNlciJdLCJhdXRoX3RpbWUiOjE2ODg1ODMzNDksInJlYWxtIjoiL2htY3RzIiwiZXhwIjoxNjg4NjEyMTQ5LCJpYXQiOjE2ODg1ODMzNDksImV4cGlyZXNfaW4iOjI4ODAwLCJqdGkiOiJfbGkwM2lkd294SXk0WnFWTHFBdDRvUXVZbGcifQ.qfRhq2DnsYaEocZNSX3QJGj9ZitV4CGCyx8Pp-11dTvZ3pEOBoEOZeiFe4DEV2insrKsEuhY-cy9lxsAIHhP3Hv4lWzclPd6rCbSQ4bT-TkPRHbuKFqANQFmrFL9kqEzMlBGhKCrJnnZhoyUxYEkTErlv0ccU8zjYLDicLuxRzisN6iHs3ZkRc8s3geLilMxHOKYEoW8MBbjyp5R6eFXeEizkLH1pdGsUoHBKq9-GZwWH6lh-uQtNZweFgrayczrmYncA4qRTO_I4n4AvxB7zr6Alg6Tsm5lXP_xNoZbBVWiASyw-kuVWMiEjBW34yjlaFunjGZKg6T7EKdw-7C29w";
        log.info("User authorization token: {}", authorisation);
        String serviceAuthorization = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJzcHRyaWJzX2Nhc2VfYXBpIiwiZXhwIjoxNjg4NTk3NzgxfQ.MtD01y02WURAY0pM9FIEu7OtiK8Ux1RNSmZ4_POvt1jeo8vI4cIFe-IqRp2sqe_-vqdut1ZH5KPjozboapFbig";
        //serviceAuthorization = serviceAuthorization.substring(7);

        log.info("Service authorization token: {}", serviceAuthorization);

        //for (Map.Entry<String, String> document : uploadedDocuments.entrySet()) {
            String docName = "doc1";
            String item = "e708ff5-4164-4303-91f6-a1984a7a3294";

            if (docName.contains(DOC_AVAILABLE)) {
                templateVars.put(docName, item);
            } else {
                if (StringUtils.isNotEmpty(item)) {
                    byte[] uploadedDocument = caseDocumentClient
                        .getDocumentBinary(authorisation, serviceAuthorization, UUID.fromString(item)).getBody();

                    if (uploadedDocument != null) {
                        templateVars.put(docName, getJsonFileAttachment(uploadedDocument));
                    } else {
                        log.info("Document not found with uuid : {}", item);
                        templateVars.put(docName, "");
                    }
                }
            }
        //}
    }

    private JSONObject getJsonFileAttachment(byte[] fileContents) {
        JSONObject jsonObject = null;
        try {
            if (Objects.nonNull(fileContents)) {
                jsonObject = NotificationClient.prepareUpload(fileContents);
            }
        } catch (NotificationClientException e) {
            log.error("unable to upload file to Notification -", e.getMessage());
            throw new NotificationException(e);
        }

        return jsonObject;
    }

    private NotificationResponse getNotificationResponse(final SendEmailResponse sendEmailResponse) {
        Optional<String> reference = sendEmailResponse.getReference();
        String clientReference = null;
        if (reference.isPresent()) {
            clientReference = reference.get();
        }

        return NotificationResponse.builder()
            .id(sendEmailResponse.getNotificationId().toString())
            .clientReference(clientReference)
            .notificationType(NotificationType.EMAIL)
            .updatedAtTime(LocalDateTime.now())
            .createdAtTime(LocalDateTime.now())
            .status("Received")
            .build();
    }

    private NotificationResponse getLetterNotificationResponse(final SendLetterResponse sendLetterResponse) {
        Optional<String> reference = sendLetterResponse.getReference();
        String clientReference = null;
        if (reference.isPresent()) {
            clientReference = reference.get();
        }

        return NotificationResponse.builder()
            .id(sendLetterResponse.getNotificationId().toString())
            .clientReference(clientReference)
            .notificationType(NotificationType.POST)
            .updatedAtTime(LocalDateTime.now())
            .createdAtTime(LocalDateTime.now())
            .status("Received")
            .build();
    }
}
