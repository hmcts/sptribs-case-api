package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesDocuments;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

@Component
public class CaseworkerContactPartiesReview implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("contactPartiesReview")
            .pageLabel("Check your answers")
            .label("contactPartiesReviewInstructions",
                "Check the information below carefully. To make changes, use **Previous**.")
            .label("contactPartiesReviewDocumentsHeading", "## Documents")
            .complex(CaseData::getContactPartiesDocuments)
            .readonlyWithLabel(ContactPartiesDocuments::getPreviewDoc, "Selected documents")
            .done()
            .label("contactPartiesReviewRecipientsHeading", "## Contact parties")
            .complex(CaseData::getCicCase)
            .readonlyWithLabel(CicCase::getNotifyPartySubject, "Subject")
            .readonlyWithLabel(CicCase::getNotifyPartyApplicant, "Applicant")
            .readonlyWithLabel(CicCase::getNotifyPartyRepresentative, "Representative")
            .readonlyWithLabel(CicCase::getNotifyPartyRespondent, "Respondent")
            .readonlyWithLabel(CicCase::getNotifyPartyMessage, "Message")
            .done();
    }
}
