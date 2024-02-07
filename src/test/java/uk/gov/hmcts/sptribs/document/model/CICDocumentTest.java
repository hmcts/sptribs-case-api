package uk.gov.hmcts.sptribs.document.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.ccd.sdk.type.Document;

import java.text.MessageFormat;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CICDocumentTest {


    @ParameterizedTest
    @MethodSource("fileNameAndMessageProvider")
    void shouldCheckDocumentIsValid(String filename, String fileMessage) {
        CICDocument document = CICDocument.builder()
            .documentEmailContent(MessageFormat.format("Dear sir/madam, here is an email with an attached {0}.", fileMessage))
            .documentLink(Document.builder().filename(filename).build())
            .build();

        assertTrue(document.isDocumentValid());
    }

    @Test
    void shouldCheckDocxDocumentIsInvalid() {
        CICDocument document = CICDocument.builder()
            .documentEmailContent("Dear sir/madam, here is an email.")
            .documentLink(Document.builder().filename("test.uml").build())
            .build();

        assertFalse(document.isDocumentValid());
    }

    static Stream<Arguments> fileNameAndMessageProvider() {
        return Stream.of(
            Arguments.arguments("test.pdf", ".pdf document"),
            Arguments.arguments("test.tif", ".tif image file"),
            Arguments.arguments("test.tiff", ".tiff image file"),
            Arguments.arguments("test.jpg", ".jpg image file"),
            Arguments.arguments("test.jpeg", ".jpeg image file"),
            Arguments.arguments("test.png", ".png image file"),
            Arguments.arguments("test.mp3", ".mp3 audio file"),
            Arguments.arguments("test.m4a", ".m4a audio file"),
            Arguments.arguments("test.mp4", ".mp4 audio/video file"),
            Arguments.arguments("test.msg", ".msg message file"),
            Arguments.arguments("test.eml", ".eml message file"),
            Arguments.arguments("test.csv", ".csv file"),
            Arguments.arguments("test.txt", ".txt file"),
            Arguments.arguments("test.rtf", ".rtf file"),
            Arguments.arguments("test.xlsx", ".xlsx spreadsheet"),
            Arguments.arguments("test.xls", ".xls spreadsheet"),
            Arguments.arguments("test.docx", ".docx document"),
            Arguments.arguments("test.doc", ".doc document")
        );
    }
}
