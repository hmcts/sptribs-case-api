package uk.gov.hmcts.sptribs.document.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.ciccase.CicCaseFieldsUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

import java.util.Map;

import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.DECISION_SIGNATURE;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.HEARING_DATE;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.HEARING_TIME;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.HEARING_VENUE_NAME;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.MAIN_CONTENT;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.REPRESENTATIVE_FULL_NAME;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.SUBJECT_FULL_NAME;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.formatter;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateHelper.getCommonFields;

@Component
public class DecisionTemplateContent {


    public Map<String, Object> apply(final CaseData caseData,
                                     final Long ccdCaseReference) {

        Map<String, Object> templateContent = getCommonFields(caseData, ccdCaseReference);

        if (caseData.getCicCase().getAnonymiseYesOrNo() != null && caseData.getCicCase().getAnonymiseYesOrNo().equals(YesOrNo.YES)
                && caseData.getCicCase().getAnonymisedAppellantName() != null) {
            templateContent.put(SUBJECT_FULL_NAME, caseData.getCicCase().getAnonymisedAppellantName());
        } else if (CicCaseFieldsUtil.useApplicantNameForSubject(
            caseData.getCicCase().getCaseSubcategory(), caseData.getCicCase().getApplicantFullName()
        )) {
            templateContent.put(SUBJECT_FULL_NAME, caseData.getCicCase().getApplicantFullName());
        } else {
            templateContent.put(SUBJECT_FULL_NAME, caseData.getLatestCompletedHearing().getSummary().getSubjectName());
        }

        templateContent.put(REPRESENTATIVE_FULL_NAME, caseData.getCicCase().getRepresentativeFullName());
        templateContent.put(DECISION_SIGNATURE, caseData.getDecisionSignature());
        templateContent.put(MAIN_CONTENT, caseData.getDecisionMainContent());
        templateContent.put(HEARING_TIME, caseData.getLatestCompletedHearing().getHearingTime());
        templateContent.put(HEARING_VENUE_NAME, caseData.getLatestCompletedHearing().getHearingVenueNameAndAddress());
        templateContent.put(HEARING_DATE, caseData.getLatestCompletedHearing().getDate() != null
            ? caseData.getLatestCompletedHearing().getDate().format(formatter) : "");
        return templateContent;
    }

}
