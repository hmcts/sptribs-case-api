package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesDocuments;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

@Component
public class CaseworkerContactPartiesReview implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("contactPartiesReview")
            .pageLabel("Check your answers")
            .complex(CaseData::getContactPartiesDocuments)
            .readonly(ContactPartiesDocuments::getD01, "contactPartiesDocumentsD01!=\"\"")
            .readonly(ContactPartiesDocuments::getD02, "contactPartiesDocumentsD02!=\"\"")
            .readonly(ContactPartiesDocuments::getD03, "contactPartiesDocumentsD03!=\"\"")
            .readonly(ContactPartiesDocuments::getD04, "contactPartiesDocumentsD04!=\"\"")
            .readonly(ContactPartiesDocuments::getD05, "contactPartiesDocumentsD05!=\"\"")
            .readonly(ContactPartiesDocuments::getD06, "contactPartiesDocumentsD06!=\"\"")
            .readonly(ContactPartiesDocuments::getD07, "contactPartiesDocumentsD07!=\"\"")
            .readonly(ContactPartiesDocuments::getD08, "contactPartiesDocumentsD08!=\"\"")
            .readonly(ContactPartiesDocuments::getD09, "contactPartiesDocumentsD09!=\"\"")
            .readonly(ContactPartiesDocuments::getD10, "contactPartiesDocumentsD10!=\"\"")
            .readonly(ContactPartiesDocuments::getReviewSelectedParties)
            .readonly(ContactPartiesDocuments::getReviewMessage)
            .done();
    }
}
