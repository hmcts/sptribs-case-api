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
            .complex(CaseData::getContactPartiesDocuments)
            .readonlyWithLabel(ContactPartiesDocuments::getPreviewDoc, "Selected documents")
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
