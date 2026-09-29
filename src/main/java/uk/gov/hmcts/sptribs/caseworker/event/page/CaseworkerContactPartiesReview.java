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
            .label("contactPartiesReviewInstructions",
                "Check the information below carefully. To make changes, use **Previous**.")
            .label("contactPartiesReviewDocumentsHeading", "## Documents")
            .label("contactPartiesReviewNoDocuments", "No documents selected",
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
            .label("contactPartiesReviewRecipientsHeading", "## Contact parties")
            .label("contactPartiesReviewSubject", "**Subject:** ${cicCaseFullName}",
                "cicCaseNotifyPartySubjectCONTAINS \"SubjectCIC\"")
            .label("contactPartiesReviewApplicant", "**Applicant:** ${cicCaseApplicantFullName}",
                "cicCaseNotifyPartyApplicantCONTAINS \"ApplicantCIC\"")
            .label("contactPartiesReviewRepresentative", "**Representative:** ${cicCaseRepresentativeFullName}",
                "cicCaseNotifyPartyRepresentativeCONTAINS \"RepresentativeCIC\"")
            .label("contactPartiesReviewRespondent", "**Respondent:** ${cicCaseRespondentName}",
                "cicCaseNotifyPartyRespondentCONTAINS \"RespondentCIC\"")
            .label("contactPartiesReviewMessageHeading", "## Message")
            .label("contactPartiesReviewMessage", "${cicCaseNotifyPartyMessage}");
    }
}
