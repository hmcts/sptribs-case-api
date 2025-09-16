package uk.gov.hmcts.sptribs.systemupdate.schedule.notification;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;
import uk.gov.hmcts.sptribs.cdam.model.UploadResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.model.Correspondence;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;
import uk.gov.service.notify.Notification;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.NotificationList;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Component
public class SystemGetCorrespondence implements Runnable {

    private final NotificationServiceCIC notificationService;
    private final CaseDocumentClientApi caseDocumentClientApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final AppsConfig appsConfig;
    private final HttpServletRequest httpServletRequest;


    @Autowired
    public SystemGetCorrespondence(NotificationServiceCIC notificationService,
                                   CaseDocumentClientApi caseDocumentClientApi,
                                   AppsConfig appsConfig,
                                   AuthTokenGenerator authTokenGenerator,
                                   HttpServletRequest httpServletRequest) {
        this.notificationService = notificationService;
        this.caseDocumentClientApi = caseDocumentClientApi;
        this.appsConfig = appsConfig;
        this.authTokenGenerator = authTokenGenerator;
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public void run() {
        try {
            NotificationList emailNotifications = this.notificationService.getNotifications("email");
            log.info("Fetched {} email notifications", emailNotifications.getNotifications().size());

            NotificationList letterNotifications = this.notificationService.getNotifications("letter");
            log.info("Fetched {} letter notifications", letterNotifications.getNotifications().size());

            NotificationList smsNotifications = this.notificationService.getNotifications("sms");
            log.info("Fetched {} SMS notifications", smsNotifications.getNotifications().size());
        } catch (NotificationClientException e) {
            log.error("Failed to fetch notifications: {}", e.getMessage());
        }
    }

    public void assignCorrespondence(CaseData caseData, Correspondence correspondence) {
        ListValue<Correspondence> listValueCorrespondence = ListValue.<Correspondence>builder()
            .id(String.valueOf(caseData.getCicCase().getCorrespondence().size() + 1))
            .value(correspondence)
            .build();

        List<ListValue<Correspondence>> newCorrespondenceList = new ArrayList<>();
        newCorrespondenceList.add(listValueCorrespondence);

        final List<ListValue<Correspondence>> correspondenceList =
            isEmpty(caseData.getCicCase().getCorrespondence())
                ? newCorrespondenceList
                : Stream.concat(caseData.getCicCase().getCorrespondence().stream(), newCorrespondenceList.stream()).toList();

        caseData.getCicCase().setCorrespondence(correspondenceList);
    }

    public List<Correspondence> getCorrespondence(NotificationList notifications) {
        List<Correspondence> correspondences = new ArrayList<>();

        for (Notification notification : notifications.getNotifications()) {
            String[] subject = notification.getSubject().toString().split("-");
            String cicaReferenceNumber = subject[subject.length - 1].trim();
            byte[] pdfBytes = this.notificationService.getNotificationAsPdf(notification.getId().toString());

            LocalDateTime sentOn = notification.getSentAt().get().toLocalDateTime();

            Correspondence correspondence = Correspondence.builder()
                .sentOn(notification.getSentAt().get().toLocalDateTime()) //LocalDateTime
                .from(notification.getCreatedByName().toString())
                .to(notification.getEmailAddress().toString())
                .documentUrl(this.getCorrespondenceDocumentURL(pdfBytes, cicaReferenceNumber, sentOn))
                .correspondenceType(notification.getNotificationType())
                .build();

            correspondences.add(correspondence);
        }
        return correspondences;
    }

    // Uploads & then retrieves the correspondence PDF details
    public Document getCorrespondenceDocumentURL(byte[] pdfBytes, String cicaReferenceNumber, LocalDateTime sentOn) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-y-HH-mm");
        final String formattedSentOn = sentOn.format(formatter);
        final InMemoryMultipartFile inMemoryMultipartFile =
            new InMemoryMultipartFile(cicaReferenceNumber.replace(" ", "-") + "-" + formattedSentOn + ".pdf", pdfBytes);

        final List<AppsConfig.AppsDetails> appDetails = this.appsConfig.getApps();
        final String caseType = appDetails.getFirst().getCaseType();
        final String jurisdiction = appDetails.getFirst().getJurisdiction();

        final DocumentUploadRequest correspondenceUploadRequest =
            new DocumentUploadRequest(Classification.RESTRICTED.toString(),
                caseType,
                jurisdiction,
                List.of(inMemoryMultipartFile));

        final String serviceToken = authTokenGenerator.generate();
        final String authorizationHeader = httpServletRequest.getHeader(AUTHORIZATION);

        UploadResponse correspondenceUploadResponse = this.caseDocumentClientApi.uploadDocuments(
            authorizationHeader,
            serviceToken,
            correspondenceUploadRequest
        );

        final uk.gov.hmcts.sptribs.cdam.model.Document document = correspondenceUploadResponse.getDocuments().getFirst();

        return Document.builder()
            .filename(document.originalDocumentName)
            .url(document.links.self.href)
            .binaryUrl(document.links.binary.href)
            .build();

    }

}
