package uk.gov.hmcts.sptribs.caseworker.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class ContactPartiesDocumentsTest {

    @Test
    void shouldShowAllTenSelectedDocumentsAndClearUnusedSlots() {
        List<ListValue<CaseworkerCICDocument>> selectedDocuments = IntStream.rangeClosed(1, 10)
            .mapToObj(number -> new ListValue<>(String.valueOf(number), CaseworkerCICDocument.builder()
                .documentLink(Document.builder().filename("document-" + number + ".pdf").build())
                .build()))
            .toList();
        ContactPartiesDocuments documents = new ContactPartiesDocuments();

        documents.setReviewDocuments(selectedDocuments);

        assertThat(documents.getReviewDocument1().getDocumentLink().getFilename()).isEqualTo("document-1.pdf");
        assertThat(documents.getReviewDocument10().getDocumentLink().getFilename()).isEqualTo("document-10.pdf");

        documents.setReviewDocuments(selectedDocuments.subList(0, 1));

        assertThat(documents.getReviewDocument1().getDocumentLink().getFilename()).isEqualTo("document-1.pdf");
        assertThat(documents.getReviewDocument2()).isNull();
        assertThat(documents.getReviewDocument10()).isNull();
    }
}
