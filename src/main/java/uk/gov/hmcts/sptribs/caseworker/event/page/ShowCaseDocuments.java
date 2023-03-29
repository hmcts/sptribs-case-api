package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.CloseCase;
import uk.gov.hmcts.sptribs.caseworker.model.DocumentManagement;
import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class ShowCaseDocuments implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("showCaseDocuments")
            .pageLabel("Show case documents")
            .label("LabelShowCaseDocuments", "")
            .complex(CaseData::getDocManagement)
            .readonlyWithLabel(DocumentManagement::getCaseworkerCICDocument, "Document Management Files")
            .done()
            .complex(CaseData::getCloseCase)
            .readonly(CloseCase::getDocuments)
            .done()
            .complex(CaseData::getCicCase)
            .readonly(CicCase::getReinstateDocuments)
            .readonly(CicCase::getApplicantDocumentsUploaded)
            .readonly(CicCase::getOrderDocumentList)
            .readonly(CicCase::getFinalDecisionDocumentList)
            .readonly(CicCase::getDecisionDocumentList)
            .done()
            .complex(CaseData::getListing)
            .complex(Listing::getSummary)
            .readonlyWithLabel(HearingSummary::getRecFile, "Hearing Summary Documents")
            .done();
    }
}
