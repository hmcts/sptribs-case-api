package uk.gov.hmcts.sptribs.notification;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_NUMBER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_SUBJECT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME;
import static uk.gov.hmcts.sptribs.common.ccd.CcdCaseType.CIC;

@Component
public class DssNotificationHelper {

    public Map<String, Object> getSubjectCommonVars(String caseNumber, DssCaseData dssCaseData) {
        Map<String, Object> templateVars = commonTemplateVars(caseNumber, dssCaseData);
        templateVars.put(CONTACT_NAME, dssCaseData.getSubjectFullName());
        return templateVars;
    }

    public Map<String, Object> getRepresentativeCommonVars(String caseNumber, DssCaseData dssCaseData) {
        Map<String, Object> templateVars = commonTemplateVars(caseNumber, dssCaseData);
        templateVars.put(CONTACT_NAME, dssCaseData.getRepresentativeFullName());
        return templateVars;
    }

    public NotificationRequest buildEmailNotificationRequest(String destinationAddress,
                                                             Map<String, Object> templateVars,
                                                             TemplateName emailTemplateName) {
        return NotificationRequest.builder()
            .destinationAddress(destinationAddress)
            .template(emailTemplateName)
            .templateVars(templateVars)
            .build();
    }

    private Map<String, Object> commonTemplateVars(final String caseNumber, final DssCaseData dssCaseData) {
        final Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(TRIBUNAL_NAME, CIC);
        templateVars.put(CIC_CASE_NUMBER, caseNumber);
        templateVars.put(CIC_CASE_SUBJECT_NAME, dssCaseData.getSubjectFullName());
        return templateVars;
    }

}
