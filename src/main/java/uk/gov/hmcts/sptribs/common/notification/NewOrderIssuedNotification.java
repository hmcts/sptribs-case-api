package uk.gov.hmcts.sptribs.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
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

    @Autowired
    private NotificationServiceCIC notificationService;

    @Autowired
    private NotificationHelper notificationHelper;

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVars = notificationHelper.getSubjectCommonVars(caseNumber, cicCase);

        NotificationResponse notificationResponse;
        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            Map<String, String> uploadedDocuments = getUploadedDocumentIds(caseData);
            notificationResponse = sendEmailNotificationWithAttachment(cicCase.getEmail(),
                uploadedDocuments, templateVars);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVars);
            notificationResponse = sendLetterNotification(templateVars);
        }

        cicCase.setSubjectNotifyList(notificationResponse);
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVars = notificationHelper.getRepresentativeCommonVars(caseNumber, cicCase);

        NotificationResponse notificationResponse;
        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            Map<String, String> uploadedDocuments = getUploadedDocumentIds(caseData);
            notificationResponse = sendEmailNotificationWithAttachment(cicCase.getRepresentativeEmailAddress(),
                uploadedDocuments, templateVars);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVars);
            notificationResponse = sendLetterNotification(templateVars);
        }

        cicCase.setRepNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToRespondent(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();

        Map<String, Object> respondentTemplateVars = notificationHelper.getRespondentCommonVars(caseNumber, cicCase);
        Map<String, String> uploadedDocuments = getUploadedDocumentIds(caseData);
        NotificationResponse notificationResponse = sendEmailNotificationWithAttachment(cicCase.getRespondentEmail(),
            uploadedDocuments, respondentTemplateVars);
        cicCase.setResNotificationResponse(notificationResponse);
    }

    @Override
    public void sendToApplicant(final CaseData caseData, final String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVars = notificationHelper.getApplicantCommonVars(caseNumber, cicCase);

        NotificationResponse notificationResponse;
        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            Map<String, String> uploadedDocuments = getUploadedDocumentIds(caseData);

            notificationResponse = sendEmailNotificationWithAttachment(cicCase.getApplicantEmailAddress(),
                uploadedDocuments, templateVars);
        } else {
            notificationHelper.addAddressTemplateVars(cicCase.getAddress(), templateVars);
            notificationResponse = sendLetterNotification(templateVars);
        }
        cicCase.setAppNotificationResponse(notificationResponse);
    }

    private NotificationResponse sendEmailNotificationWithAttachment(final String destinationAddress,
                                                                     Map<String, String> uploadedDocuments,
                                                                     final Map<String, Object> templateVars) {
        NotificationRequest emailRequest = notificationHelper.buildEmailNotificationRequest(
            destinationAddress,
            true,
            uploadedDocuments,
            templateVars,
            TemplateName.NEW_ORDER_ISSUED_EMAIL);
        return notificationService.sendEmail(emailRequest);
    }

    private NotificationResponse sendLetterNotification(Map<String, Object> templateVarsLetter) {
        NotificationRequest letterRequest = notificationHelper.buildLetterNotificationRequest(
            templateVarsLetter,
            TemplateName.NEW_ORDER_ISSUED_POST);
        return notificationService.sendLetter(letterRequest);
    }

    private Map<String, String> getUploadedDocumentIds(CaseData caseData) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, String> uploadedDocuments = new HashMap<>();
        if (null != cicCase.getSelectedOrder()) {
            uploadedDocuments.put(TRIBUNAL_ORDER, StringUtils.substringAfterLast(cicCase.getSelectedOrder().getUrl(), "/"));

        }

        return uploadedDocuments;
    }

    private Document getLastSelectedOrder(CicCase cicCase) {
        Document lastSelectedOrder = null;
        for (ListValue<Order> orderListValue : cicCase.getOrderList()) {
            Order order = orderListValue.getValue();
            if (YesOrNo.YES.equals(order.getIsLastSelectedOrder())) {
                if (null != order.getDraftOrder()) {
                    lastSelectedOrder = order.getDraftOrder().getTemplateGeneratedDocument();
                } else if (null != order.getUploadedFile()
                    && !CollectionUtils.isEmpty(order.getUploadedFile())) {
                    lastSelectedOrder = order.getUploadedFile().get(0).getValue().getDocumentLink();
                }
            }
        }
        return lastSelectedOrder;
    }

}
