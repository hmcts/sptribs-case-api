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
            .label("uploadCaseDocumentLabel", "Upload a copy of the order that you want to issue as part of this case")
            .label("uploadCaseDocumentConditions", """
                The order should be:
                 *  uploaded separately, not in one large file
                 *  a maximum of 100MB in size (larger files must be split)
                 *  labelled clearly, e.g. applicant-name-decision-notice.pdf
                 """
            )
            .complex(CaseData::getDocManagement)
            .mandatory(DocumentManagement::getCaseworkerCICDocument)
            .done();
    }
}
