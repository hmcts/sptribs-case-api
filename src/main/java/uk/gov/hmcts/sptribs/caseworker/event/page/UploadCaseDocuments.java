package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.DocumentManagement;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class UploadCaseDocuments implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("uploadCaseDocuments")
            .pageLabel("Upload case documents")
            .label("LabelUploadCaseDocuments", "")
            .label("uploadCaseDocumentLabel", "Please upload copies of any information or evidence that you want to add to this case")
            .label("uploadCaseDocumentConditions", """
                Files should be:
                 *  uploaded separately and not in one large file
                 *  a maximum of 100MB in size (larger files must be split)
                 *  labelled clearly, e.g. applicant-name-B1-form.pdf
                 """
            )
            .complex(CaseData::getDocManagement)
            .mandatory(DocumentManagement::getCaseworkerCICDocument)
            .done();
    }
}
