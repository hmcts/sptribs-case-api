package uk.gov.hmcts.sptribs.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

import java.util.Map;

import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.MAIN_CONTENT;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.REPRESENTATIVE_FULL_NAME;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateHelper.getCommonFields;

@Component
@Slf4j
public class PreviewDraftOrderTemplateContent {

    public Map<String, Object> apply(final CaseData caseData,
                                     final Long ccdCaseReference) {

        Map<String, Object> templateContent = getCommonFields(caseData, ccdCaseReference);
        templateContent.put(REPRESENTATIVE_FULL_NAME, caseData.getCicCase().getRepresentativeFullName());
        templateContent.put(MAIN_CONTENT, caseData.getDraftOrderMainContentCIC().getMainContent());

        return templateContent;
    }
}
