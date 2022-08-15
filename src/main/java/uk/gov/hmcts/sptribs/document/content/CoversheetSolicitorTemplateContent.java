package uk.gov.hmcts.sptribs.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.sptribs.notification.FormatUtil.formatId;

@Component
@Slf4j
public class CoversheetSolicitorTemplateContent {

    public Map<String, Object> apply(final CaseData caseData, final Long ccdCaseReference) {
        final Map<String, Object> templateContent = new HashMap<>();
        templateContent.put(CASE_REFERENCE, formatId(ccdCaseReference));
        templateContent.put(SOLICITOR_NAME, caseData.getApplicant2().getSolicitor().getName());
        templateContent.put(SOLICITOR_ADDRESS, caseData.getApplicant2().getSolicitor().getAddress());
        return templateContent;
    }
}
