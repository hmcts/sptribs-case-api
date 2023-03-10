package uk.gov.hmcts.sptribs.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

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
        templateContent.put(SUBJECT_FULL_NAME, caseData.getListing().getSummary().getSubjectName());
        templateContent.put(HEARING_TYPE, caseData.getListing().getHearingType());
        templateContent.put(TRIBUNAL_MEMBERS, getMembers(caseData.getListing().getSummary().getMemberList()));
        templateContent.put(DECISION_SIGNATURE, caseData.getDecisionSignature());
        templateContent.put(HEARING_TIME, caseData.getListing().getHearingTime());
        templateContent.put(HEARING_VENUE_NAME, caseData.getListing().getHearingVenueNameAndAddress());
        templateContent.put(HEARING_DATE, caseData.getListing().getHearingDate() != null
            ? caseData.getListing().getHearingDate().format(formatter) : "");
        templateContent.put(MAIN_CONTENT, caseData.getDecisionMainContent());

        return templateContent;
    }

}
