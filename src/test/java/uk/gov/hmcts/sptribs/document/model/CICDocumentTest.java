package uk.gov.hmcts.sptribs.document.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.ccd.sdk.type.Document;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CICDocumentTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "test.pdf",
        "test.tif",
        "test.tiff",
        "test.jpg",
        "test.jpeg",
        "test.png",
        "test.mp3",
        "test.m4a",
        "test.mp4",
        "test.csv",
        "test.txt",
        "test.rtf",
        "test.xlsx",
        "test.docx",
        "test.doc",
        "test.jpg",
        "test.jpeg",
        "test.bmp",
        "test.tif",
        "test.tiff",
        "test.png"
    })
    void shouldCheckDocumentIsValid(String filename) {
        CICDocument document = CICDocument.builder()
            .documentEmailContent("Dear sir/madam, here is an email.")
            .documentLink(Document.builder().filename(filename).build())
            .build();

        assertTrue(document.isDocumentValid());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "test.PDF",
        "test.TIF",
        "test.TIFF",
        "test.JPG",
        "test.JPEG",
        "test.pnG",
        "test.mP3",
        "test.M4a",
        "test.MP4",
        "test.cSv",
        "test.txT",
        "test.RTF",
        "test.XLSX",
        "test.DOCX",
        "test.Doc",
        "test.Xls",
        "test.JPG",
        "test.Jpeg",
        "test.bMP",
        "test.TIf",
        "test.TIff",
        "test.PnG"
    })
    void shouldCheckUppercaseDocumentExtensionIsValid(String filename) {
        CICDocument document = CICDocument.builder()
            .documentEmailContent("Dear sir/madam, here is an email.")
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
    void shouldCheckDocxDocumentIsInvalid(String filename) {
        CICDocument document = CICDocument.builder()
            .documentEmailContent("Dear sir/madam, here is an email.")
            .documentLink(Document.builder().filename(filename).build())
            .build();

        assertFalse(document.isDocumentValid());
    }

}
