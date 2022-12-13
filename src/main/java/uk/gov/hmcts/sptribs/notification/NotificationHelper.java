package uk.gov.hmcts.sptribs.notification;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;

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
import static uk.gov.hmcts.sptribs.common.CommonConstants.NONE_PROVIDED;
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
        String addressLine2 = StringUtils.isNotEmpty(address.getAddressLine2())
            ? address.getAddressLine2() : NONE_PROVIDED;
        String addressLine3 = StringUtils.isNotEmpty(address.getAddressLine3())
            ? address.getAddressLine3() : NONE_PROVIDED;
        String addressLine4 = StringUtils.isNotEmpty(address.getPostTown())
            ? address.getPostTown() : NONE_PROVIDED;
        String addressLine5 = StringUtils.isNotEmpty(address.getCounty())
            ? address.getCounty() : NONE_PROVIDED;
        String addressLine6 = StringUtils.isNotEmpty(address.getCountry())
            ? address.getCountry() : NONE_PROVIDED;
        String addressLine7 = StringUtils.isNotEmpty(address.getPostCode())
            ? address.getPostCode() : NONE_PROVIDED;

        templateVars.put(ADDRESS_LINE_1, address.getAddressLine1());
        templateVars.put(ADDRESS_LINE_2, addressLine2);
        templateVars.put(ADDRESS_LINE_3, addressLine3);
        templateVars.put(ADDRESS_LINE_4, addressLine4);
        templateVars.put(ADDRESS_LINE_5, addressLine5);
        templateVars.put(ADDRESS_LINE_6, addressLine6);
        templateVars.put(ADDRESS_LINE_7, addressLine7);
    }
}
