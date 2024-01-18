package uk.gov.hmcts.sptribs.document.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.ccd.sdk.type.Document;

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
        "test.msg",
        "test.eml"
    })
    void shouldCheckDocumentIsValid(String filename) {
        CICDocument document = CICDocument.builder()
            .documentEmailContent("Dear sir/madam, here is an email.")
            .documentLink(Document.builder().filename(filename).build())
            .build();

        Assertions.assertTrue(document.isDocumentValid());

        document = CICDocument.builder()
            .documentEmailContent("Dear sir/madam, here is a message.")
            .documentLink(Document.builder().filename("test.msg").build())
            .build();

        Assertions.assertTrue(document.isDocumentValid());
    }

    @Test
    void shouldCheckDocxDocumentIsInvalid() {
        CICDocument document = CICDocument.builder()
            .documentEmailContent("Dear sir/madam, here is an email.")
            .documentLink(Document.builder().filename("test.docx").build())
            .build();

        Assertions.assertFalse(document.isDocumentValid());
    }

}
