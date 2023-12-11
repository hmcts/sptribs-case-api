package uk.gov.hmcts.sptribs.document.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.Document;

import static org.assertj.core.api.Assertions.assertThat;

public class CICDocumentTest {

    @Test
    public void shouldCheckPdfDocumentIsValid() {
        CICDocument document = CICDocument.builder()
            .documentEmailContent("Dear sir/madam, here is an email.")
            .documentLink(Document.builder().filename("test.pdf").build())
            .build();

        boolean result = document.isDocumentValid();

        assertThat(result).isTrue();
    }

    @Test
    public void shouldCheckTifDocumentIsValid() {
        CICDocument document = CICDocument.builder()
            .documentEmailContent("Dear sir/madam, here is an email.")
            .documentLink(Document.builder().filename("test.tif").build())
            .build();

        boolean result = document.isDocumentValid();

        assertThat(result).isTrue();
    }

    @Test
    public void shouldCheckTiffDocumentIsValid() {
        CICDocument document = CICDocument.builder()
            .documentEmailContent("Dear sir/madam, here is an email.")
            .documentLink(Document.builder().filename("test.tiff").build())
            .build();

        boolean result = document.isDocumentValid();

        assertThat(result).isTrue();
    }

    @Test
    public void shouldCheckJpgDocumentIsValid() {
        CICDocument document = CICDocument.builder()
            .documentEmailContent("Dear sir/madam, here is an email.")
            .documentLink(Document.builder().filename("test.jpg").build())
            .build();

        boolean result = document.isDocumentValid();

        assertThat(result).isTrue();
    }

    @Test
    public void shouldCheckJpegDocumentIsValid() {
        CICDocument document = CICDocument.builder()
            .documentEmailContent("Dear sir/madam, here is an email.")
            .documentLink(Document.builder().filename("test.jpeg").build())
            .build();

        boolean result = document.isDocumentValid();

        assertThat(result).isTrue();
    }

    @Test
    public void shouldCheckPngDocumentIsValid() {
        CICDocument document = CICDocument.builder()
            .documentEmailContent("Dear sir/madam, here is an email.")
            .documentLink(Document.builder().filename("test.png").build())
            .build();

        boolean result = document.isDocumentValid();

        assertThat(result).isTrue();
    }

    @Test
    public void shouldCheckMp3DocumentIsValid() {
        CICDocument document = CICDocument.builder()
            .documentEmailContent("Dear sir/madam, here is an email.")
            .documentLink(Document.builder().filename("test.mp3").build())
            .build();

        boolean result = document.isDocumentValid();

        assertThat(result).isTrue();
    }

    @Test
    public void shouldCheckM4aDocumentIsValid() {
        CICDocument document = CICDocument.builder()
            .documentEmailContent("Dear sir/madam, here is an email.")
            .documentLink(Document.builder().filename("test.m4a").build())
            .build();

        boolean result = document.isDocumentValid();

        assertThat(result).isTrue();
    }

    @Test
    public void shouldCheckCsvDocumentIsValid() {
        CICDocument document = CICDocument.builder()
            .documentEmailContent("Dear sir/madam, here is an email.")
            .documentLink(Document.builder().filename("test.csv").build())
            .build();

        boolean result = document.isDocumentValid();

        assertThat(result).isTrue();
    }

    @Test
    public void shouldCheckTxtDocumentIsValid() {
        CICDocument document = CICDocument.builder()
            .documentEmailContent("Dear sir/madam, here is an email.")
            .documentLink(Document.builder().filename("test.txt").build())
            .build();

        boolean result = document.isDocumentValid();

        assertThat(result).isTrue();
    }

    @Test
    public void shouldCheckRtfDocumentIsValid() {
        CICDocument document = CICDocument.builder()
            .documentEmailContent("Dear sir/madam, here is an email.")
            .documentLink(Document.builder().filename("test.rtf").build())
            .build();

        boolean result = document.isDocumentValid();

        assertThat(result).isTrue();
    }

    @Test
    public void shouldCheckXlsxDocumentIsValid() {
        CICDocument document = CICDocument.builder()
            .documentEmailContent("Dear sir/madam, here is an email.")
            .documentLink(Document.builder().filename("test.txt").build())
            .build();

        boolean result = document.isDocumentValid();

        assertThat(result).isTrue();
    }

    @Test
    public void shouldCheckXlsDocumentIsValid() {
        CICDocument document = CICDocument.builder()
            .documentEmailContent("Dear sir/madam, here is an email.")
            .documentLink(Document.builder().filename("test.xls").build())
            .build();

        boolean result = document.isDocumentValid();

        assertThat(result).isTrue();
    }

    @Test
    public void shouldCheckDocxDocumentIsValid() {
        CICDocument document = CICDocument.builder()
            .documentEmailContent("Dear sir/madam, here is an email.")
            .documentLink(Document.builder().filename("test.docx").build())
            .build();

        boolean result = document.isDocumentValid();

        assertThat(result).isTrue();
    }

    @Test
    public void shouldCheckDocDocumentIsValid() {
        CICDocument document = CICDocument.builder()
            .documentEmailContent("Dear sir/madam, here is an email.")
            .documentLink(Document.builder().filename("test.doc").build())
            .build();

        boolean result = document.isDocumentValid();

        assertThat(result).isTrue();
    }

    @Test
    public void shouldCheckSqlDocumentIsInvalid() {
        CICDocument document = CICDocument.builder()
            .documentEmailContent("Dear sir/madam, here is an email.")
            .documentLink(Document.builder().filename("test.sql").build())
            .build();

        boolean result = document.isDocumentValid();

        assertThat(result).isFalse();
    }
}
