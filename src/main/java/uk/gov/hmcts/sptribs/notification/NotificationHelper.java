package uk.gov.hmcts.sptribs.notification;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.CicCaseFieldsUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
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
import static uk.gov.hmcts.sptribs.common.CommonConstants.CASE_DOCUMENT;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CICA_REF_NUMBER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_NUMBER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_SUBJECT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.DOC_AVAILABLE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.EMPTY_PLACEHOLDER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.HAS_CICA_NUMBER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.HEARING_DATE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.HEARING_TIME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.MARKUP_SEPARATOR;
import static uk.gov.hmcts.sptribs.common.CommonConstants.NO;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME_VALUE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.YES;
import static uk.gov.hmcts.sptribs.common.ccd.CcdCaseType.CIC;

@Component
@Slf4j
public class NotificationHelper {

    public void addAddressTemplateVars(AddressGlobalUK address, Map<String, Object> templateVars) {
        templateVars.put(ADDRESS_LINE_1, address.getAddressLine1());
        templateVars.put(ADDRESS_LINE_2, address.getAddressLine2());
        templateVars.put(ADDRESS_LINE_3, address.getAddressLine3());
        templateVars.put(ADDRESS_LINE_4, address.getPostTown());
        templateVars.put(ADDRESS_LINE_5, address.getCounty());
        templateVars.put(ADDRESS_LINE_6, address.getCountry());
        templateVars.put(ADDRESS_LINE_7, address.getPostCode());
    }

    public Map<String, Object> getSubjectCommonVars(String caseNumber, CaseData caseData) {
        final Map<String, Object> templateVars = commonTemplateVars(caseData, caseNumber);
        templateVars.put(CONTACT_NAME, caseData.getCicCase().getFullName());
        return templateVars;
    }

    public Map<String, Object> getRepresentativeCommonVars(String caseNumber, CaseData caseData) {
        final Map<String, Object> templateVars = commonTemplateVars(caseData, caseNumber);
        templateVars.put(CONTACT_NAME, caseData.getCicCase().getRepresentativeFullName());
        return templateVars;
    }

    public Map<String, Object> getRespondentCommonVars(String caseNumber, CaseData caseData) {
        final Map<String, Object> templateVars = commonTemplateVars(caseData, caseNumber);
        templateVars.put(CONTACT_NAME, caseData.getCicCase().getRespondentName());
        return templateVars;
    }

    public Map<String, Object> getTribunalCommonVars(String caseNumber, CaseData caseData) {
        final Map<String, Object> templateVars = commonTemplateVars(caseData, caseNumber);
        templateVars.put(CONTACT_NAME, TRIBUNAL_NAME_VALUE);
        return templateVars;
    }

    public Map<String, Object> getApplicantCommonVars(String caseNumber, CaseData caseData) {
        final Map<String, Object> templateVars = commonTemplateVars(caseData, caseNumber);
        templateVars.put(CONTACT_NAME, caseData.getCicCase().getApplicantFullName());
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
                                                             Map<String, String> uploadedDocuments,
                                                             Map<String, Object> templateVars,
                                                             TemplateName emailTemplateName) {
        return NotificationRequest.builder()
            .destinationAddress(destinationAddress)
            .hasFileAttachments(hasFileAttachment)
            .uploadedDocuments(uploadedDocuments)
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

    public void setRecordingTemplateVars(Map<String, Object> templateVars, Listing listing) {
        templateVars.put(CommonConstants.CIC_CASE_HEARING_TYPE, listing.getHearingType());

        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(CommonConstants.CIC_CASE_UK_DATE_FORMAT);
        templateVars.put(CommonConstants.CIC_CASE_HEARING_DATE, listing.getDate().format(dateTimeFormatter));
        templateVars.put(CommonConstants.CIC_CASE_HEARING_TIME, listing.getHearingTime());

        if (isVideoFormat(listing) || isTelephoneFormat(listing)) {
            templateVars.put(CommonConstants.CIC_CASE_HEARING_VENUE, CommonConstants.CIC_CASE_RECORD_REMOTE_HEARING);
        } else if (listing.getSelectedVenue() != null) {
            templateVars.put(CommonConstants.CIC_CASE_HEARING_VENUE, listing.getSelectedVenue());
        } else if (listing.getHearingVenueNameAndAddress() != null) {
            templateVars.put(CommonConstants.CIC_CASE_HEARING_VENUE, listing.getHearingVenueNameAndAddress());
        } else {
            templateVars.put(CommonConstants.CIC_CASE_HEARING_VENUE, " ");
        }

        if (listing.getAddlInstr() != null) {
            templateVars.put(CommonConstants.CIC_CASE_HEARING_INFO, listing.getAddlInstr());
        } else {
            templateVars.put(CommonConstants.CIC_CASE_HEARING_INFO, " ");
        }
        if (listing.getVideoCallLink() != null) {
            templateVars.put(CommonConstants.CIC_CASE_RECORD_VIDEO_CALL_LINK, listing.getVideoCallLink());
        } else {
            templateVars.put(CommonConstants.CIC_CASE_RECORD_VIDEO_CALL_LINK, " ");
        }
        if (listing.getConferenceCallNumber() != null) {
            templateVars.put(CommonConstants.CIC_CASE_RECORD_CONF_CALL_NUM, listing.getConferenceCallNumber());
        } else {
            templateVars.put(CommonConstants.CIC_CASE_RECORD_CONF_CALL_NUM, " ");
        }

        if (isVideoFormat(listing)) {
            templateVars.put(CommonConstants.CIC_CASE_RECORD_HEARING_FORMAT_VIDEO, true);
        } else {
            templateVars.put(CommonConstants.CIC_CASE_RECORD_HEARING_FORMAT_VIDEO, false);
        }

        if (isTelephoneFormat(listing)) {
            templateVars.put(CommonConstants.CIC_CASE_RECORD_FORMAT_TEL, true);
        } else {
            templateVars.put(CommonConstants.CIC_CASE_RECORD_FORMAT_TEL, false);
        }

        if (isFaceToFaceFormat(listing)) {
            templateVars.put(CommonConstants.CIC_CASE_RECORD_HEARING_1FACE_TO_FACE, true);
        } else {
            templateVars.put(CommonConstants.CIC_CASE_RECORD_HEARING_1FACE_TO_FACE, false);
        }
    }

    public Map<String, String> buildDocumentList(DynamicMultiSelectList documentList, int docAttachLimit) {
        final Map<String, String> uploadedDocuments = new HashMap<>();

        int count = 0;
        if (ObjectUtils.isNotEmpty(documentList.getValue())) {
            final List<DynamicListElement> documents = documentList.getValue();
            for (DynamicListElement element : documents) {
                count++;
                final String[] labels = element.getLabel().split(MARKUP_SEPARATOR);
                uploadedDocuments.put(DOC_AVAILABLE + count, YES);
                uploadedDocuments.put(CASE_DOCUMENT + count,
                    StringUtils.substringAfterLast(labels[1].substring(1, labels[1].length() - 8),
                        "/"));
                log.debug("Document when Available: {}, {} with value {}", count, uploadedDocuments.get(DOC_AVAILABLE + count),
                    uploadedDocuments.get(CASE_DOCUMENT + count));
            }
        }
        while (count < docAttachLimit) {
            count++;
            uploadedDocuments.put(DOC_AVAILABLE + count, NO);
            uploadedDocuments.put(CASE_DOCUMENT + count, EMPTY_PLACEHOLDER);
            log.debug("Document not Available: {}, {} with value {}", count, uploadedDocuments.get(DOC_AVAILABLE + count),
                uploadedDocuments.get(CASE_DOCUMENT + count));
        }

        return uploadedDocuments;
    }

    public void addHearingPostponedTemplateVars(CicCase cicCase, Map<String, Object> templateVars) {
        final String selectedHearingDateTime = CicCaseFieldsUtil.getSelectedHearingToCancel(cicCase.getHearingList());
        final String[] hearingDateTimeArr = (selectedHearingDateTime != null)
            ? selectedHearingDateTime.split(SPACE + HYPHEN + SPACE) : null;
        final int arrayLength = hearingDateTimeArr != null ? hearingDateTimeArr.length : 0;
        final int lastIndex = arrayLength > 0 ? hearingDateTimeArr.length - 1 : 0;
        final String hearingDate = ArrayUtils.isNotEmpty(hearingDateTimeArr)
            ? hearingDateTimeArr[lastIndex].substring(0, hearingDateTimeArr[lastIndex].lastIndexOf(SPACE))
            : null;
        final String hearingTime = ArrayUtils.isNotEmpty(hearingDateTimeArr)
            ? hearingDateTimeArr[lastIndex].substring(hearingDateTimeArr[lastIndex].lastIndexOf(SPACE) + 1)
            : null;

        templateVars.put(HEARING_DATE, hearingDate);
        templateVars.put(HEARING_TIME, hearingTime);
    }

    private Map<String, Object> commonTemplateVars(final CaseData caseData, final String caseNumber) {
        final CicCase cicCase = caseData.getCicCase();
        final Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(TRIBUNAL_NAME, CIC);
        templateVars.put(CIC_CASE_NUMBER, caseNumber);
        templateVars.put(CIC_CASE_SUBJECT_NAME, cicCase.getFullName());
        if (caseData.getEditCicaCaseDetails() != null && !StringUtils.isEmpty(caseData.getEditCicaCaseDetails().getCicaReferenceNumber())) {
            templateVars.put(HAS_CICA_NUMBER, true);
            templateVars.put(CICA_REF_NUMBER, caseData.getEditCicaCaseDetails().getCicaReferenceNumber());
        } else {
            templateVars.put(HAS_CICA_NUMBER, false);
            templateVars.put(CICA_REF_NUMBER, "");
        }
        return templateVars;
    }

    private boolean isFaceToFaceFormat(Listing listing) {
        return listing.getHearingFormat() != null && listing.getHearingFormat().equals(HearingFormat.FACE_TO_FACE);
    }

    private boolean isVideoFormat(Listing listing) {
        return listing.getHearingFormat() != null && listing.getHearingFormat().equals(HearingFormat.VIDEO);
    }

    private boolean isTelephoneFormat(Listing listing) {
        return listing.getHearingFormat() != null && listing.getHearingFormat().equals(HearingFormat.TELEPHONE);
    }

}
