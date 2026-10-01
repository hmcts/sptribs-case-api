package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesDocuments;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

@Component
public class RespondentContactPartiesReview implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("contactPartiesReview")
            .pageLabel("Check your answers")
            .complex(CaseData::getContactPartiesDocuments)
            .readonly(ContactPartiesDocuments::getD01, "contactPartiesDocumentsActCONTAINS \"D01\"")
            .readonly(ContactPartiesDocuments::getD02, "contactPartiesDocumentsActCONTAINS \"D02\"")
            .readonly(ContactPartiesDocuments::getD03, "contactPartiesDocumentsActCONTAINS \"D03\"")
            .readonly(ContactPartiesDocuments::getD04, "contactPartiesDocumentsActCONTAINS \"D04\"")
            .readonly(ContactPartiesDocuments::getD05, "contactPartiesDocumentsActCONTAINS \"D05\"")
            .readonly(ContactPartiesDocuments::getD06, "contactPartiesDocumentsActCONTAINS \"D06\"")
            .readonly(ContactPartiesDocuments::getD07, "contactPartiesDocumentsActCONTAINS \"D07\"")
            .readonly(ContactPartiesDocuments::getD08, "contactPartiesDocumentsActCONTAINS \"D08\"")
            .readonly(ContactPartiesDocuments::getD09, "contactPartiesDocumentsActCONTAINS \"D09\"")
            .readonly(ContactPartiesDocuments::getD10, "contactPartiesDocumentsActCONTAINS \"D10\"")
            .readonly(ContactPartiesDocuments::getReviewSelectedParties)
            .readonly(ContactPartiesDocuments::getReviewMessage)
            .done();
    }
}
