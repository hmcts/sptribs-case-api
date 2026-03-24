package uk.gov.hmcts.sptribs.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderContentCIC;
import uk.gov.hmcts.sptribs.ciccase.CicCaseFieldsUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;

import java.util.Map;

import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.HEARING_DATE;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.HEARING_TIME;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.HEARING_VENUE_NAME;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.MAIN_CONTENT;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.ORDER_SIGNATURE;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.SUBJECT_FULL_NAME;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.formatter;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateHelper.getCommonFields;

@Component
@Slf4j
public class PreviewDraftOrderTemplateContent {

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
            templateContent.put(SUBJECT_FULL_NAME, caseData.getCicCase().getFullName());
        }

        templateContent.put(ORDER_SIGNATURE, caseData.getDraftOrderContentCIC().getOrderSignature());
        templateContent.put(HEARING_TIME, caseData.getLatestCompletedHearing().getHearingTime());
        templateContent.put(HEARING_VENUE_NAME, caseData.getLatestCompletedHearing().getHearingVenueNameAndAddress());
        templateContent.put(HEARING_DATE, caseData.getLatestCompletedHearing().getDate() != null
            ? caseData.getLatestCompletedHearing().getDate().format(formatter) : "");
        templateContent.put(MAIN_CONTENT, applyAnonymityStatement(caseData));

        return templateContent;
    }

    private static String applyAnonymityStatement(CaseData caseData) {
        CicCase cicCase = caseData.getCicCase();
        DraftOrderContentCIC draftOrder = caseData.getDraftOrderContentCIC();
        String mainContent = draftOrder.getMainContent();

        if (!shouldApplyAnonymityStatement(cicCase) || mainContent == null) {
            return mainContent;
        }

        String anonymisationStatement = DocmosisTemplateConstants.generateAnonymisationStatement(cicCase.getAnonymisationDate());

        if (mainContent.contains(anonymisationStatement.trim())) {
            return mainContent;
        }

        String updatedContent = mainContent + anonymisationStatement;
        draftOrder.setMainContent(updatedContent);
        return updatedContent;
    }

    private static boolean shouldApplyAnonymityStatement(CicCase cicCase) {
        return cicCase != null
                && YesOrNo.YES.equals(cicCase.getAnonymiseYesOrNo())
                && cicCase.getAnonymisedAppellantName() != null
                && cicCase.getAnonymisationDate() != null;
    }
}
