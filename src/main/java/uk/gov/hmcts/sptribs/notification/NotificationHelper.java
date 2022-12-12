package uk.gov.hmcts.sptribs.notification;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_NUMBER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_SUBJECT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.EMPTY_STRING;
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

    public void addAddressTemplateVars(CicCase cicCase, Map<String, Object> templateVars) {
        String addressLine2 = StringUtils.isNotEmpty(cicCase.getAddress().getAddressLine2()) ? cicCase.getAddress().getAddressLine2() : EMPTY_STRING;
        String addressLine3 = StringUtils.isNotEmpty(cicCase.getAddress().getAddressLine3()) ? cicCase.getAddress().getAddressLine3() : EMPTY_STRING;
        String addressLine4 = StringUtils.isNotEmpty(cicCase.getAddress().getPostTown()) ? cicCase.getAddress().getPostTown() : EMPTY_STRING;
        String addressLine5 = StringUtils.isNotEmpty(cicCase.getAddress().getCounty()) ? cicCase.getAddress().getCounty() : EMPTY_STRING;
        String addressLine6 = StringUtils.isNotEmpty(cicCase.getAddress().getCountry()) ? cicCase.getAddress().getCountry() : EMPTY_STRING;
        String addressLine7 = StringUtils.isNotEmpty(cicCase.getAddress().getPostCode()) ? cicCase.getAddress().getPostCode() : EMPTY_STRING;
        templateVars.put("address_line_1", cicCase.getAddress().getAddressLine1());
        templateVars.put("address_line_2", addressLine2);
        templateVars.put("address_line_3", addressLine3);
        templateVars.put("address_line_4", addressLine4);
        templateVars.put("address_line_5", addressLine5);
        templateVars.put("address_line_6", addressLine6);
        templateVars.put("address_line_7", addressLine7);
    }
}
