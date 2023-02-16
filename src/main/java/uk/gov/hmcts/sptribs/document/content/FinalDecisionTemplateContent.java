package uk.gov.hmcts.sptribs.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.PanelMember;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.CASE_NUMBER;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.CIC_CASE_SCHEME;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.DATED;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.DECISION_SIGNATURE;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.HEARING_DATE;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.HEARING_TIME;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.HEARING_TYPE;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.HEARING_VENUE_NAME;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.SUBJECT_FULL_NAME;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.TRIBUNAL_MEMBERS;
import static uk.gov.hmcts.sptribs.notification.FormatUtil.FILE_NAME_DATE_FORMATTER;

@Component
@Slf4j
public class FinalDecisionTemplateContent {

    private static final String COMMA_SPACE = ", ";

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public Map<String, Object> apply(final CaseData caseData,
                                     final Long ccdCaseReference) {

        Map<String, Object> templateContent = new HashMap<>();

        templateContent.put(DATED, LocalDate.now().format(FILE_NAME_DATE_FORMATTER));
        templateContent.put(CIC_CASE_SCHEME, caseData.getCicCase().getSchemeCic().getLabel());
        templateContent.put(CASE_NUMBER, ccdCaseReference);
        templateContent.put(SUBJECT_FULL_NAME, caseData.getHearingSummary().getSubjectName());
        templateContent.put(HEARING_TYPE, caseData.getHearingSummary().getHearingType());
        templateContent.put(TRIBUNAL_MEMBERS, getMembers(caseData.getHearingSummary().getPanelMemberList()));
        templateContent.put(DECISION_SIGNATURE, caseData.getFinalDecisionSignature());
        templateContent.put(HEARING_TIME, caseData.getRecordListing().getHearingTime());
        templateContent.put(HEARING_VENUE_NAME, caseData.getRecordListing().getHearingVenueNameAndAddress());
        templateContent.put(HEARING_DATE, caseData.getRecordListing().getHearingDate().format(formatter));

        return templateContent;
    }

    private String getMembers(List<ListValue<PanelMember>> memberList) {

        if (!CollectionUtils.isEmpty(memberList)) {
            StringBuilder members = new StringBuilder(100);
            for (ListValue<PanelMember> panelMember : memberList) {
                members.append(panelMember.getValue().getName().getValue().getLabel()).append(COMMA_SPACE);
            }
            return members.substring(0, members.length() - 2);
        }
        return "";
    }
}
