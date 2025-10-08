package uk.gov.hmcts.sptribs.systemupdate.schedule.notification;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.cdam.model.UploadResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.model.Correspondence;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;
import uk.gov.hmcts.sptribs.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdSearchService;
import uk.gov.service.notify.Notification;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.NotificationList;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Component
public class SystemGetCorrespondence implements Runnable {

    private final AppsConfig appsConfig;
    private final AuthTokenGenerator authTokenGenerator;
    private final CcdSearchService ccdSearchService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final CaseDocumentClientApi caseDocumentClientApi;
    private final IdamService idamService;
    private final NotificationServiceCIC notificationService;

    private static final String EMAIL = "email";
    private static final String LETTER = "letter";
    private static final String SMS = "sms";



    @Autowired
    public SystemGetCorrespondence(AppsConfig appsConfig,
                                   AuthTokenGenerator authTokenGenerator,
                                   CaseDetailsConverter caseDetailsConverter,
                                   CaseDocumentClientApi caseDocumentClientApi,
                                   CcdSearchService ccdSearchService,
                                   IdamService idamService,
                                   NotificationServiceCIC notificationService) {
        this.appsConfig = appsConfig;
        this.authTokenGenerator = authTokenGenerator;
        this.caseDetailsConverter = caseDetailsConverter;
        this.caseDocumentClientApi = caseDocumentClientApi;
        this.ccdSearchService = ccdSearchService;
        this.idamService = idamService;
        this.notificationService = notificationService;
    }

    @Override
    public void run() {

        try {
            NotificationList emailNotifications = this.notificationService.getNotifications(EMAIL);
            log.info("Fetched {} email notifications", emailNotifications.getNotifications().size());
            List<Correspondence> emailCorrespondences = this.getCorrespondence(emailNotifications, EMAIL);
            for (Correspondence correspondence : emailCorrespondences) {
                this.setCorrespondenceToCaseData(correspondence, EMAIL);
            }
            log.info("Set email correspondence to case data");

            NotificationList letterNotifications = this.notificationService.getNotifications(LETTER);
            log.info("Fetched {} letter notifications", letterNotifications.getNotifications().size());
            List<Correspondence> letterCorrespondences = this.getCorrespondence(letterNotifications, LETTER);
            for (Correspondence correspondence : letterCorrespondences) {
                this.setCorrespondenceToCaseData(correspondence, LETTER);
            }
            log.info("Set letter correspondence to case data");

            NotificationList smsNotifications = this.notificationService.getNotifications(SMS);
            log.info("Fetched {} SMS notifications", smsNotifications.getNotifications().size());
            List<Correspondence> smsCorrespondences = this.getCorrespondence(smsNotifications, SMS);
            for (Correspondence correspondence : smsCorrespondences) {
                this.setCorrespondenceToCaseData(correspondence, SMS);
            }
            log.info("Set SMS correspondence to case data");
        } catch (NotificationClientException e) {
            log.error("Failed to fetch notifications: {}", e.getMessage());
        }
    }

    public void assignCorrespondence(CaseData caseData, Correspondence correspondence) {
        ListValue<Correspondence> listValueCorrespondence = ListValue.<Correspondence>builder()
            .id(String.valueOf(1))
            .value(correspondence)
            .build();

        if (caseData.getCicCase().getCorrespondence() != null) {
            listValueCorrespondence.setId((String.valueOf(caseData.getCicCase().getCorrespondence().size() + 1)));
        }

        List<ListValue<Correspondence>> newCorrespondenceList = new ArrayList<>();
        newCorrespondenceList.add(listValueCorrespondence);

        final List<ListValue<Correspondence>> correspondenceList =
            isEmpty(caseData.getCicCase().getCorrespondence())
                ? newCorrespondenceList
                : Stream.concat(caseData.getCicCase().getCorrespondence().stream(), newCorrespondenceList.stream()).toList();
        // sort by oldest first

        caseData.getCicCase().setCorrespondence(correspondenceList);
    }

    public List<Correspondence> getCorrespondence(NotificationList notifications, String notificationType) {
        List<Correspondence> correspondences = new ArrayList<>();

        for (Notification notification : notifications.getNotifications()) {
            String to = notification.getEmailAddress().toString().replace("Optional[", "").replace("]", "");
            if (notificationType.equals("sms")) {
                to = notification.getPhoneNumber().toString().replace("Optional[", "").replace("]", "");
            } else if (notificationType.equals("letter")) {
                if (notification.getLine1().isPresent()) {
                    to = notification.getLine1().get();
                }
                if (notification.getLine2().isPresent() && !notification.getLine2().get().trim().isEmpty()) {
                    to = to + ", " + notification.getLine2().get();
                }
                if (notification.getLine3().isPresent() && !notification.getLine3().get().trim().isEmpty()) {
                    to = to + ", " + notification.getLine3().get();
                }
                if (notification.getLine4().isPresent() && !notification.getLine4().get().trim().isEmpty()) {
                    to = to + ", " + notification.getLine4().get();
                }
                if (notification.getLine5().isPresent() && !notification.getLine5().get().trim().isEmpty()) {
                    to = to + ", " + notification.getLine5().get();
                }
                if (notification.getLine6().isPresent() && !notification.getLine6().get().trim().isEmpty()) {
                    to = to + ", " + notification.getLine6().get();
                }
                if (notification.getPostcode().isPresent()) {
                    to = to + ", " + notification.getPostcode().get();
                }
            }

            String[] emailSubject = notification.getSubject().toString().split("-");
            String cicaReferenceNumber = emailSubject[emailSubject.length - 1].trim();
            byte[] pdfBytes = this.notificationService.getNotificationAsPdf(notification.getId().toString());

            LocalDateTime sentOn = null;

            if (notification.getSentAt().isEmpty() && notification.getCompletedAt().isPresent()) {
                sentOn = notification.getCompletedAt().get().toLocalDateTime();
            } else if (notification.getSentAt().isPresent()) {
                sentOn = notification.getSentAt().get().toLocalDateTime();
            } else {
                log.error("Notification with ID {} has no sent or completed date", notification.getId());
                throw new IllegalStateException("Notification has no sent or completed date");
            }

            String from = "Criminal Injuries Compensation Tribunal";
            if (notification.getCreatedByName().isPresent()) {
                from = notification.getCreatedByName().toString();
            }



            Correspondence correspondence = Correspondence.builder()
                .sentOn(sentOn) //LocalDateTime
                .from(from)
                .to(to)
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
        final String authorizationHeader = idamService.retrieveSystemUpdateUserDetails().getAuthToken();

        try {
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
        } catch (FeignException feignException) {
            log.error("Failed to upload correspondence document to CDAM");
            return null;
        }

    }

    public void setCorrespondenceToCaseData(Correspondence correspondence, String notificationType) {
        String[] emailQueryStrings = {
            "Email", "RespondentEmail", "ApplicantEmailAddress", "RepresentativeEmailAddress"
        };

        String[] phoneNumberQueryStrings = {
            "PhoneNumber", "ApplicantPhoneNumber", "RepresentativePhoneNumber"
        };

        String[] addressQueryStrings = {
            "Address", "ApplicantAddress", "RepresentativeAddress"
        };

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        String[] queryStrings;

        switch (notificationType) {
            case EMAIL -> queryStrings = emailQueryStrings;
            case SMS -> queryStrings = phoneNumberQueryStrings;
            case LETTER -> queryStrings = addressQueryStrings;
            default -> {
                log.error("Notification type not recognised: {}", notificationType);
                return;
            }
        }

        try {
            BoolQueryBuilder query = boolQuery();
            for (String queryString : queryStrings) {
                if (notificationType.equals(EMAIL)) {
                    query = query.must(matchQuery("data.cicCase" + queryString, correspondence.getTo()));
                } else if (notificationType.equals(LETTER)) {
                    query = query
                        .must(matchQuery("data.cicCase" + queryString + ".AddressLine1", correspondence.getTo().split(",")[0]))
                        .must(matchQuery("data.cicCase" + queryString + ".PostCode",
                            correspondence.getTo().split(",")[correspondence.getTo().split(",").length - 1].trim()));
                }
                final List<CaseDetails> caseToLogCorrespondence =
                    ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuth);
                log.info("Cases:{} using {} key with contact information: {}",
                    caseToLogCorrespondence.size(), queryString, correspondence.getTo());
                for (final CaseDetails caseDetails : caseToLogCorrespondence) {
                    CaseData caseData = caseDetailsConverter.convertToCaseDetailsFromReformModel(caseDetails).getData();
                    this.assignCorrespondence(caseData, correspondence);
                }
            }
            log.info("System get correspondence task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("System get correspondence task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("System get correspondence task stopping due to conflict with another running task"
            );
        }
    }

}
