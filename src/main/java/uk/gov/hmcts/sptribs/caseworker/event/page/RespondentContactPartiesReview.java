package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.ContactParties;
import uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesDocuments;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

@Component
public class RespondentContactPartiesReview implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("contactPartiesReview")
            .pageLabel("Check your answers")
            .label("respondentContactPartiesReviewInstructions",
                "Check the information below carefully. To make changes, use **Previous**.")
            .label("respondentContactPartiesReviewDocumentsHeading", "## Documents")
            .complex(CaseData::getContactPartiesDocuments)
            .readonlyWithLabel(ContactPartiesDocuments::getPreviewDoc, "Selected documents")
            .done()
            .label("respondentContactPartiesReviewRecipientsHeading", "## Contact parties")
            .complex(CaseData::getContactParties)
            .readonlyWithLabel(ContactParties::getSubjectContactParties, "Subject")
            .readonlyWithLabel(ContactParties::getApplicantContactParties, "Applicant")
            .readonlyWithLabel(ContactParties::getRepresentativeContactParties, "Representative")
            .readonlyWithLabel(ContactParties::getTribunal, "Tribunal")
            .done()
            .complex(CaseData::getCicCase)
            .readonlyWithLabel(CicCase::getNotifyPartyMessage, "Message")
            .done();
    }
}
