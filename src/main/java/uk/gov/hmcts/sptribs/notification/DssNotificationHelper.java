package uk.gov.hmcts.sptribs.notification;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.sptribs.common.CommonConstants.CICA_REF_NUMBER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_NUMBER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_SUBJECT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.HAS_CICA_NUMBER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME;
import static uk.gov.hmcts.sptribs.common.ccd.CcdCaseType.CIC;

@Component
public class DssNotificationHelper {

    public Map<String, Object> getSubjectCommonVars(String caseNumber, CaseData caseData) {
        final DssCaseData dssCaseData = caseData.getDssCaseData();
        Map<String, Object> templateVars = commonTemplateVars(caseNumber, dssCaseData);
        templateVars.put(CONTACT_NAME, dssCaseData.getSubjectFullName());
        if (caseData.getEditCicaCaseDetails() != null && !StringUtils.isEmpty(caseData.getEditCicaCaseDetails().getCicaReferenceNumber())) {
            templateVars.put(HAS_CICA_NUMBER, true);
            templateVars.put(CICA_REF_NUMBER, caseData.getEditCicaCaseDetails().getCicaReferenceNumber());
        } else {
            templateVars.put(HAS_CICA_NUMBER, false);
            templateVars.put(CICA_REF_NUMBER, "");
        }
        return templateVars;
    }

    public Map<String, Object> getRepresentativeCommonVars(String caseNumber, CaseData caseData) {
        final DssCaseData dssCaseData = caseData.getDssCaseData();
        Map<String, Object> templateVars = commonTemplateVars(caseNumber, dssCaseData);
        templateVars.put(CONTACT_NAME, dssCaseData.getRepresentativeFullName());
        if (caseData.getEditCicaCaseDetails() != null && !StringUtils.isEmpty(caseData.getEditCicaCaseDetails().getCicaReferenceNumber())) {
            templateVars.put(HAS_CICA_NUMBER, true);
            templateVars.put(CICA_REF_NUMBER, caseData.getEditCicaCaseDetails().getCicaReferenceNumber());
        } else {
            templateVars.put(HAS_CICA_NUMBER, false);
            templateVars.put(CICA_REF_NUMBER, "");
        }
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
