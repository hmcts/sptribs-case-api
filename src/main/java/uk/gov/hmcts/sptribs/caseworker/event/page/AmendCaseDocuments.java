package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.DocumentManagement;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class AmendCaseDocuments implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("amendCaseDocuments")
            .pageLabel("Amend case documents")
            .label("LabelAmendCaseDocuments", "Amend the document details below")
            .label("LabelAmendCaseDocumentsWarning", "")
            .complex(CaseData::getDocManagement)
            .optional(DocumentManagement::getDocumentList)
            .done();
    }
}
