package uk.gov.hmcts.sptribs.common.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.AcknowledgementOfService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.task.CaseTask;
import uk.gov.hmcts.sptribs.document.CaseDataDocumentService;
import uk.gov.hmcts.sptribs.document.content.AosResponseLetterTemplateContent;
import uk.gov.hmcts.sptribs.document.content.AosUndefendedResponseLetterTemplateContent;

import static uk.gov.hmcts.sptribs.document.DocumentConstants.AOS_RESPONSE_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.RESPONDENT_RESPONDED_DISPUTED_TEMPLATE_ID;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.RESPONDENT_RESPONDED_UNDEFENDED_TEMPLATE_ID;
import static uk.gov.hmcts.sptribs.document.model.DocumentType.AOS_RESPONSE_LETTER;

@Component
@Slf4j
public class GenerateAosResponseLetterDocument implements CaseTask {

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private AosResponseLetterTemplateContent aosResponseLetterTemplateContent;

    @Autowired
    private AosUndefendedResponseLetterTemplateContent aosUndefendedResponseLetterTemplateContent;

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> caseDetails) {

        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();
        final AcknowledgementOfService acknowledgementOfService = caseData.getAcknowledgementOfService();

        if (caseData.getApplicant1().isOffline()) {

            if (acknowledgementOfService.isDisputed()) {
                log.info("Generating aos response (disputed) letter pdf for case id: {}", caseDetails.getId());
                caseDataDocumentService.renderDocumentAndUpdateCaseData(
                    caseData,
                    AOS_RESPONSE_LETTER,
                    aosResponseLetterTemplateContent.apply(caseData, caseId),
                    caseId,
                    RESPONDENT_RESPONDED_DISPUTED_TEMPLATE_ID,
                    caseData.getApplicant1().getLanguagePreference(),
                    AOS_RESPONSE_LETTER_DOCUMENT_NAME
                );
            } else {
                log.info("Generating aos response (undefended) letter pdf for case id: {}", caseId);
                caseDataDocumentService.renderDocumentAndUpdateCaseData(
                    caseData,
                    AOS_RESPONSE_LETTER,
                    aosUndefendedResponseLetterTemplateContent.apply(caseData, caseId),
                    caseId,
                    RESPONDENT_RESPONDED_UNDEFENDED_TEMPLATE_ID,
                    caseData.getApplicant1().getLanguagePreference(),
                    AOS_RESPONSE_LETTER_DOCUMENT_NAME
                );
            }
        }
        return caseDetails;
    }
}
