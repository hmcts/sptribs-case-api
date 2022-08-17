package uk.gov.hmcts.sptribs.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.Applicant;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

import java.util.Map;

import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.DISPLAY_HEADER_ADDRESS;

@Component
@Slf4j
public class NoticeOfProceedingsWithAddressContent {

    @Autowired
    private NoticeOfProceedingContent noticeOfProceedingContent;

    public Map<String, Object> apply(final CaseData caseData, final Long ccdCaseReference, final Applicant partner) {

        final Map<String, Object> templateContent = noticeOfProceedingContent.apply(caseData, ccdCaseReference, partner);
        templateContent.put(DISPLAY_HEADER_ADDRESS, true);

        return templateContent;
    }
}
