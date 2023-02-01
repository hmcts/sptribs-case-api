package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.CaseIssue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;


public class IssueCaseAdditionalDocument implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("selectAdditionalDocument")
            .pageLabel("Select documentation")
            .label("LabelSelectAdditionalDocument", "")
            .complex(CaseData::getCaseIssue)
            .mandatory(CaseIssue::getAdditionalDocument)
            .label("tribunalDocuments", """
                        * Application Form
                        * First decision
                        * Application for review
                        * Review decision
                        * Notice of Appeal
                        * Evidence/correspondence from the Appellant
                        * Correspondence from the CICA
                """, "issueCaseAdditionalDocumentCONTAINS  \"Tribunal form\"")
            .label("applicantEvidenceDocuments", """
                            * Police evidence
                            * GP records
                            * Hospital records
                            * Mental Health records
                            * Expert evidence
                            * Other medical records
                            * Application for an extension of time
                            * Application for a postponement
                            * Submission from appellant
                            * Submission from respondent
                            * Other
                """, "issueCaseAdditionalDocumentCONTAINS  \"Applicant evidence\"")
            .done();
    }

}
