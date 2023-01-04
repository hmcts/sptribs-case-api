package uk.gov.hmcts.sptribs.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.Applicant;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.notification.ApplicantNotification;
import uk.gov.hmcts.sptribs.notification.CommonContent;
import uk.gov.hmcts.sptribs.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.sptribs.notification.CommonContent.DATE_FINAL_ORDER_ELIGIBLE_FROM_PLUS_3_MONTHS;
import static uk.gov.hmcts.sptribs.notification.CommonContent.IS_REMINDER;
import static uk.gov.hmcts.sptribs.notification.CommonContent.NO;
import static uk.gov.hmcts.sptribs.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.sptribs.notification.TemplateName.APPLICANT_APPLY_FOR_FINAL_ORDER;

@Component
@Slf4j
public class AwaitingFinalOrderNotification implements ApplicantNotification {

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        log.info("Notifying applicant 1 that they can apply for a final order: {}", id);
        final Applicant applicant1 = caseData.getApplicant1();
        final Applicant applicant2 = caseData.getApplicant2();

        notificationService.sendEmail(
            applicant1.getEmail(),
            APPLICANT_APPLY_FOR_FINAL_ORDER,
            templateVars(caseData, id, applicant1, applicant2),
            applicant1.getLanguagePreference()
        );
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {
        final Applicant applicant1 = caseData.getApplicant1();
        final Applicant applicant2 = caseData.getApplicant2();

        if (!caseData.getApplicationType().isSole()) {
            log.info("Notifying applicant 2 (joint application) that they can apply for a final order: {}", id);

            notificationService.sendEmail(
                caseData.getApplicant2EmailAddress(),
                APPLICANT_APPLY_FOR_FINAL_ORDER,
                templateVars(caseData, id, applicant2, applicant1),
                applicant2.getLanguagePreference()
            );
        }
    }

    private Map<String, String> templateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner) {
        Map<String, String> templateVars = commonContent.conditionalOrderTemplateVars(caseData, id, applicant, partner);
        templateVars.put(IS_REMINDER, NO);
        templateVars.put(DATE_FINAL_ORDER_ELIGIBLE_FROM_PLUS_3_MONTHS,
            caseData.getFinalOrder().getDateFinalOrderEligibleToRespondent().format(DATE_TIME_FORMATTER));
        return templateVars;
    }
}
