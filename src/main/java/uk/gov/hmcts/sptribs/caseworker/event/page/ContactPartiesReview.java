package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

@Component
public class ContactPartiesReview implements CcdPageConfiguration {

    private static final String CHANGE_DOCUMENTS_LINK =
        "[Change documents](/callbacks/mid-event?page=contactPartiesSelectDocument&eventId=contact-parties)";
    private static final String CHANGE_PARTIES_LINK =
        "[Change contact parties](/callbacks/mid-event?page=partiesToContact&eventId=contact-parties)";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("contactPartiesReview")
            .pageLabel("Check your answers")
            .label("contactPartiesReviewLabel", "Review the message details before sending.")
            .complex(CaseData::getContactPartiesReview)
            .label("contactPartiesReviewDocumentsChange", CHANGE_DOCUMENTS_LINK)
            .label("contactPartiesReviewDocuments", "### Documents")
            .readonlyWithLabel(
                uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesReview::getPreviewDoc,
                "Selected documents"
            )
            .label("contactPartiesReviewRecipientsChange", CHANGE_PARTIES_LINK)
            .label("contactPartiesReviewRecipients", "### Contact parties")
            .readonlyWithLabel(
                uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesReview::getContactParties,
                "Recipients"
            )
            .readonlyWithLabel(
                uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesReview::getMessage,
                "Message"
            )
            .done();
    }
}
