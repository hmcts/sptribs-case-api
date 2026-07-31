package uk.gov.hmcts.sptribs.document.bundling;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AudioVideoEvidenceBundleServiceTest {

    @Mock
    private PDFServiceClient pdfServiceClient;

    @Mock
    private CaseDocumentClientApi caseDocumentClientApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private HttpServletRequest request;

    @Test
    void shouldExtractOnlyAudioVideoRowsFromActiveCaseDocuments() {
        AudioVideoEvidenceBundleService service = new AudioVideoEvidenceBundleService(
            pdfServiceClient,
            caseDocumentClientApi,
            authTokenGenerator,
            request
        );

        CaseData caseData = CaseData.builder().build();
        CicCase cicCase = CicCase.builder().build();
        cicCase.setApplicantDocumentsUploaded(List.of(
            toListValue("notes.pdf", "http://dm/documents/notes/binary", DocumentType.APPLICATION_FORM, LocalDate.of(2026, 1, 8)),
            toListValue("hearing-audio.mp3", "http://dm/documents/audio/binary", DocumentType.LINKED_DOCS, LocalDate.of(2026, 1, 10)),
            toListValue("hearing-video.mp4", "http://dm/documents/video/binary", DocumentType.POLICE_EVIDENCE, LocalDate.of(2026, 1, 12)),
            toListValue("bad-video.mp4", "", DocumentType.POLICE_EVIDENCE, LocalDate.of(2026, 1, 14))
        ));
        caseData.setCicCase(cicCase);

        List<AudioVideoEvidenceBundleService.AudioVideoDocumentRow> rows = service.extractRows(caseData);

        assertThat(rows).hasSize(2);
        assertThat(rows)
            .extracting(AudioVideoEvidenceBundleService.AudioVideoDocumentRow::documentUrl)
            .containsExactly("http://dm/documents/audio/binary", "http://dm/documents/video/binary");
        assertThat(rows)
            .extracting(AudioVideoEvidenceBundleService.AudioVideoDocumentRow::dateAdded)
            .containsExactly("2026-01-10", "2026-01-12");
    }

    private ListValue<CaseworkerCICDocument> toListValue(String filename,
                                                         String binaryUrl,
                                                         DocumentType type,
                                                         LocalDate date) {
        CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentCategory(type)
            .documentEmailContent("desc")
            .date(date)
            .documentLink(Document.builder().filename(filename).binaryUrl(binaryUrl).url(binaryUrl).build())
            .build();
        return ListValue.<CaseworkerCICDocument>builder()
            .id(UUID.randomUUID().toString())
            .value(document)
            .build();
    }
}
