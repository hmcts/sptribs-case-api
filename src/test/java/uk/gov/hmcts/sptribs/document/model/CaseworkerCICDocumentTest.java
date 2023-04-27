package uk.gov.hmcts.sptribs.document.model;


import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.Document;

import static org.assertj.core.api.Assertions.assertThat;

public class CaseworkerCICDocumentTest {

    @Test
    public void shouldCheckIsValid() {
        CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().filename("test.pdf").build())
            .build();

        boolean result = document.isDocumentValid();

        assertThat(result).isTrue();
    }

    @Test
    public void shouldCheckIsValidInvalid() {
        CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().filename("test.docx").build())
            .build();

        boolean result = document.isDocumentValid();

        assertThat(result).isFalse();
    }

    @Test
    public void shouldCheckIsValidForEmail() {
        CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().filename("test.pdf").build())
            .build();

        boolean result = document.isDocumentValidForEmail();

        assertThat(result).isTrue();
    }

    @Test
    public void shouldCheckIsValidForEmailInvalid() {
        CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().filename("test.png").build())
            .build();

        boolean result = document.isDocumentValidForEmail();

        assertThat(result).isFalse();
    }
}
