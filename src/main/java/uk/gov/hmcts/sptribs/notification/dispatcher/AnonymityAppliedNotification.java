package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.Map;

@Component
@Slf4j
public class AnonymityAppliedNotification implements PartiesNotification {

    private final NotificationServiceCIC notificationService;
    private final NotificationHelper notificationHelper;

    @Autowired
    public AnonymityAppliedNotification(NotificationServiceCIC notificationService,
                                        NotificationHelper notificationHelper) {
        this.notificationService = notificationService;
        this.notificationHelper = notificationHelper;
    }

    @Override
    public void sendToSubject(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        if (cicCase.getContactPreferenceType() != ContactPreferenceType.EMAIL) {
            log.info("Skipping anonymity notification for subject because contact preference is not email");
            return;
        }

        final Map<String, Object> templateVars = notificationHelper.getSubjectCommonVars(caseNumber, caseData);
        NotificationResponse response = sendEmailNotification(cicCase.getEmail(), templateVars, caseNumber);
        cicCase.setSubjectNotifyList(response);
    }

    @Override
    public void sendToRepresentative(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        if (cicCase.getRepresentativeContactDetailsPreference() != ContactPreferenceType.EMAIL) {
            log.info("Skipping anonymity notification for representative because contact preference is not email");
            return;
        }

        final Map<String, Object> templateVars = notificationHelper.getRepresentativeCommonVars(caseNumber, caseData);
        NotificationResponse response = sendEmailNotification(cicCase.getRepresentativeEmailAddress(), templateVars, caseNumber);
        cicCase.setRepNotificationResponse(response);
    }

    private NotificationResponse sendEmailNotification(final String destinationAddress,
                                                       final Map<String, Object> templateVars,
                                                       String caseReferenceNumber) {
        final NotificationRequest request = notificationHelper.buildEmailNotificationRequest(
            destinationAddress,
            templateVars,
            TemplateName.ANONYMITY_APPLIED_EMAIL);
        return notificationService.sendEmail(request, caseReferenceNumber);
    }
}
