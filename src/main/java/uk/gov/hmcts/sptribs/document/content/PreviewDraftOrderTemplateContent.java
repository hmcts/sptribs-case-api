package uk.gov.hmcts.sptribs.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.CASE_NUMBER;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.CIC_CASE_SCHEME;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.DATED;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.REPRESENTATIVE_FULL_NAME;
import static uk.gov.hmcts.sptribs.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class PreviewDraftOrderTemplateContent {

    public Map<String, Object> apply(final CaseData caseData,
                                     final Long ccdCaseReference) {

        Map<String, Object> templateContent = new HashMap<>();

        templateContent.put(DATED, LocalDate.now().format(DATE_TIME_FORMATTER));
        templateContent.put(CIC_CASE_SCHEME, caseData.getCicCase().getSchemeCic().getLabel());
        templateContent.put(CASE_NUMBER, ccdCaseReference);
        templateContent.put(REPRESENTATIVE_FULL_NAME, caseData.getCicCase().getRepresentativeFullName());

        return templateContent;
    }
}
