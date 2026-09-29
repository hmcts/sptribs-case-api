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
            .label("respondentContactPartiesReviewInstructions",
                "Check the information below carefully. To make changes, use **Previous**.")
            .label("respondentContactPartiesReviewDocumentsHeading", "## Documents")
            .label("respondentContactPartiesReviewNoDocuments", "No documents selected",
                "contactPartiesDocumentsReviewDocument1=\"\"")
            .complex(CaseData::getContactPartiesDocuments)
            .readonly(ContactPartiesDocuments::getReviewDocument1, "contactPartiesDocumentsReviewDocument1!=\"\"")
            .readonly(ContactPartiesDocuments::getReviewDocument2, "contactPartiesDocumentsReviewDocument2!=\"\"")
            .readonly(ContactPartiesDocuments::getReviewDocument3, "contactPartiesDocumentsReviewDocument3!=\"\"")
            .readonly(ContactPartiesDocuments::getReviewDocument4, "contactPartiesDocumentsReviewDocument4!=\"\"")
            .readonly(ContactPartiesDocuments::getReviewDocument5, "contactPartiesDocumentsReviewDocument5!=\"\"")
            .readonly(ContactPartiesDocuments::getReviewDocument6, "contactPartiesDocumentsReviewDocument6!=\"\"")
            .readonly(ContactPartiesDocuments::getReviewDocument7, "contactPartiesDocumentsReviewDocument7!=\"\"")
            .readonly(ContactPartiesDocuments::getReviewDocument8, "contactPartiesDocumentsReviewDocument8!=\"\"")
            .readonly(ContactPartiesDocuments::getReviewDocument9, "contactPartiesDocumentsReviewDocument9!=\"\"")
            .readonly(ContactPartiesDocuments::getReviewDocument10, "contactPartiesDocumentsReviewDocument10!=\"\"")
            .done()
            .label("respondentContactPartiesReviewRecipientsHeading", "## Contact parties")
            .label("respondentContactPartiesReviewSubject", "**Subject:** ${cicCaseFullName}",
                "contactParties.subjectContactPartiesCONTAINS \"SubjectCIC\"")
            .label("respondentContactPartiesReviewApplicant", "**Applicant:** ${cicCaseApplicantFullName}",
                "contactParties.applicantContactPartiesCONTAINS \"ApplicantCIC\"")
            .label("respondentContactPartiesReviewRepresentative", "**Representative:** ${cicCaseRepresentativeFullName}",
                "contactParties.representativeContactPartiesCONTAINS \"RepresentativeCIC\"")
            .label("respondentContactPartiesReviewTribunal", "**Tribunal**",
                "contactParties.tribunalCONTAINS \"TribunalCIC\"")
            .label("respondentContactPartiesReviewMessageHeading", "## Message")
            .label("respondentContactPartiesReviewMessage", "${cicCaseNotifyPartyMessage}");
    }
}
