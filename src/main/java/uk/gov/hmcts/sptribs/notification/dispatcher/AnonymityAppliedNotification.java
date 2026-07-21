package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.notification.NotificationConstants;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
@Slf4j
public class AnonymityAppliedNotification implements PartiesNotification {

    private static final DateTimeFormatter UK_DATE_FORMATTER = DateTimeFormatter.ofPattern(CommonConstants.CIC_CASE_UK_DATE_FORMAT);

    private final NotificationServiceCIC notificationService;
    private final NotificationHelper notificationHelper;

    @Autowired
    public AnonymityAppliedNotification(NotificationServiceCIC notificationService,
                                        NotificationHelper notificationHelper) {
        this.notificationService = notificationService;
        this.notificationHelper = notificationHelper;
    }

    @Override
    public void sendToTribunal(final CaseData caseData, final String caseNumber) {
        final Map<String, Object> templateVars = buildTemplateVars(caseData, caseNumber);
        final NotificationRequest request = notificationHelper.buildEmailNotificationRequest(
            NotificationConstants.ANONYMITY_RECIPIENT_EMAIL,
            templateVars,
            TemplateName.ANONYMITY_APPLIED_EMAIL
        );

        NotificationResponse response = notificationService.sendEmail(request, caseNumber);
        caseData.getCicCase().setTribunalNotificationResponse(response);
    }

    private Map<String, Object> buildTemplateVars(CaseData caseData, String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVars = notificationHelper.getTribunalCommonVars(caseNumber, caseData);

        templateVars.put(CommonConstants.CIC_CASE_HEARING_DATE, cicCase.getAnonymisationDate() != null
            ? cicCase.getAnonymisationDate().format(UK_DATE_FORMATTER) : LocalDate.now().format(UK_DATE_FORMATTER));
        templateVars.put(CommonConstants.CIC_CASE_STATUS, formatCaseStatus(caseData.getCaseStatus()));
        templateVars.put(CommonConstants.CIC_CASE_SUBJECT_NAME, valueOrNoneProvided(cicCase.getFullName()));
        templateVars.put(CommonConstants.CIC_CASE_REPRESENTATIVE_NAME, valueOrNoneProvided(cicCase.getRepresentativeFullName()));
        templateVars.put(CommonConstants.CIC_CASE_APPLICANT_NAME, valueOrNoneProvided(cicCase.getApplicantFullName()));
        templateVars.put(CommonConstants.CIC_CASE_RESPONDENT_NAME, valueOrNoneProvided(cicCase.getRespondentName()));

        return templateVars;
    }

    private String valueOrNoneProvided(String value) {
        return value == null || value.isBlank() ? CommonConstants.NONE_PROVIDED : value;
    }

    private String formatCaseStatus(State caseStatus) {
        if (caseStatus == null) {
            return CommonConstants.NONE_PROVIDED;
        }

        String rawName = caseStatus.getName();
        if (rawName == null || rawName.isBlank()) {
            return CommonConstants.NONE_PROVIDED;
        }

        return rawName
            .replaceAll("([a-z])([A-Z])", "$1 $2")
            .replaceAll("[_-]", " ")
            .trim();
    }

    public void sendAnonymityNotificationIfNewlyApplied(final CaseData caseData, final CaseData beforeData) {
        if (isAnonymityNewlyApplied(caseData, beforeData)) {
            sendToTribunal(caseData, caseData.getHyphenatedCaseRef());
        }
    }

    private boolean isAnonymityNewlyApplied(final CaseData caseData, final CaseData beforeData) {
        if (caseData == null) {
            return false;
        }
        final CicCase cicCaseAfter = caseData.getCicCase();
        if (cicCaseAfter == null) {
            return false;
        }
        final CicCase cicCaseBefore = beforeData != null ? beforeData.getCicCase() : null;

        final boolean anonymityAppliedAfter = YesOrNo.YES.equals(cicCaseAfter.getAnonymiseYesOrNo())
            && cicCaseAfter.getAnonymisedAppellantName() != null;

        final boolean anonymityAppliedBefore = cicCaseBefore != null
            && YesOrNo.YES.equals(cicCaseBefore.getAnonymiseYesOrNo());

        return anonymityAppliedAfter && !anonymityAppliedBefore;
    }
}
