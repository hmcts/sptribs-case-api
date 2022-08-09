package uk.gov.hmcts.sptribs.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.sptribs.notification.FormatUtil.formatId;

@Component
@Slf4j
public class CoversheetApplicant2TemplateContent {

    public Map<String, Object> apply(final CaseData caseData,
                                     final Long ccdCaseReference) {
        final Map<String, Object> templateContent = new HashMap<>();
        templateContent.put(CASE_REFERENCE, formatId(ccdCaseReference));
        templateContent.put("applicantFirstName", caseData.getApplicant2().getFirstName());
        templateContent.put("applicantLastName", caseData.getApplicant2().getLastName());
        templateContent.put("applicantAddress", caseData.getApplicant2().getPostalAddress());
        return templateContent;
    }
}
