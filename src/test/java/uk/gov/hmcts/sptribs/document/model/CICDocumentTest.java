package uk.gov.hmcts.sptribs.document.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import uk.gov.hmcts.ccd.sdk.type.Document;

import static org.assertj.core.api.Assertions.assertThat;

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
                    "test.m4a"
    })
    void shouldCheckDocumentIsValid(String filename) {
        CICDocument document = CICDocument.builder()
            .documentEmailContent("Dear sir/madam, here is an email.")
            .documentLink(Document.builder().filename(filename).build())
            .build();

        boolean result = document.isDocumentValid();

        assertThat(result).isTrue();
    }

    @Test
    void shouldCheckDocxDocumentIsInvalid() {
        CICDocument document = CICDocument.builder()
            .documentEmailContent("Dear sir/madam, here is an email.")
            .documentLink(Document.builder().filename("test.docx").build())
            .build();

        boolean result = document.isDocumentValid();

        assertThat(result).isFalse();
    }

}
