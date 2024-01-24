package uk.gov.hmcts.sptribs.document.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.ccd.sdk.type.Document;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        "test.m4a",
        "test.mp4",
        "test.msg",
        "test.eml"
    })
    void shouldCheckIsValid() {
        CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().filename("test.pdf").build())
            .build();

        assertTrue(document.isDocumentValid());

        document = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().filename("test.mp4").build())
            .build();

        assertTrue(document.isDocumentValid());

        document = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.APPLICATION_FORM)
            .documentLink(Document.builder().filename("test.msg").build())
            .build();

        assertTrue(document.isDocumentValid());

        document = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.TRIBUNAL_DIRECTION)
            .documentLink(Document.builder().filename("test.eml").build())
            .build();

        assertTrue(document.isDocumentValid());
    }

    @Test
    void shouldCheckIsValidInvalid() {
        CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().filename("test.xml").build())
            .build();

        assertFalse(document.isDocumentValid());
    }
}
