package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.sptribs.common.CommonConstants.DASHBOARD_KEY;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_ORDER;

@Component
@Slf4j
public class NewOrderIssuedNotification extends PartiesNotification {

    @Value("${sptribs-frontend.dashboard-url}")
    private String citizenDashboardUrl;

    @Value("${feature.citizen-dashboard.enabled}")
    private boolean citizenDashboardEnabled;

    public NewOrderIssuedNotification(NotificationServiceCIC notificationService, NotificationHelper notificationHelper) {
        super(notificationService, notificationHelper);
    }

    @Override
    protected PartyNotification buildSubjectNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVars = notificationHelper().getSubjectCommonVars(caseNumber, caseData);
        addDashboardLink(templateVars);

        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            Map<String, String> uploadedDocuments = getUploadedDocumentIds(caseData);
            return new EmailNotification(
                    cicCase.getEmail(),
                    templateVars,
                    getTemplateName(),
                    uploadedDocuments,
                    new ArrayList<>(),
                    saveToCicCase(CicCase::setSubjectNotifyList)
            );
        } else {
            notificationHelper().addAddressTemplateVars(cicCase.getAddress(), templateVars);
            return new LetterNotification(
                    cicCase.getAddress(),
                    templateVars,
                    TemplateName.NEW_ORDER_ISSUED_POST,
                    saveToCicCase(CicCase::setSubjectLetterNotifyList)
            );
        }
    }

    @Override
    protected PartyNotification buildApplicantNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVars = notificationHelper().getApplicantCommonVars(caseNumber, caseData);

        if (cicCase.getContactPreferenceType() == ContactPreferenceType.EMAIL) {
            Map<String, String> uploadedDocuments = getUploadedDocumentIds(caseData);
            return new EmailNotification(
                    cicCase.getApplicantEmailAddress(),
                    templateVars,
                    getTemplateName(),
                    uploadedDocuments,
                    new ArrayList<>(),
                    saveToCicCase(CicCase::setAppNotificationResponse)
            );
        } else {
            notificationHelper().addAddressTemplateVars(cicCase.getAddress(), templateVars);
            return new LetterNotification(
                    cicCase.getApplicantAddress(),
                    templateVars,
                    TemplateName.NEW_ORDER_ISSUED_POST,
                    saveToCicCase(CicCase::setAppLetterNotificationResponse)
            );
        }
    }

    @Override
    protected PartyNotification buildRepresentativeNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> templateVars = notificationHelper().getRepresentativeCommonVars(caseNumber, caseData);

        if (cicCase.getRepresentativeContactDetailsPreference() == ContactPreferenceType.EMAIL) {
            Map<String, String> uploadedDocuments = getUploadedDocumentIds(caseData);
            return new EmailNotification(
                    cicCase.getRepresentativeEmailAddress(),
                    templateVars,
                    getTemplateName(),
                    uploadedDocuments,
                    new ArrayList<>(),
                    saveToCicCase(CicCase::setRepNotificationResponse)
            );
        } else {
            notificationHelper().addAddressTemplateVars(cicCase.getRepresentativeAddress(), templateVars);
            return new LetterNotification(
                    cicCase.getRepresentativeAddress(),
                    templateVars,
                    TemplateName.NEW_ORDER_ISSUED_POST,
                    saveToCicCase(CicCase::setRepLetterNotificationResponse)
            );
        }
    }

    @Override
    protected PartyNotification buildRespondentNotification(CaseData caseData, String caseNumber) {
        CicCase cicCase = caseData.getCicCase();
        Map<String, Object> respondentTemplateVars = notificationHelper().getRespondentCommonVars(caseNumber, caseData);
        Map<String, String> uploadedDocuments = getUploadedDocumentIds(caseData);

        return new EmailNotification(
                cicCase.getRespondentEmail(),
                respondentTemplateVars,
                TemplateName.NEW_ORDER_ISSUED_EMAIL,
                uploadedDocuments,
                new ArrayList<>(),
                saveToCicCase(CicCase::setResNotificationResponse)
        );
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
            final Order order = cicCase.getOrderList().getFirst().getValue();

            if (order.getDraftOrder() != null) {
                return order.getDraftOrder().getTemplateGeneratedDocument();
            } else if (order.getUploadedFile() != null
                && CollectionUtils.isNotEmpty(order.getUploadedFile())) {
                return order.getUploadedFile().getFirst().getValue().getDocumentLink();
            }
        }
        return null;
    }

    private TemplateName getTemplateName() {
        return citizenDashboardEnabled ? TemplateName.NEW_ORDER_ISSUED_EMAIL_NEW_CD : TemplateName.NEW_ORDER_ISSUED_EMAIL;
    }

    private void addDashboardLink(Map<String, Object> templateVars) {
        if (citizenDashboardEnabled) {
            templateVars.put(DASHBOARD_KEY, citizenDashboardUrl);
        }
    }
}
