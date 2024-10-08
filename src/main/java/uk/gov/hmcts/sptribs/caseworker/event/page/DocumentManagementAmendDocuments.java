package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class DocumentManagementAmendDocuments implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "cicCaseAmendDocumentList=\"NEVER_SHOW\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("amendCaseDocuments")
            .pageLabel("Amend case documents")
            .label("LabelAmendCaseDocuments", "")
            .label("LabelAmendCaseDocumentsMessage", "Amend the document details below")
            .complex(CaseData::getCicCase)
            .readonly(CicCase::getSelectedDocumentType, ALWAYS_HIDE)
            .readonly(CicCase::getSelectedDocumentLink, ALWAYS_HIDE)
            .readonly(CicCase::getIsDocumentCreatedFromTemplate, ALWAYS_HIDE)
            .mandatory(CicCase::getSelectedDocumentCategory)
            .mandatory(CicCase::getSelectedDocumentEmailContent)
            .done();
    }
}
