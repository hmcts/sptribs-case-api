package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_ORDER;

@Component
@Slf4j
public class NewOrderIssuedNotification implements PartiesNotification {

    private final NotificationServiceCIC notificationService;

    private final NotificationHelper notificationHelper;

    @Autowired
    public NewOrderIssuedNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        this.notificationService = notificationService;
        this.notificationHelper = notificationHelper;
    }

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVars = notificationHelper.getSubjectCommonVars(caseNumber, caseData);

        final NotificationResponse notificationResponse;
        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            final Map<String, String> uploadedDocuments = getUploadedDocumentIds(caseData);
            notificationResponse = sendEmailNotificationWithAttachment(cicCase.getEmail(),
                uploadedDocuments, templateVars, caseNumber);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVars);
            notificationResponse = sendLetterNotification(templateVars, caseNumber);
        }
        cicCase.setSubjectNotifyList(notificationResponse);
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVars = notificationHelper.getRepresentativeCommonVars(caseNumber, caseData);

        final NotificationResponse notificationResponse;
        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            final Map<String, String> uploadedDocuments = getUploadedDocumentIds(caseData);
            notificationResponse = sendEmailNotificationWithAttachment(cicCase.getRepresentativeEmailAddress(),
                uploadedDocuments, templateVars, caseNumber);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVars);
            notificationResponse = sendLetterNotification(templateVars, caseNumber);
        }

        cicCase.setRepNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToRespondent(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();

        final Map<String, Object> respondentTemplateVars = notificationHelper.getRespondentCommonVars(caseNumber, caseData);
        final Map<String, String> uploadedDocuments = getUploadedDocumentIds(caseData);
        final NotificationResponse notificationResponse = sendEmailNotificationWithAttachment(cicCase.getRespondentEmail(),
            uploadedDocuments, respondentTemplateVars, caseNumber);
        cicCase.setResNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToApplicant(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVars = notificationHelper.getApplicantCommonVars(caseNumber, caseData);

        final NotificationResponse notificationResponse;
        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            final Map<String, String> uploadedDocuments = getUploadedDocumentIds(caseData);

            notificationResponse = sendEmailNotificationWithAttachment(cicCase.getApplicantEmailAddress(),
                uploadedDocuments, templateVars, caseNumber);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVars);
            notificationResponse = sendLetterNotification(templateVars, caseNumber);
        }

        cicCase.setAppNotificationResponse(notificationResponse);
    }

    private NotificationResponse sendEmailNotificationWithAttachment(final String destinationAddress,
                                                                     Map<String, String> uploadedDocuments,
                                                                     final Map<String, Object> templateVars,
                                                                     String caseReferenceNumber) {
        final NotificationRequest emailRequest = notificationHelper.buildEmailNotificationRequest(
            destinationAddress,
            true,
            uploadedDocuments,
            templateVars,
            TemplateName.NEW_ORDER_ISSUED_EMAIL);
        return notificationService.sendEmail(emailRequest, caseReferenceNumber);
    }

    private NotificationResponse sendLetterNotification(Map<String, Object> templateVarsLetter, String caseReferenceNumber) {

        final NotificationRequest letterRequest = notificationHelper.buildLetterNotificationRequest(
            templateVarsLetter,
            TemplateName.NEW_ORDER_ISSUED_POST);
        return notificationService.sendLetter(letterRequest, caseReferenceNumber);
    }

    private Map<String, String> getUploadedDocumentIds(CaseData caseData) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, String> uploadedDocuments = new HashMap<>();
        final Document lastSelectedOrder = getLastSelectedOrder(cicCase);

        if (lastSelectedOrder != null) {
            uploadedDocuments.put(TRIBUNAL_ORDER, StringUtils.substringAfterLast(lastSelectedOrder.getUrl(), "/"));
        }

        return uploadedDocuments;
    }

    private Document getLastSelectedOrder(CicCase cicCase) {
        if (CollectionUtils.isNotEmpty(cicCase.getOrderList())) {
            final Order order = cicCase.getOrderList().get(0).getValue();

            if (order.getDraftOrder() != null) {
                return order.getDraftOrder().getTemplateGeneratedDocument();
            } else if (order.getUploadedFile() != null
                && CollectionUtils.isNotEmpty(order.getUploadedFile())) {
                return order.getUploadedFile().get(0).getValue().getDocumentLink();
            }
        }

        return null;
    }

}
