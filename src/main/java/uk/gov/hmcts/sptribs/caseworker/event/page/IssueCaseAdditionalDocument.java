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
            .mandatory(CaseIssue::getTribunalDocuments, "issueCaseAdditionalDocumentCONTAINS  \"Tribunal form\"")
            .mandatory(CaseIssue::getApplicationEvidences, "issueCaseAdditionalDocumentCONTAINS  \"Applicant evidence\"")
            .done();
    }

}
