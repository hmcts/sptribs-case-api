package uk.gov.hmcts.sptribs.notification;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationType;
import uk.gov.hmcts.sptribs.common.config.EmailTemplatesConfigCIC;
import uk.gov.hmcts.sptribs.controllers.model.CorrespondenceRepository;
import uk.gov.hmcts.sptribs.controllers.model.MainController;
import uk.gov.hmcts.sptribs.document.DocumentClient;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.notification.exception.NotificationException;
import uk.gov.hmcts.sptribs.notification.model.Correspondence;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;
import uk.gov.service.notify.SendLetterResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.common.CommonConstants.DOC_AVAILABLE;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.BEARER_PREFIX;

@Service
@Slf4j
public class NotificationServiceCIC {

    @Autowired
    private final NotificationClient notificationClient;

    @Autowired
    private final EmailTemplatesConfigCIC emailTemplatesConfig;

    @Autowired
    private final IdamService idamService;

    @Autowired
    private final HttpServletRequest request;

    @Autowired
    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    private final DocumentClient caseDocumentClient;

    @Autowired
    private final MainController mainController;

    public NotificationServiceCIC(NotificationClient notificationClient,
                                  EmailTemplatesConfigCIC emailTemplatesConfig,
                                  IdamService idamService, HttpServletRequest request,
                                  AuthTokenGenerator authTokenGenerator,
                                  DocumentClient caseDocumentClient,
                                  MainController mainController) {

        this.notificationClient = notificationClient;
        this.emailTemplatesConfig = emailTemplatesConfig;
        this.idamService = idamService;
        this.request = request;
        this.authTokenGenerator = authTokenGenerator;
        this.caseDocumentClient = caseDocumentClient;
        this.mainController = mainController;
    }

    public void saveEmailCorrespondence(SendEmailResponse sendEmailResponse, String sentTo, String caseReferenceNumber) {
        String sentFrom = "Criminal Injuries Compensation Tribunal";

        if (sendEmailResponse.getFromEmail().isPresent()) {
            sentFrom = sendEmailResponse.getFromEmail().get();
        }

        Correspondence correspondence = Correspondence.builder()
            .sentOn(LocalDateTime.now())
            .from(sentFrom)
            .to(sentTo)
            .correspondenceType("Email")
            .build();

        mainController.addNewCorrespondence(correspondence);
    }

    public void saveLetterCorrespondence(SendLetterResponse sendLetterResponse, String sentTo, String caseReferenceNumber) {
        String sentFrom = "Criminal Injuries Compensation Tribunal";

        Correspondence correspondence = Correspondence.builder()
            .sentOn(LocalDateTime.now())
            .from(sentFrom)
            .to(sentTo)
            .correspondenceType("Letter")
            .build();

        mainController.addNewCorrespondence(correspondence);
    }

    public NotificationResponse sendEmail(NotificationRequest notificationRequest, String caseReferenceNumber) {
        final SendEmailResponse sendEmailResponse;
        final String destinationAddress = notificationRequest.getDestinationAddress();
        final TemplateName template = notificationRequest.getTemplate();
        final Map<String, Object> templateVars = notificationRequest.getTemplateVars();

        final String referenceId = UUID.randomUUID().toString();

        try {
            if (notificationRequest.isHasFileAttachments()) {
                addAttachmentsToTemplateVars(templateVars, notificationRequest.getUploadedDocuments());
            }

            String templateId = emailTemplatesConfig.getTemplatesCIC().get(template.name());

            sendEmailResponse =
                notificationClient.sendEmail(
                    templateId,
                    destinationAddress,
                    templateVars,
                    referenceId
                );

            this.saveEmailCorrespondence(sendEmailResponse, destinationAddress, caseReferenceNumber);

            log.debug("Successfully sent email with notification id {} and reference {}",
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

    public NotificationResponse sendLetter(NotificationRequest notificationRequest, String caseReferenceNumber) {
        final TemplateName template = notificationRequest.getTemplate();
        final Map<String, Object> templateVars = notificationRequest.getTemplateVars();

        final String referenceId = UUID.randomUUID().toString();

        try {
            final String templateId = emailTemplatesConfig.getTemplatesCIC().get(template.name());

            SendLetterResponse sendLetterResponse =
                notificationClient.sendLetter(
                    templateId,
                    templateVars,
                    referenceId
                );

            this.saveLetterCorrespondence(sendLetterResponse, notificationRequest.getDestinationAddress(), caseReferenceNumber);

            log.debug("Successfully sent letter with notification id {} and reference {}",
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
        final String authorisation = user.getAuthToken().startsWith(BEARER_PREFIX)
            ? user.getAuthToken() : BEARER_PREFIX + user.getAuthToken();
        final String serviceAuthorization = authTokenGenerator.generate();
        final String serviceAuthorizationLatest = serviceAuthorization.startsWith(BEARER_PREFIX)
            ? serviceAuthorization.substring(7) : serviceAuthorization;

        for (Map.Entry<String, String> document : uploadedDocuments.entrySet()) {
            final String docName = document.getKey();
            final String item = document.getValue();

            if (docName.contains(DOC_AVAILABLE)) {
                templateVars.put(docName, item);
            } else {
                if (StringUtils.isNotEmpty(item)) {
                    byte[] uploadedDocument = caseDocumentClient
                        .getDocumentBinary(authorisation, serviceAuthorizationLatest, UUID.fromString(item)).getBody();

                    if (uploadedDocument != null) {
                        log.debug("Document available for: {}", docName);
                        templateVars.put(docName, getJsonFileAttachment(uploadedDocument));
                    } else {
                        templateVars.put(docName, "");
                    }
                } else {
                    log.info("Document not available for: {}", docName);
                    templateVars.put(docName, "");
                }
            }
        }
    }

    private JSONObject getJsonFileAttachment(byte[] fileContents) {
        JSONObject jsonObject = null;
        try {
            if (Objects.nonNull(fileContents)) {
                jsonObject = NotificationClient.prepareUpload(fileContents);
            }
        } catch (NotificationClientException e) {
            log.error("Unable to upload file to Notification - {}", e.getMessage());
            throw new NotificationException(e);
        }
        return jsonObject;
    }

    private NotificationResponse getNotificationResponse(final SendEmailResponse sendEmailResponse) {
        final Optional<String> reference = sendEmailResponse.getReference();
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
        final Optional<String> reference = sendLetterResponse.getReference();
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
