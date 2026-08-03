package uk.gov.hmcts.sptribs.document.bundling;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.cdam.model.UploadResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.document.service.DocumentsService;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
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

    @Test
    void shouldExtractOnlyAudioVideoRowsFromCaseDocumentsAndSortBySavedDateWithEmptyValues() {
        List<ListValue<CaseworkerCICDocument>> docs = new ArrayList<>();
        docs.add(ListValue.<CaseworkerCICDocument>builder().id(UUID.randomUUID().toString()).value(null).build());
        docs.add(toListValue("notes.pdf", "http://dm/documents/notes/binary", DocumentType.APPLICATION_FORM, LocalDate.of(2026, 1, 8)));
        docs.add(toListValue("hearing-audio.MP3", "http://dm/documents/audio/binary", DocumentType.LINKED_DOCS, LocalDate.of(2026, 1, 10)));
        docs.add(toListValue("bad-video.mp4", "", DocumentType.POLICE_EVIDENCE, LocalDate.of(2026, 1, 14)));
        docs.add(toListValueWithNullDocumentLink(DocumentType.POLICE_EVIDENCE, LocalDate.of(2026, 1, 13)));
        docs.add(toListValue("", "http://dm/documents/no-name/binary", DocumentType.POLICE_EVIDENCE, LocalDate.of(2026, 1, 12)));
        docs.add(toListValue("no-link.mp4", null, DocumentType.POLICE_EVIDENCE, LocalDate.of(2026, 1, 12)));
        docs.add(toListValue("unsupported.wav", "http://dm/documents/wav/binary", DocumentType.LINKED_DOCS, LocalDate.of(2026, 1, 11)));
        docs.add(toListValue("hearing-video.mp4", "http://dm/documents/video/binary", null, null));


        CaseData caseData = CaseData.builder().build();
        caseData.setCaseNumber("12345");
        CicCase cicCase = CicCase.builder().build();

        cicCase.setApplicantDocumentsUploaded(docs);
        caseData.setCicCase(cicCase);

        AudioVideoEvidenceBundleService service = new AudioVideoEvidenceBundleService(
            pdfServiceClient,
            caseDocumentClientApi,
            documentsService,
            authTokenGenerator,
            request
        );

        when(documentsService.getCaseDocumentsByBinaryUrls(eq(12345L), any()))
            .thenReturn(Map.of(
                "http://dm/documents/audio/binary",
                DocumentEntity.builder().savedAt(OffsetDateTime.parse("2026-01-10T10:15:30Z")).build()
            ));

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
            request
        );

        CaseData caseData = CaseData.builder().build();
        CicCase cicCase = CicCase.builder().build();
        cicCase.setApplicantDocumentsUploaded(List.of(
            toListValue("notes.pdf", "http://dm/documents/notes/binary", DocumentType.APPLICATION_FORM, LocalDate.of(2026, 1, 8))
        ));
        caseData.setCicCase(cicCase);

        Document result = service.createAudioVideoEvidenceBundleDocument(caseData, 12345L);

        assertThat(result).isNull();
        verifyNoInteractions(pdfServiceClient, caseDocumentClientApi, authTokenGenerator);
    }

    @Test
    void shouldSkipNullListValueAndNullDocumentValue() {
        AudioVideoEvidenceBundleService service = new AudioVideoEvidenceBundleService(
            pdfServiceClient,
            caseDocumentClientApi,
            documentsService,
            authTokenGenerator,
            request
        );

        List<ListValue<CaseworkerCICDocument>> documents = new ArrayList<>();
        documents.add(null);
        documents.add(ListValue.<CaseworkerCICDocument>builder().id(UUID.randomUUID().toString()).value(null).build());
        documents.add(toListValue("valid-media.mp4", "http://dm/documents/media/binary", DocumentType.LINKED_DOCS, LocalDate.of(2026, 1, 15)));

        try (MockedStatic<DocumentListUtil> documentListUtil = mockStatic(DocumentListUtil.class)) {
            documentListUtil.when(() -> DocumentListUtil.getAllCaseDocuments(any(CaseData.class))).thenReturn(documents);

            List<AudioVideoEvidenceBundleService.AudioVideoDocumentRow> rows = service.extractRows(CaseData.builder().build());

            assertThat(rows).hasSize(1);
            assertThat(rows.getFirst().documentUrl()).isEqualTo("http://dm/documents/media/binary");
        }
    }

    @Test
    void shouldGenerateUploadAndReturnAudioVideoEvidenceBundleDocument() {
        CaseData caseData = CaseData.builder().build();
        caseData.setCaseNumber("12345");
        CicCase cicCase = CicCase.builder().build();
        cicCase.setApplicantDocumentsUploaded(List.of(
            toListValue(
                "hearing-audio.mp3",
                "http://dm/documents/audio/binary?a=1&b=<x>\"'",
                DocumentType.LINKED_DOCS,
                LocalDate.of(2026, 1, 10)
            )
        ));
        caseData.setCicCase(cicCase);

        byte[] generatedPdf = "generated-pdf".getBytes(StandardCharsets.UTF_8);
        when(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap())).thenReturn(generatedPdf);
        when(authTokenGenerator.generate()).thenReturn("service-token");
        when(request.getHeader(AUTHORIZATION)).thenReturn("Bearer user-token");
        when(caseDocumentClientApi.uploadDocuments(eq("Bearer user-token"), eq("service-token"), any()))
            .thenReturn(buildUploadResponse("http://dm-store/documents/generated", "http://dm-store/documents/generated/binary"));
        when(documentsService.getCaseDocumentsByBinaryUrls(eq(12345L), any()))
            .thenReturn(Map.of(
                "http://dm/documents/audio/binary?a=1&b=<x>\"'",
                DocumentEntity.builder().savedAt(OffsetDateTime.parse("2026-01-10T12:00:00Z")).build()
            ));

        AudioVideoEvidenceBundleService service = new AudioVideoEvidenceBundleService(
            pdfServiceClient,
            caseDocumentClientApi,
            documentsService,
            authTokenGenerator,
            request
        );
        Document result = service.createAudioVideoEvidenceBundleDocument(caseData, 12345L);

        assertThat(result).isNotNull();
        assertThat(result.getFilename()).isEqualTo("audio-video-evidence-12345.pdf");
        assertThat(result.getUrl()).isEqualTo("http://dm-store/documents/generated");
        assertThat(result.getBinaryUrl()).isEqualTo("http://dm-store/documents/generated/binary");

        ArgumentCaptor<byte[]> htmlCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(pdfServiceClient).generateFromHtml(htmlCaptor.capture(), eq(Collections.emptyMap()));
        String html = new String(htmlCaptor.getValue(), StandardCharsets.UTF_8);
        assertThat(html).contains("Audio/video evidence document - case 12345");
        assertThat(html).contains("<th>Date approved</th>");
        assertThat(html).contains("<th>Uploaded by</th>");
        assertThat(html).contains("<a href=\"http://dm/documents/audio/binary?a=1&amp;b=&lt;x&gt;&quot;&#39;\">hearing-audio.mp3</a>");
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
            request
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
            request
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
            request
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
            request
        );

        Method buildHtml = AudioVideoEvidenceBundleService.class.getDeclaredMethod("buildHtml", List.class, Long.class);
        buildHtml.setAccessible(true);

        String html = (String) buildHtml.invoke(service, List.of(), 42L);
        assertThat(html).contains("No active MP3/MP4 documents found.");

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
        CicCase cicCase = CicCase.builder().build();
        cicCase.setApplicantDocumentsUploaded(List.of(
            toListValue("media.mp4", "http://dm/documents/media/binary", DocumentType.LINKED_DOCS, LocalDate.of(2026, 2, 5))
        ));
        caseData.setCicCase(cicCase);
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
