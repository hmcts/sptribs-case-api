package uk.gov.hmcts.sptribs.document.model;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import uk.gov.hmcts.ccd.sdk.type.Document;

import static org.assertj.core.api.Assertions.assertThat;

class CaseworkerCICDocumentTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "test.pdf",
        "test.csv",
        "test.txt",
        "test.rtf",
        "test.xlsx",
        "test.docx",
        "test.doc",
        "test.xls",
        "test.mp3",
        "test.m4a"
    })
    void shouldCheckIsValid() {
        CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().filename("test.pdf").build())
            .build();

        boolean result = document.isDocumentValid();

        assertThat(result).isTrue();
    }

    @Test
    void shouldCheckIsValidInvalid() {
        CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().filename("test.xml").build())
            .build();

        boolean result = document.isDocumentValid();

        assertThat(result).isFalse();
    }
}
