package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesDocuments;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

@Component
public class ContactPartiesReview implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("contactPartiesReview")
            .pageLabel("Check your answers")
            .complex(CaseData::getContactPartiesDocuments)
            .readonly(ContactPartiesDocuments::getD01, "contactPartiesDocumentsActiveDocumentSlotsCONTAINS \"D01\"")
            .readonly(ContactPartiesDocuments::getD02, "contactPartiesDocumentsActiveDocumentSlotsCONTAINS \"D02\"")
            .readonly(ContactPartiesDocuments::getD03, "contactPartiesDocumentsActiveDocumentSlotsCONTAINS \"D03\"")
            .readonly(ContactPartiesDocuments::getD04, "contactPartiesDocumentsActiveDocumentSlotsCONTAINS \"D04\"")
            .readonly(ContactPartiesDocuments::getD05, "contactPartiesDocumentsActiveDocumentSlotsCONTAINS \"D05\"")
            .readonly(ContactPartiesDocuments::getD06, "contactPartiesDocumentsActiveDocumentSlotsCONTAINS \"D06\"")
            .readonly(ContactPartiesDocuments::getD07, "contactPartiesDocumentsActiveDocumentSlotsCONTAINS \"D07\"")
            .readonly(ContactPartiesDocuments::getD08, "contactPartiesDocumentsActiveDocumentSlotsCONTAINS \"D08\"")
            .readonly(ContactPartiesDocuments::getD09, "contactPartiesDocumentsActiveDocumentSlotsCONTAINS \"D09\"")
            .readonly(ContactPartiesDocuments::getD10, "contactPartiesDocumentsActiveDocumentSlotsCONTAINS \"D10\"")
            .readonly(ContactPartiesDocuments::getReviewSelectedParties)
            .readonly(ContactPartiesDocuments::getReviewMessage)
            .done();
    }
}
