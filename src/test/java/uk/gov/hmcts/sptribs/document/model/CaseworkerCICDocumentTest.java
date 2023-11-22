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
    public void shouldCheckIsValidForM4AFileType() {
        CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().filename("test.m4a").build())
            .build();

        boolean result = document.isDocumentValid();

        assertThat(result).isTrue();
    }

    @Test
    public void shouldCheckIsValidInvalid() {
        CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().filename("test.xml").build())
            .build();

        boolean result = document.isDocumentValid();

        assertThat(result).isFalse();
    }
}
