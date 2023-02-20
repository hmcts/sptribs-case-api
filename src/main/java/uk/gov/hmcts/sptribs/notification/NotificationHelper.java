package uk.gov.hmcts.sptribs.notification;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.sptribs.caseworker.model.RecordListing;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.HearingFormat;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.HYPHEN;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.SPACE;
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
import static uk.gov.hmcts.sptribs.common.CommonConstants.HEARING_DATE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.HEARING_TIME;
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
        templateVars.put(CONTACT_NAME, cicCase.getRespondentName());
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

    public NotificationRequest buildEmailNotificationRequest(String destinationAddress,
                                                             boolean hasFileAttachment,
                                                             List<String> uploadedDocumentIds,
                                                             Map<String, Object> templateVars,
                                                             TemplateName emailTemplateName) {
        return NotificationRequest.builder()
            .destinationAddress(destinationAddress)
            .hasFileAttachments(hasFileAttachment)
            .uploadedDocumentIds(uploadedDocumentIds)
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

    public void setRecordingTemplateVars(Map<String, Object> templateVars, RecordListing recordListing) {
        templateVars.put(CommonConstants.CIC_CASE_HEARING_TYPE, recordListing.getHearingType());

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(CommonConstants.CIC_CASE_UK_DATE_FORMAT);
        templateVars.put(CommonConstants.CIC_CASE_HEARING_DATE, recordListing.getHearingDate().format(dateTimeFormatter));
        templateVars.put(CommonConstants.CIC_CASE_HEARING_TIME, recordListing.getHearingTime());

        if (isVideoFormat(recordListing) || isTelephoneFormat(recordListing)) {
            templateVars.put(CommonConstants.CIC_CASE_HEARING_VENUE, CommonConstants.CIC_CASE_RECORD_REMOTE_HEARING);
        } else
            if (null != recordListing.getSelectedVenue()) {
                templateVars.put(CommonConstants.CIC_CASE_HEARING_VENUE, recordListing.getSelectedVenue());
            } else
                if (null != recordListing.getHearingVenueNameAndAddress()) {
                    templateVars.put(CommonConstants.CIC_CASE_HEARING_VENUE, recordListing.getHearingVenueNameAndAddress());
                } else {
                    templateVars.put(CommonConstants.CIC_CASE_HEARING_VENUE, " ");
                }

        if (null != recordListing.getAddlInstr()) {
            templateVars.put(CommonConstants.CIC_CASE_HEARING_INFO, recordListing.getAddlInstr());
        } else {
            templateVars.put(CommonConstants.CIC_CASE_HEARING_INFO, " ");
        }
        if (null != recordListing.getVideoCallLink()) {
            templateVars.put(CommonConstants.CIC_CASE_RECORD_VIDEO_CALL_LINK, recordListing.getVideoCallLink());
        } else {
            templateVars.put(CommonConstants.CIC_CASE_RECORD_VIDEO_CALL_LINK, " ");
        }
        if (null != recordListing.getConferenceCallNumber()) {
            templateVars.put(CommonConstants.CIC_CASE_RECORD_CONF_CALL_NUM, recordListing.getConferenceCallNumber());
        } else {
            templateVars.put(CommonConstants.CIC_CASE_RECORD_CONF_CALL_NUM, " ");
        }
        if (isVideoFormat(recordListing)) {
            templateVars.put(CommonConstants.CIC_CASE_RECORD_HEARING_FORMAT_VIDEO, true);
        } else {
            templateVars.put(CommonConstants.CIC_CASE_RECORD_HEARING_FORMAT_VIDEO, false);
        }
        if (isTelephoneFormat(recordListing)) {
            templateVars.put(CommonConstants.CIC_CASE_RECORD_FORMAT_TEL, true);
        } else {
            templateVars.put(CommonConstants.CIC_CASE_RECORD_FORMAT_TEL, false);
        }
        if (isFaceToFaceFormat(recordListing)) {
            templateVars.put(CommonConstants.CIC_CASE_RECORD_HEARING_1FACE_TO_FACE, true);
        } else {
            templateVars.put(CommonConstants.CIC_CASE_RECORD_HEARING_1FACE_TO_FACE, false);
        }
    }

    private boolean isFaceToFaceFormat(RecordListing recordListing) {
        return null != recordListing.getHearingFormat() && recordListing.getHearingFormat().equals(HearingFormat.FACE_TO_FACE);
    }

    private boolean isVideoFormat(RecordListing recordListing) {
        return null != recordListing.getHearingFormat() && recordListing.getHearingFormat().equals(HearingFormat.VIDEO);
    }

    private boolean isTelephoneFormat(RecordListing recordListing) {
        return null != recordListing.getHearingFormat() && recordListing.getHearingFormat().equals(HearingFormat.TELEPHONE);
    }

    public void addHearingPostponedTemplateVars(CicCase cicCase, Map<String, Object> templateVars) {
        String selectedHearingDateTime = cicCase.getSelectedHearingToCancel();
        String[] hearingDateTimeArr = (null != selectedHearingDateTime) ? selectedHearingDateTime.split(SPACE + HYPHEN + SPACE) : null;
        String hearingDate = null != hearingDateTimeArr && ArrayUtils.isNotEmpty(hearingDateTimeArr)
            ? hearingDateTimeArr[1].substring(0, hearingDateTimeArr[1].lastIndexOf(SPACE))
            : null;
        String hearingTime = null != hearingDateTimeArr && ArrayUtils.isNotEmpty(hearingDateTimeArr)
            ? hearingDateTimeArr[1].substring(hearingDateTimeArr[1].lastIndexOf(SPACE) + 1)
            : null;

        templateVars.put(HEARING_DATE, hearingDate);
        templateVars.put(HEARING_TIME, hearingTime);
    }
}
