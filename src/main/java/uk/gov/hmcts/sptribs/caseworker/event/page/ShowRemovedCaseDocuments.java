package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class ShowRemovedCaseDocuments implements CcdPageConfiguration {


    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("showRemovedCaseDocuments")
            .pageLabel("Removed documents")
            .label("LabelShowRemovedCaseDocuments", "")
            .label("LabelShowRemovedCaseDocumentsWarning", "Below documents will be removed")
            .complex(CaseData::getCicCase)
            .readonly(CicCase::getReadOnlyRemovedDocList)
            .done();
    }


}
