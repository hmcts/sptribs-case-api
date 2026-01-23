package uk.gov.hmcts.sptribs.document.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.ccd.sdk.type.Document;

import static org.junit.jupiter.api.Assertions.*;

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
        "test.jpeg",
        "test.jpg",
        "test.tiff",
        "test.bmp",
        "test.gif",
        "test.svg",
        "test.png",
        "test.PDF",
        "test.tXt",
        "test.XlSx",
        "test.DOCx",
        "test.Doc",
        "test.xlS",
        "test.JPeg",
        "test.JpG",
        "test.TiFF",
        "test.BMp",
        "test.gIF",
        "test.sVg",
        "test.pNG",
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

    @Test
    void shouldGetDocumentCategoryOfDocumentLink() {
        CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().filename("test.pdf").categoryId("DSS").build())
            .build();

        assertEquals(DocumentType.DSS_TRIBUNAL_FORM, document.getDocumentCategory());
    }

    @Test
    void shouldReturnNullDocumentCategoryIfCategoryIdDoesNotExistForGetDocumentCategoryOfDocumentLink() {
        CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().filename("test.pdf").categoryId("Does not exist in DocumentType").build())
            .build();

        assertNull(document.getDocumentCategory());
    }
}
