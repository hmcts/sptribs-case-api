package uk.gov.hmcts.sptribs.document.bundling;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.sptribs.cdam.model.UploadResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.document.bundling.model.AudioVideoEvidenceBundleDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.document.service.DocumentsService;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@ExtendWith(MockitoExtension.class)
class AudioVideoEvidenceBundleServiceTest {

    @Mock
    private PDFServiceClient pdfServiceClient;

    @Mock
    private CaseDocumentClientApi caseDocumentClientApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private DocumentsService documentsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Clock clock;

    @Test
    void shouldExtractOnlyAudioVideoRowsFromCaseDocumentsAndSortBySavedDateWithEmptyValues() {
        CaseData caseData = CaseData.builder().build();
        caseData.setCaseNumber("12345");

        AudioVideoEvidenceBundleService service = new AudioVideoEvidenceBundleService(
            pdfServiceClient,
            caseDocumentClientApi,
            documentsService,
            authTokenGenerator,
            request,
            clock
        );

        DocumentEntity audioDoc = DocumentEntity.builder()
            .documentFilename("hearing-audio.MP3")
            .documentBinaryUrl("http://dm/documents/audio/binary")
            .documentTypeName(DocumentType.LINKED_DOCS.name())
            .savedAt(OffsetDateTime.parse("2026-01-10T10:15:30Z"))
            .build();

        DocumentEntity videoDoc = DocumentEntity.builder()
            .documentFilename("hearing-video.mp4")
            .documentBinaryUrl("http://dm/documents/video/binary")
            .documentTypeName(null)
            .savedAt(null)
            .build();

        when(documentsService.getAudioVideoDocuments(12345L))
            .thenReturn(Stream.of(audioDoc, videoDoc));

        List<AudioVideoEvidenceBundleService.AudioVideoDocumentRow> rows = service.extractRows(caseData);

        assertThat(rows).hasSize(2);
        assertThat(rows)
            .extracting(AudioVideoEvidenceBundleService.AudioVideoDocumentRow::documentUrl)
            .containsExactly("http://dm/documents/audio/binary", "http://dm/documents/video/binary");
        assertThat(rows)
            .extracting(AudioVideoEvidenceBundleService.AudioVideoDocumentRow::dateAdded)
            .containsExactly("2026-01-10", "");
        assertThat(rows)
            .extracting(AudioVideoEvidenceBundleService.AudioVideoDocumentRow::documentType)
            .containsExactly("L - Linked docs", "Unknown");
    }

    @Test
    void shouldReturnNullAndSkipPdfUploadWhenNoAudioVideoRows() {
        AudioVideoEvidenceBundleService service = new AudioVideoEvidenceBundleService(
            pdfServiceClient,
            caseDocumentClientApi,
            documentsService,
            authTokenGenerator,
            request,
            clock
        );

        CaseData caseData = CaseData.builder().build();
        caseData.setCaseNumber("12345");

        when(documentsService.getAudioVideoDocuments(12345L))
            .thenReturn(Stream.empty());

        AudioVideoEvidenceBundleDocument result = service.createAudioVideoEvidenceBundleDocument(caseData, 12345L);

        assertThat(result).isNull();
        verifyNoInteractions(pdfServiceClient, caseDocumentClientApi, authTokenGenerator);
    }

    @Test
    void shouldReturnEmptyRowsWhenCaseNumberIsNull() {
        AudioVideoEvidenceBundleService service = new AudioVideoEvidenceBundleService(
            pdfServiceClient,
            caseDocumentClientApi,
            documentsService,
            authTokenGenerator,
            request,
            clock
        );

        CaseData caseData = CaseData.builder().build();

        List<AudioVideoEvidenceBundleService.AudioVideoDocumentRow> rows = service.extractRows(caseData);

        assertThat(rows).isEmpty();
        verifyNoInteractions(documentsService);
    }

    @Test
    void shouldGenerateUploadAndReturnAudioVideoEvidenceBundleDocument() {
        CaseData caseData = CaseData.builder().build();
        caseData.setCaseNumber("12345");

        DocumentEntity audioDoc = DocumentEntity.builder()
            .documentFilename("hearing-audio.mp3")
            .documentBinaryUrl("http://dm/documents/audio/binary?a=1&b=<x>\"'")
            .documentTypeName(DocumentType.LINKED_DOCS.name())
            .savedAt(OffsetDateTime.parse("2026-01-10T12:00:00Z"))
            .build();

        byte[] generatedPdf = "generated-pdf".getBytes(StandardCharsets.UTF_8);
        when(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap())).thenReturn(generatedPdf);
        when(authTokenGenerator.generate()).thenReturn("service-token");
        when(request.getHeader(AUTHORIZATION)).thenReturn("Bearer user-token");
        when(caseDocumentClientApi.uploadDocuments(eq("Bearer user-token"), eq("service-token"), any()))
            .thenReturn(buildUploadResponse("http://dm-store/documents/generated", "http://dm-store/documents/generated/binary"));
        when(documentsService.getAudioVideoDocuments(12345L))
            .thenReturn(Stream.of(audioDoc));

        when(clock.instant()).thenReturn(Instant.parse("2026-08-05T00:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        AudioVideoEvidenceBundleService service = new AudioVideoEvidenceBundleService(
            pdfServiceClient,
            caseDocumentClientApi,
            documentsService,
            authTokenGenerator,
            request,
            clock
        );
        AudioVideoEvidenceBundleDocument result = service.createAudioVideoEvidenceBundleDocument(caseData, 12345L);

        assertThat(result).isNotNull();
        assertThat(result.getDocumentLink()).isNotNull();
        assertThat(result.getDocumentLink().getFilename()).isEqualTo("audio-video-evidence-12345.pdf");
        assertThat(result.getDocumentLink().getUrl()).isEqualTo("http://dm-store/documents/generated");
        assertThat(result.getDocumentLink().getBinaryUrl()).isEqualTo("http://dm-store/documents/generated/binary");
        assertThat(result.getDate()).isEqualTo(LocalDate.of(2026, 8, 5));

        ArgumentCaptor<byte[]> htmlTemplateCaptor = ArgumentCaptor.forClass(byte[].class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> placeholdersCaptor = ArgumentCaptor.forClass(Map.class);
        verify(pdfServiceClient).generateFromHtml(htmlTemplateCaptor.capture(), placeholdersCaptor.capture());
        String htmlTemplate = new String(htmlTemplateCaptor.getValue(), StandardCharsets.UTF_8);
        assertThat(htmlTemplate).contains("Audio/video evidence document - case {{caseId}}");
        assertThat(htmlTemplate).contains("{{rowsHtml | raw}}");

        assertThat(placeholdersCaptor.getValue()).containsEntry("caseId", "12345");
        assertThat(placeholdersCaptor.getValue().get("rowsHtml").toString())
            .contains("<a href=\"http://dm/documents/audio/binary?a=1&amp;b=&lt;x&gt;&quot;&#39;\">hearing-audio.mp3</a>")
            .contains("<td>2026-01-10</td>");
        verify(caseDocumentClientApi).uploadDocuments(eq("Bearer user-token"), eq("service-token"), any());
    }

    @Test
    void shouldThrowWhenUploadResponseIsNull() {
        when(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap())).thenReturn("pdf".getBytes(StandardCharsets.UTF_8));
        when(authTokenGenerator.generate()).thenReturn("service-token");
        when(request.getHeader(AUTHORIZATION)).thenReturn("Bearer user-token");
        when(caseDocumentClientApi.uploadDocuments(eq("Bearer user-token"), eq("service-token"), any())).thenReturn(null);

        AudioVideoEvidenceBundleService service = new AudioVideoEvidenceBundleService(
            pdfServiceClient,
            caseDocumentClientApi,
            documentsService,
            authTokenGenerator,
            request,
            clock
        );
        assertThatThrownBy(() -> service.createAudioVideoEvidenceBundleDocument(caseDataWithAudioVideoDoc(), 111L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Unable to upload audio/video evidence bundle document");
    }

    @Test
    void shouldThrowWhenUploadResponseHasNullDocumentList() {
        UploadResponse uploadResponse = new UploadResponse();
        uploadResponse.setDocuments(null);

        when(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap())).thenReturn("pdf".getBytes(StandardCharsets.UTF_8));
        when(authTokenGenerator.generate()).thenReturn("service-token");
        when(request.getHeader(AUTHORIZATION)).thenReturn("Bearer user-token");
        when(caseDocumentClientApi.uploadDocuments(eq("Bearer user-token"), eq("service-token"), any())).thenReturn(uploadResponse);

        AudioVideoEvidenceBundleService service = new AudioVideoEvidenceBundleService(
            pdfServiceClient,
            caseDocumentClientApi,
            documentsService,
            authTokenGenerator,
            request,
            clock
        );
        assertThatThrownBy(() -> service.createAudioVideoEvidenceBundleDocument(caseDataWithAudioVideoDoc(), 111L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Unable to upload audio/video evidence bundle document");
    }

    @Test
    void shouldThrowWhenUploadResponseHasEmptyDocumentList() {
        UploadResponse uploadResponse = new UploadResponse();
        uploadResponse.setDocuments(List.of());

        when(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap())).thenReturn("pdf".getBytes(StandardCharsets.UTF_8));
        when(authTokenGenerator.generate()).thenReturn("service-token");
        when(request.getHeader(AUTHORIZATION)).thenReturn("Bearer user-token");
        when(caseDocumentClientApi.uploadDocuments(eq("Bearer user-token"), eq("service-token"), any())).thenReturn(uploadResponse);

        AudioVideoEvidenceBundleService service = new AudioVideoEvidenceBundleService(
            pdfServiceClient,
            caseDocumentClientApi,
            documentsService,
            authTokenGenerator,
            request,
            clock
        );
        assertThatThrownBy(() -> service.createAudioVideoEvidenceBundleDocument(caseDataWithAudioVideoDoc(), 111L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Unable to upload audio/video evidence bundle document");
    }

    @Test
    void shouldHandlePrivateHtmlHelpersForEdgeCases() throws Exception {
        AudioVideoEvidenceBundleService service = new AudioVideoEvidenceBundleService(
            pdfServiceClient,
            caseDocumentClientApi,
            documentsService,
            authTokenGenerator,
            request,
            clock
        );

        Method buildRowsHtml = AudioVideoEvidenceBundleService.class.getDeclaredMethod("buildRowsHtml", List.class);
        buildRowsHtml.setAccessible(true);

        String rowsHtml = (String) buildRowsHtml.invoke(service, List.of());
        assertThat(rowsHtml).contains("No active MP3/MP4 documents found.");

        Method escapeHtml = AudioVideoEvidenceBundleService.class.getDeclaredMethod("escapeHtml", String.class);
        escapeHtml.setAccessible(true);

        String escapedNull = (String) escapeHtml.invoke(service, new Object[] {null});
        String escapedValue = (String) escapeHtml.invoke(service, "a&b<c>d\"e'f");
        assertThat(escapedNull).isEmpty();
        assertThat(escapedValue).isEqualTo("a&amp;b&lt;c&gt;d&quot;e&#39;f");
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

    private ListValue<CaseworkerCICDocument> toListValueWithNullDocumentLink(DocumentType type, LocalDate date) {
        CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentCategory(type)
            .documentEmailContent("desc")
            .date(date)
            .documentLink(null)
            .build();
        return ListValue.<CaseworkerCICDocument>builder()
            .id(UUID.randomUUID().toString())
            .value(document)
            .build();
    }

    private CaseData caseDataWithAudioVideoDoc() {
        CaseData caseData = CaseData.builder().build();
        caseData.setCaseNumber("111");
        DocumentEntity mediaDoc = DocumentEntity.builder()
            .documentFilename("media.mp4")
            .documentBinaryUrl("http://dm/documents/media/binary")
            .documentTypeName(DocumentType.LINKED_DOCS.name())
            .savedAt(OffsetDateTime.parse("2026-02-05T10:00:00Z"))
            .build();
        when(documentsService.getAudioVideoDocuments(111L)).thenReturn(Stream.of(mediaDoc));
        return caseData;
    }

    private UploadResponse buildUploadResponse(String selfUrl, String binaryUrl) {
        uk.gov.hmcts.sptribs.cdam.model.Document.DocumentLink self = new uk.gov.hmcts.sptribs.cdam.model.Document.DocumentLink();
        self.href = selfUrl;
        uk.gov.hmcts.sptribs.cdam.model.Document.DocumentLink binary = new uk.gov.hmcts.sptribs.cdam.model.Document.DocumentLink();
        binary.href = binaryUrl;

        uk.gov.hmcts.sptribs.cdam.model.Document.Links links = new uk.gov.hmcts.sptribs.cdam.model.Document.Links();
        links.self = self;
        links.binary = binary;

        uk.gov.hmcts.sptribs.cdam.model.Document uploadedDoc = new uk.gov.hmcts.sptribs.cdam.model.Document();
        uploadedDoc.links = links;

        UploadResponse uploadResponse = new UploadResponse();
        uploadResponse.setDocuments(List.of(uploadedDoc));
        return uploadResponse;
    }
}
