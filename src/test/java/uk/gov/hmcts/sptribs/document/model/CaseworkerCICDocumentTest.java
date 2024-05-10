package uk.gov.hmcts.sptribs.document.model;

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
        "test.mp4"
    })
    void shouldCheckDocumentIsValid(String filename) {
        CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().filename(filename).build())
            .build();

        assertTrue(document.isDocumentValid());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "test.xml",
        "test.eml",
        "test.msg",
        "test.uml"
    })
    void shouldCheckDocumentIsInvalid(String filename) {
        CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().filename(filename).build())
            .build();

        assertFalse(document.isDocumentValid());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "test.pdf",
        "test.txt",
        "test.xlsx",
        "test.docx",
        "test.doc",
        "test.xls",
    })
    void shouldCheckIsValidBundleDocument(String filename) {
        CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().filename(filename).build())
            .build();

        assertTrue(document.isValidBundleDocument());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "test.xml",
        "test.eml",
        "test.msg",
        "test.uml",
        "test.csv",
        "test.mp3",
        "test.m4a",
        "test.mp4"
    })
    void shouldCheckIsInvalidBundleDocument(String filename) {
        CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().filename(filename).build())
            .build();

        assertFalse(document.isValidBundleDocument());
    }
}
