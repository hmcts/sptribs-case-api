package uk.gov.hmcts.sptribs.notification;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.sptribs.common.CommonConstants.ADDRESS_LINE_1;
import static uk.gov.hmcts.sptribs.common.CommonConstants.ADDRESS_LINE_2;
import static uk.gov.hmcts.sptribs.common.CommonConstants.ADDRESS_LINE_3;
import static uk.gov.hmcts.sptribs.common.CommonConstants.ADDRESS_LINE_4;
import static uk.gov.hmcts.sptribs.common.CommonConstants.ADDRESS_LINE_5;
import static uk.gov.hmcts.sptribs.common.CommonConstants.ADDRESS_LINE_6;
import static uk.gov.hmcts.sptribs.common.CommonConstants.ADDRESS_LINE_7;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_NUMBER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_SUBJECT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME;


@Component
public class NotificationHelper {

    public Map<String, Object> commonTemplateVars(final CicCase cicCase, final String caseNumber) {
        final Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(TRIBUNAL_NAME, CIC);
        templateVars.put(CIC_CASE_NUMBER, caseNumber);
        templateVars.put(CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        return templateVars;
    }

    public void addAddressTemplateVars(AddressGlobalUK address, Map<String, Object> templateVars) {
        templateVars.put(ADDRESS_LINE_1, address.getAddressLine1());
        templateVars.put(ADDRESS_LINE_2, address.getAddressLine2());
        templateVars.put(ADDRESS_LINE_3, address.getAddressLine3());
        templateVars.put(ADDRESS_LINE_4, address.getPostTown());
        templateVars.put(ADDRESS_LINE_5, address.getCounty());
        templateVars.put(ADDRESS_LINE_6, address.getCountry());
        templateVars.put(ADDRESS_LINE_7, address.getPostCode());
    }

    public Map<String, Object> getSubjectCommonVars(String caseNumber, CicCase cicCase) {
        Map<String, Object> templateVars = commonTemplateVars(cicCase, caseNumber);
        templateVars.put(CONTACT_NAME, cicCase.getFullName());
        return templateVars;
    }

    public Map<String, Object> getRepresentativeCommonVars(String caseNumber, CicCase cicCase) {
        Map<String, Object> templateVars = commonTemplateVars(cicCase, caseNumber);
        templateVars.put(CONTACT_NAME, cicCase.getRepresentativeFullName());
        return templateVars;
    }

    public Map<String, Object> getRespondentCommonVars(String caseNumber, CicCase cicCase) {
        Map<String, Object> templateVars = commonTemplateVars(cicCase, caseNumber);
        templateVars.put(CONTACT_NAME, cicCase.getRespondantName());
        return templateVars;
    }

    public Map<String, Object> getApplicantCommonVars(String caseNumber, CicCase cicCase) {
        Map<String, Object> templateVars = commonTemplateVars(cicCase, caseNumber);
        templateVars.put(CONTACT_NAME, cicCase.getApplicantFullName());
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

    public NotificationRequest buildLetterNotificationRequest(Map<String, Object> templateVarsLetter,
                                                              TemplateName letterTemplateName) {
        return NotificationRequest.builder()
            .template(letterTemplateName)
            .templateVars(templateVarsLetter)
            .build();
    }

}
