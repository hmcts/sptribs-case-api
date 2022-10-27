package uk.gov.hmcts.sptribs.caseworker.service.task.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

class FileNameUtilTest {

    @Test
    void shouldFormatDocumentName() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse("2022-03-21:10:00", formatter);

        String formattedDocName = FileNameUtil.formatDocumentName(Long.valueOf(1234L), "docName", dateTime);
        assertThat(formattedDocName).isEqualTo("docName-1234-2022-03-21:10:00");

        String formattedDocNameWithoutCaseId = FileNameUtil.formatDocumentName("docName", dateTime);
        assertThat(formattedDocNameWithoutCaseId).isEqualTo("docName-2022-03-21:10:00");
    }
}
