package uk.gov.hmcts.sptribs.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CaseSubcategory;

import java.util.Map;

import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.DECISION_SIGNATURE;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.HEARING_DATE;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.HEARING_TIME;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.HEARING_TYPE;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.HEARING_VENUE_NAME;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.MAIN_CONTENT;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.SUBJECT_FULL_NAME;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.TRIBUNAL_MEMBERS;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.formatter;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateHelper.getCommonFields;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateHelper.getMembers;

@Component
@Slf4j
public class FinalDecisionTemplateContent {

    public Map<String, Object> apply(final CaseData caseData,
                                     final Long ccdCaseReference) {

        Map<String, Object> templateContent = getCommonFields(caseData, ccdCaseReference);

        if ((caseData.getCicCase().getCaseSubcategory() == CaseSubcategory.FATAL
            || caseData.getCicCase().getCaseSubcategory() == CaseSubcategory.MINOR)
            && !caseData.getCicCase().getApplicantFullName().isEmpty()) {
            templateContent.put(SUBJECT_FULL_NAME, caseData.getCicCase().getApplicantFullName());
        } else {
            templateContent.put(SUBJECT_FULL_NAME, caseData.getLatestCompletedHearing().getSummary().getSubjectName());
        }

        templateContent.put(HEARING_TYPE, caseData.getLatestCompletedHearing().getHearingType());
        templateContent.put(TRIBUNAL_MEMBERS, getMembers(caseData.getLatestCompletedHearing().getSummary().getMemberList()));
        templateContent.put(DECISION_SIGNATURE, caseData.getDecisionSignature());
        templateContent.put(HEARING_TIME, caseData.getLatestCompletedHearing().getHearingTime());
        templateContent.put(HEARING_VENUE_NAME, caseData.getLatestCompletedHearing().getHearingVenueNameAndAddress());
        templateContent.put(HEARING_DATE, caseData.getLatestCompletedHearing().getDate() != null
            ? caseData.getLatestCompletedHearing().getDate().format(formatter) : "");
        templateContent.put(MAIN_CONTENT, caseData.getDecisionMainContent());

        return templateContent;
    }

}
