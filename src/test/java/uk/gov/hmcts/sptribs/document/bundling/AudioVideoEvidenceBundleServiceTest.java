package uk.gov.hmcts.sptribs.document.bundling;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.sptribs.cdam.model.UploadResponse;
import uk.gov.hmcts.sptribs.document.bundling.model.AudioVideoEvidenceBundleDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.document.service.DocumentsService;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
    private ManageCaseDocumentUrlBuilder manageCaseDocumentUrlBuilder;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Clock clock;

    private AudioVideoEvidenceBundleService service;

    @BeforeEach
    void setUp() {
        service = service();
    }

    @Test
    void shouldGenerateForAudioOnly() {
        DocumentEntity audioDoc = documentEntity(
            "hearing-audio.MP3",
            "http://dm/documents/11111111-1111-1111-1111-111111111111/binary",
            DocumentType.LINKED_DOCS.name(),
            "2026-01-10T12:00:00Z"
        );

        when(documentsService.getAudioVideoDocuments(12345L)).thenReturn(List.of(audioDoc));
        when(manageCaseDocumentUrlBuilder.buildPublicBinaryUrl(audioDoc.getDocumentBinaryUrl()))
            .thenReturn("https://manage-case.demo.platform.hmcts.net/documents/11111111-1111-1111-1111-111111111111/binary");
        when(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .thenReturn("generated-pdf".getBytes(StandardCharsets.UTF_8));
        when(authTokenGenerator.generate()).thenReturn("service-token");
        when(request.getHeader("Authorization")).thenReturn("Bearer user-token");
        when(caseDocumentClientApi.uploadDocuments(eq("Bearer user-token"), eq("service-token"), any()))
            .thenReturn(uploadResponse(
                "audio-video-evidence-12345.pdf",
                "http://dm-store/documents/generated",
                "http://dm-store/documents/generated/binary"
            ));
        when(clock.instant()).thenReturn(Instant.parse("2026-08-05T00:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        Optional<AudioVideoEvidenceBundleDocument> result = service.createAudioVideoEvidenceBundleDocument(12345L);

        assertThat(result).isPresent();
        assertThat(result.get().getDate()).isEqualTo(LocalDate.of(2026, 8, 5));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> placeholdersCaptor = ArgumentCaptor.forClass(Map.class);
        verify(pdfServiceClient).generateFromHtml(any(byte[].class), placeholdersCaptor.capture());
        assertThat(placeholdersCaptor.getValue().get("rowsHtml").toString())
            .contains("Audio Document")
            .contains("hearing-audio.MP3")
            .contains("https://manage-case.demo.platform.hmcts.net/documents/11111111-1111-1111-1111-111111111111/binary");
        verify(manageCaseDocumentUrlBuilder).buildPublicBinaryUrl(audioDoc.getDocumentBinaryUrl());
    }

    @Test
    void shouldGenerateForVideoOnly() {
        DocumentEntity videoDoc = documentEntity(
            "hearing-video.mp4",
            "http://dm/documents/22222222-2222-2222-2222-222222222222/binary",
            null,
            null
        );

        when(documentsService.getAudioVideoDocuments(12345L)).thenReturn(List.of(videoDoc));
        when(manageCaseDocumentUrlBuilder.buildPublicBinaryUrl(videoDoc.getDocumentBinaryUrl()))
            .thenReturn("https://manage-case.demo.platform.hmcts.net/documents/22222222-2222-2222-2222-222222222222/binary");
        when(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .thenReturn("generated-pdf".getBytes(StandardCharsets.UTF_8));
        when(authTokenGenerator.generate()).thenReturn("service-token");
        when(request.getHeader("Authorization")).thenReturn("Bearer user-token");
        when(caseDocumentClientApi.uploadDocuments(eq("Bearer user-token"), eq("service-token"), any()))
            .thenReturn(uploadResponse(
                "audio-video-evidence-12345.pdf",
                "http://dm-store/documents/generated",
                "http://dm-store/documents/generated/binary"
            ));
        when(clock.instant()).thenReturn(Instant.parse("2026-08-05T00:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        Optional<AudioVideoEvidenceBundleDocument> result = service.createAudioVideoEvidenceBundleDocument(12345L);

        assertThat(result).isPresent();
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> placeholdersCaptor = ArgumentCaptor.forClass(Map.class);
        verify(pdfServiceClient).generateFromHtml(any(byte[].class), placeholdersCaptor.capture());
        assertThat(placeholdersCaptor.getValue().get("rowsHtml").toString())
            .contains("Video Document")
            .contains("hearing-video.mp4")
            .contains("Unknown");
    }

    @Test
    void shouldGenerateForMixedRowsInSavedDateOrder() {
        DocumentEntity audioDoc = documentEntity(
            "audio.mp3",
            "http://dm/documents/33333333-3333-3333-3333-333333333333/binary",
            DocumentType.LINKED_DOCS.name(),
            "2026-01-10T10:00:00Z"
        );
        DocumentEntity videoDoc = documentEntity(
            "video.mp4",
            "http://dm/documents/44444444-4444-4444-4444-444444444444/binary",
            DocumentType.POLICE_EVIDENCE.name(),
            "2026-01-11T10:00:00Z"
        );

        when(documentsService.getAudioVideoDocuments(12345L)).thenReturn(List.of(audioDoc, videoDoc));
        when(manageCaseDocumentUrlBuilder.buildPublicBinaryUrl(audioDoc.getDocumentBinaryUrl()))
            .thenReturn("https://manage-case.demo.platform.hmcts.net/documents/33333333-3333-3333-3333-333333333333/binary");
        when(manageCaseDocumentUrlBuilder.buildPublicBinaryUrl(videoDoc.getDocumentBinaryUrl()))
            .thenReturn("https://manage-case.demo.platform.hmcts.net/documents/44444444-4444-4444-4444-444444444444/binary");
        when(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .thenReturn("generated-pdf".getBytes(StandardCharsets.UTF_8));
        when(authTokenGenerator.generate()).thenReturn("service-token");
        when(request.getHeader("Authorization")).thenReturn("Bearer user-token");
        when(caseDocumentClientApi.uploadDocuments(eq("Bearer user-token"), eq("service-token"), any()))
            .thenReturn(uploadResponse(
                "audio-video-evidence-12345.pdf",
                "http://dm-store/documents/generated",
                "http://dm-store/documents/generated/binary"
            ));
        when(clock.instant()).thenReturn(Instant.parse("2026-08-05T00:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        Optional<AudioVideoEvidenceBundleDocument> result = service.createAudioVideoEvidenceBundleDocument(12345L);

        assertThat(result).isPresent();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> placeholdersCaptor = ArgumentCaptor.forClass(Map.class);
        verify(pdfServiceClient).generateFromHtml(any(byte[].class), placeholdersCaptor.capture());
        String rowsHtml = placeholdersCaptor.getValue().get("rowsHtml").toString();
        assertThat(rowsHtml.indexOf("audio.mp3")).isLessThan(rowsHtml.indexOf("video.mp4"));
    }

    @Test
    void shouldWrapLookupFailureInCustomException() {
        when(documentsService.getAudioVideoDocuments(12345L)).thenThrow(new RuntimeException("db down"));

        assertThatThrownBy(() -> service.createAudioVideoEvidenceBundleDocument(12345L))
            .isInstanceOf(AudioVideoEvidenceBundleException.class)
            .hasMessage("Unable to create audio/video evidence document for case 12345")
            .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldWrapInvalidStoredDocumentUrlInCustomException() {
        DocumentEntity audioDoc = documentEntity(
            "audio.mp3",
            "http://dm/documents/not-a-uuid/binary",
            DocumentType.LINKED_DOCS.name(),
            "2026-01-10T10:00:00Z"
        );
        when(documentsService.getAudioVideoDocuments(12345L)).thenReturn(List.of(audioDoc));
        when(manageCaseDocumentUrlBuilder.buildPublicBinaryUrl(audioDoc.getDocumentBinaryUrl()))
            .thenThrow(new IllegalArgumentException("Invalid document identifier"));

        assertThatThrownBy(() -> service.createAudioVideoEvidenceBundleDocument(12345L))
            .isInstanceOf(AudioVideoEvidenceBundleException.class)
            .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldWrapPdfGenerationFailureInCustomException() {
        DocumentEntity audioDoc = documentEntity(
            "audio.mp3",
            "http://dm/documents/33333333-3333-3333-3333-333333333333/binary",
            DocumentType.LINKED_DOCS.name(),
            "2026-01-10T10:00:00Z"
        );
        when(documentsService.getAudioVideoDocuments(12345L)).thenReturn(List.of(audioDoc));
        when(manageCaseDocumentUrlBuilder.buildPublicBinaryUrl(audioDoc.getDocumentBinaryUrl()))
            .thenReturn("https://manage-case.demo.platform.hmcts.net/documents/33333333-3333-3333-3333-333333333333/binary");
        when(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap())).thenThrow(new RuntimeException("pdf fail"));

        assertThatThrownBy(() -> service.createAudioVideoEvidenceBundleDocument(12345L))
            .isInstanceOf(AudioVideoEvidenceBundleException.class)
            .hasCauseInstanceOf(RuntimeException.class);
        verify(caseDocumentClientApi, never()).uploadDocuments(any(), any(), any());
    }

    @ParameterizedTest
    @MethodSource("invalidUploadResponses")
    void shouldWrapUploadFailureForInvalidCdamResponse(UploadResponse invalidResponse) {
        setupSingleAudioGeneration();
        when(caseDocumentClientApi.uploadDocuments(eq("Bearer user-token"), eq("service-token"), any())).thenReturn(invalidResponse);

        assertThatThrownBy(() -> service.createAudioVideoEvidenceBundleDocument(12345L))
            .isInstanceOf(AudioVideoEvidenceBundleException.class)
            .hasCauseInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldWrapUploadFailureWhenResponseIsNull() {
        setupSingleAudioGeneration();
        when(caseDocumentClientApi.uploadDocuments(eq("Bearer user-token"), eq("service-token"), any())).thenReturn(null);

        assertThatThrownBy(() -> service.createAudioVideoEvidenceBundleDocument(12345L))
            .isInstanceOf(AudioVideoEvidenceBundleException.class)
            .hasCauseInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldFailWhenPdfServiceReturnsEmptyDocument() {
        DocumentEntity audioDoc = documentEntity(
            "audio.mp3",
            "http://dm/documents/33333333-3333-3333-3333-333333333333/binary",
            DocumentType.LINKED_DOCS.name(),
            "2026-01-10T10:00:00Z"
        );
        when(documentsService.getAudioVideoDocuments(12345L)).thenReturn(List.of(audioDoc));
        when(manageCaseDocumentUrlBuilder.buildPublicBinaryUrl(audioDoc.getDocumentBinaryUrl()))
            .thenReturn("https://manage-case.demo.platform.hmcts.net/documents/33333333-3333-3333-3333-333333333333/binary");
        when(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap())).thenReturn(new byte[0]);

        assertThatThrownBy(() -> service.createAudioVideoEvidenceBundleDocument(12345L))
            .isInstanceOf(AudioVideoEvidenceBundleException.class)
            .hasCauseInstanceOf(IllegalStateException.class);
        verify(caseDocumentClientApi, never()).uploadDocuments(any(), any(), any());
    }

    @Test
    void shouldEscapeGeneratedPublicUrlAndNotContainRawInternalUrlInHtml() {
        DocumentEntity audioDoc = documentEntity(
            "audio.mp3",
            "http://dm/documents/33333333-3333-3333-3333-333333333333/binary?a=1",
            DocumentType.LINKED_DOCS.name(),
            "2026-01-10T10:00:00Z"
        );
        when(documentsService.getAudioVideoDocuments(12345L)).thenReturn(List.of(audioDoc));
        when(manageCaseDocumentUrlBuilder.buildPublicBinaryUrl(audioDoc.getDocumentBinaryUrl()))
            .thenReturn("https://manage-case.demo.platform.hmcts.net/documents/33333333-3333-3333-3333-333333333333/binary?x=<x>");
        when(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .thenReturn("generated-pdf".getBytes(StandardCharsets.UTF_8));
        when(authTokenGenerator.generate()).thenReturn("service-token");
        when(request.getHeader("Authorization")).thenReturn("Bearer user-token");
        when(caseDocumentClientApi.uploadDocuments(eq("Bearer user-token"), eq("service-token"), any()))
            .thenReturn(uploadResponse(
                "audio-video-evidence-12345.pdf",
                "http://dm-store/documents/generated",
                "http://dm-store/documents/generated/binary"
            ));
        when(clock.instant()).thenReturn(Instant.parse("2026-08-05T00:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        Optional<AudioVideoEvidenceBundleDocument> result = service.createAudioVideoEvidenceBundleDocument(12345L);

        assertThat(result).isPresent();
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> placeholdersCaptor = ArgumentCaptor.forClass(Map.class);
        verify(pdfServiceClient).generateFromHtml(any(byte[].class), placeholdersCaptor.capture());
        String rowsHtml = placeholdersCaptor.getValue().get("rowsHtml").toString();
        assertThat(rowsHtml).contains("https://manage-case.demo.platform.hmcts.net/documents/33333333-3333-3333-3333-333333333333/binary?x=&lt;x&gt;");
        assertThat(rowsHtml).doesNotContain("http://dm/documents/33333333-3333-3333-3333-333333333333/binary?a=1");
    }

    @Test
    void shouldNotGeneratePdfOrUploadWhenNoMedia() {
        when(documentsService.getAudioVideoDocuments(22222L)).thenReturn(List.of());

        Optional<AudioVideoEvidenceBundleDocument> result = service.createAudioVideoEvidenceBundleDocument(22222L);

        assertThat(result).isEmpty();
        verifyNoInteractions(pdfServiceClient, caseDocumentClientApi, authTokenGenerator);
        verifyNoInteractions(manageCaseDocumentUrlBuilder);
        verifyNoMoreInteractions(documentsService);
    }

    private AudioVideoEvidenceBundleService service() {
        return new AudioVideoEvidenceBundleService(
            pdfServiceClient,
            caseDocumentClientApi,
            documentsService,
            manageCaseDocumentUrlBuilder,
            authTokenGenerator,
            request,
            clock
        );
    }

    private DocumentEntity documentEntity(String fileName, String binaryUrl, String typeName, String savedAt) {
        return DocumentEntity.builder()
            .documentFilename(fileName)
            .documentBinaryUrl(binaryUrl)
            .documentTypeName(typeName)
            .savedAt(savedAt == null ? null : OffsetDateTime.parse(savedAt))
            .build();
    }

    private UploadResponse uploadResponse(String originalFilename, String selfUrl, String binaryUrl) {
        uk.gov.hmcts.sptribs.cdam.model.Document.DocumentLink self = new uk.gov.hmcts.sptribs.cdam.model.Document.DocumentLink();
        self.href = selfUrl;
        uk.gov.hmcts.sptribs.cdam.model.Document.DocumentLink binary = new uk.gov.hmcts.sptribs.cdam.model.Document.DocumentLink();
        binary.href = binaryUrl;

        uk.gov.hmcts.sptribs.cdam.model.Document.Links links = new uk.gov.hmcts.sptribs.cdam.model.Document.Links();
        links.self = self;
        links.binary = binary;

        uk.gov.hmcts.sptribs.cdam.model.Document uploaded = new uk.gov.hmcts.sptribs.cdam.model.Document();
        uploaded.originalDocumentName = originalFilename;
        uploaded.links = links;

        UploadResponse response = new UploadResponse();
        response.setDocuments(List.of(uploaded));
        return response;
    }

    private void setupSingleAudioGeneration() {
        DocumentEntity audioDoc = documentEntity(
            "audio.mp3",
            "http://dm/documents/55555555-5555-5555-5555-555555555555/binary",
            DocumentType.LINKED_DOCS.name(),
            "2026-01-10T10:00:00Z"
        );

        when(documentsService.getAudioVideoDocuments(12345L)).thenReturn(List.of(audioDoc));
        when(manageCaseDocumentUrlBuilder.buildPublicBinaryUrl(audioDoc.getDocumentBinaryUrl()))
            .thenReturn("https://manage-case.demo.platform.hmcts.net/documents/55555555-5555-5555-5555-555555555555/binary");
        when(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap())).thenReturn("pdf".getBytes(StandardCharsets.UTF_8));
        when(authTokenGenerator.generate()).thenReturn("service-token");
        when(request.getHeader("Authorization")).thenReturn("Bearer user-token");
    }

    private static Stream<Arguments> invalidUploadResponses() {
        UploadResponse nullDocumentList = new UploadResponse();
        nullDocumentList.setDocuments(null);

        UploadResponse emptyDocumentList = new UploadResponse();
        emptyDocumentList.setDocuments(List.of());

        UploadResponse nullFirstDocument = new UploadResponse();
        List<uk.gov.hmcts.sptribs.cdam.model.Document> documentsWithNull = new java.util.ArrayList<>();
        documentsWithNull.add(null);
        nullFirstDocument.setDocuments(documentsWithNull);

        uk.gov.hmcts.sptribs.cdam.model.Document documentWithoutLinks = new uk.gov.hmcts.sptribs.cdam.model.Document();
        UploadResponse responseWithoutLinks = new UploadResponse();
        responseWithoutLinks.setDocuments(List.of(documentWithoutLinks));

        uk.gov.hmcts.sptribs.cdam.model.Document documentWithNullSelf = new uk.gov.hmcts.sptribs.cdam.model.Document();
        uk.gov.hmcts.sptribs.cdam.model.Document.Links nullSelfLinks = new uk.gov.hmcts.sptribs.cdam.model.Document.Links();
        nullSelfLinks.binary = new uk.gov.hmcts.sptribs.cdam.model.Document.DocumentLink();
        nullSelfLinks.binary.href = "http://dm-store/documents/generated/binary";
        documentWithNullSelf.links = nullSelfLinks;
        UploadResponse responseWithNullSelf = new UploadResponse();
        responseWithNullSelf.setDocuments(List.of(documentWithNullSelf));

        uk.gov.hmcts.sptribs.cdam.model.Document documentWithNullBinary = new uk.gov.hmcts.sptribs.cdam.model.Document();
        uk.gov.hmcts.sptribs.cdam.model.Document.Links nullBinaryLinks = new uk.gov.hmcts.sptribs.cdam.model.Document.Links();
        nullBinaryLinks.self = new uk.gov.hmcts.sptribs.cdam.model.Document.DocumentLink();
        nullBinaryLinks.self.href = "http://dm-store/documents/generated";
        documentWithNullBinary.links = nullBinaryLinks;
        UploadResponse responseWithNullBinary = new UploadResponse();
        responseWithNullBinary.setDocuments(List.of(documentWithNullBinary));

        uk.gov.hmcts.sptribs.cdam.model.Document.Links blankSelfLinks = new uk.gov.hmcts.sptribs.cdam.model.Document.Links();
        blankSelfLinks.self = new uk.gov.hmcts.sptribs.cdam.model.Document.DocumentLink();
        blankSelfLinks.self.href = " ";
        blankSelfLinks.binary = new uk.gov.hmcts.sptribs.cdam.model.Document.DocumentLink();
        blankSelfLinks.binary.href = "http://dm-store/documents/generated/binary";
        uk.gov.hmcts.sptribs.cdam.model.Document documentWithBlankSelf = new uk.gov.hmcts.sptribs.cdam.model.Document();
        documentWithBlankSelf.links = blankSelfLinks;
        UploadResponse responseWithBlankSelf = new UploadResponse();
        responseWithBlankSelf.setDocuments(List.of(documentWithBlankSelf));

        uk.gov.hmcts.sptribs.cdam.model.Document.Links blankBinaryLinks = new uk.gov.hmcts.sptribs.cdam.model.Document.Links();
        blankBinaryLinks.self = new uk.gov.hmcts.sptribs.cdam.model.Document.DocumentLink();
        blankBinaryLinks.self.href = "http://dm-store/documents/generated";
        blankBinaryLinks.binary = new uk.gov.hmcts.sptribs.cdam.model.Document.DocumentLink();
        blankBinaryLinks.binary.href = "";
        uk.gov.hmcts.sptribs.cdam.model.Document documentWithBlankBinary = new uk.gov.hmcts.sptribs.cdam.model.Document();
        documentWithBlankBinary.links = blankBinaryLinks;
        UploadResponse responseWithBlankBinary = new UploadResponse();
        responseWithBlankBinary.setDocuments(List.of(documentWithBlankBinary));

        return Stream.of(
            Arguments.of(nullDocumentList),
            Arguments.of(emptyDocumentList),
            Arguments.of(nullFirstDocument),
            Arguments.of(responseWithoutLinks),
            Arguments.of(responseWithNullSelf),
            Arguments.of(responseWithNullBinary),
            Arguments.of(responseWithBlankSelf),
            Arguments.of(responseWithBlankBinary)
        );
    }
}
