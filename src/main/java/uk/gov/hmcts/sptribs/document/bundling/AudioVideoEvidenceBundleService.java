package uk.gov.hmcts.sptribs.document.bundling;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.sptribs.cdam.model.UploadResponse;
import uk.gov.hmcts.sptribs.common.ccd.CcdCaseType;
import uk.gov.hmcts.sptribs.common.ccd.CcdJurisdiction;
import uk.gov.hmcts.sptribs.document.bundling.model.AudioVideoEvidenceBundleDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.document.service.DocumentsService;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
@RequiredArgsConstructor
@Slf4j
public class AudioVideoEvidenceBundleService {

    private static final String UNKNOWN = "Unknown";
    private static final String AUDIO_DOCUMENT_TYPE = "Audio Document";
    private static final String VIDEO_DOCUMENT_TYPE = "Video Document";
    private static final String AUDIO_VIDEO_EVIDENCE_TEMPLATE = "/templates/audio_video_evidence.html";

    private final PDFServiceClient pdfServiceClient;
    private final CaseDocumentClientApi caseDocumentClientApi;
    private final DocumentsService documentsService;
    private final ManageCaseDocumentUrlBuilder manageCaseDocumentUrlBuilder;
    private final AuthTokenGenerator authTokenGenerator;
    private final HttpServletRequest request;
    private final Clock clock;

    public Optional<AudioVideoEvidenceBundleDocument> createAudioVideoEvidenceBundleDocument(Long caseId) {
        try {
            return createBundleDocument(caseId);
        } catch (RuntimeException exception) {
            throw new AudioVideoEvidenceBundleException(caseId, exception);
        }
    }

    private Optional<AudioVideoEvidenceBundleDocument> createBundleDocument(Long caseId) {
        List<AudioVideoDocumentRow> rows = extractRows(caseId);
        if (rows.isEmpty()) {
            return Optional.empty();
        }

        String filename = "audio-video-evidence-" + caseId + ".pdf";
        byte[] pdf = generatePdf(rows, caseId);
        Document uploadedDocument = upload(pdf, filename);
        return Optional.of(buildBundleDocument(uploadedDocument));
    }

    List<AudioVideoDocumentRow> extractRows(Long caseId) {
        List<AudioVideoDocumentRow> rows = new ArrayList<>();
        for (DocumentEntity entity : documentsService.getAudioVideoDocuments(caseId)) {
            rows.add(toRow(entity));
        }
        return rows;
    }

    private AudioVideoDocumentRow toRow(DocumentEntity entity) {
        LocalDate savedDate = entity.getSavedAt() == null ? null : entity.getSavedAt().toLocalDate();
        return new AudioVideoDocumentRow(
            resolveMediaType(entity.getDocumentFilename()),
            entity.getDocumentFilename(),
            manageCaseDocumentUrlBuilder.buildPublicBinaryUrl(entity.getDocumentBinaryUrl()),
            savedDate == null ? UNKNOWN : savedDate.toString(),
            resolveDocumentType(entity.getDocumentTypeName())
        );
    }

    private String resolveMediaType(String documentFilename) {
        String extension = StringUtils.substringAfterLast(
            documentFilename,
            "."
        ).toLowerCase(Locale.ROOT);

        return switch (extension) {
            case "mp3" -> AUDIO_DOCUMENT_TYPE;
            case "mp4" -> VIDEO_DOCUMENT_TYPE;
            default -> throw new IllegalStateException("Unsupported audio/video document extension");
        };
    }

    private String resolveDocumentType(String documentTypeName) {
        if (StringUtils.isBlank(documentTypeName)) {
            return UNKNOWN;
        }
        try {
            return DocumentType.valueOf(documentTypeName).getLabel();
        } catch (IllegalArgumentException e) {
            return documentTypeName;
        }
    }

    private byte[] generatePdf(List<AudioVideoDocumentRow> rows, Long caseId) {
        byte[] template = loadTemplate();
        Map<String, Object> placeholders = Map.of(
            "caseId", String.valueOf(caseId),
            "rowsHtml", buildRowsHtml(rows)
        );
        log.info("Generating audio/video evidence PDF for case {} with {} rows", caseId, rows.size());
        byte[] pdf = pdfServiceClient.generateFromHtml(template, placeholders);
        if (pdf == null || pdf.length == 0) {
            throw new IllegalStateException("PDF service returned an empty audio/video evidence document");
        }
        return pdf;
    }

    private String buildRowsHtml(List<AudioVideoDocumentRow> rows) {
        StringBuilder rowsHtml = new StringBuilder();
        for (AudioVideoDocumentRow row : rows) {
            rowsHtml.append("<tr><td>")
                .append(escapeHtml(row.documentType()))
                .append("</td><td>")
                .append(buildDocumentLink(row.documentFilename(), row.documentUrl()))
                .append("</td><td>")
                .append(escapeHtml(row.dateAdded()))
                .append("</td><td>")
                .append(escapeHtml(row.documentCategory()))
                .append("</td></tr>");
        }
        return rowsHtml.toString();
    }

    private byte[] loadTemplate() {
        InputStream inputStream = getClass().getResourceAsStream(AUDIO_VIDEO_EVIDENCE_TEMPLATE);
        if (inputStream == null) {
            throw new IllegalStateException("Audio/video evidence PDF template was not found");
        }

        try (inputStream) {
            return IOUtils.toByteArray(inputStream);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load audio/video evidence PDF template", exception);
        }
    }

    private String buildDocumentLink(String filename, String documentUrl) {
        String safeFileName = escapeHtml(filename);
        String safeUrl = escapeHtml(documentUrl);
        return "<a href=\"" + safeUrl + "\">" + safeFileName + "</a>";
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }

    private AudioVideoEvidenceBundleDocument buildBundleDocument(Document generatedPdf) {
        return AudioVideoEvidenceBundleDocument.builder()
            .documentLink(generatedPdf)
            .date(LocalDate.now(clock))
            .build();
    }

    private Document upload(byte[] pdf, String fileName) {
        String serviceToken = authTokenGenerator.generate();
        String authorizationHeader = request.getHeader(AUTHORIZATION);

        UploadResponse response = caseDocumentClientApi.uploadDocuments(
            authorizationHeader,
            serviceToken,
            buildUploadRequest(pdf, fileName)
        );
        return toCcdDocument(response, fileName);
    }

    private DocumentUploadRequest buildUploadRequest(byte[] pdf, String fileName) {
        return new DocumentUploadRequest(
            Classification.RESTRICTED.toString(),
            CcdCaseType.CIC.getCaseTypeName(),
            CcdJurisdiction.CRIMINAL_INJURIES_COMPENSATION.getJurisdictionId(),
            List.of(new InMemoryMultipartFile(fileName, pdf))
        );
    }

    private Document toCcdDocument(UploadResponse response, String filename) {
        if (response == null || response.getDocuments() == null || response.getDocuments().isEmpty()) {
            throw invalidUploadResponse();
        }

        uk.gov.hmcts.sptribs.cdam.model.Document uploaded = response.getDocuments().getFirst();
        if (uploaded == null
            || uploaded.links == null
            || uploaded.links.self == null
            || uploaded.links.binary == null
            || StringUtils.isBlank(uploaded.links.self.href)
            || StringUtils.isBlank(uploaded.links.binary.href)) {
            throw invalidUploadResponse();
        }

        return Document.builder()
            .filename(filename)
            .url(uploaded.links.self.href)
            .binaryUrl(uploaded.links.binary.href)
            .build();
    }

    private IllegalStateException invalidUploadResponse() {
        return new IllegalStateException("CDAM returned an incomplete audio/video evidence document");
    }

    record AudioVideoDocumentRow(
        String documentType,
        String documentFilename,
        String documentUrl,
        String dateAdded,
        String documentCategory
    ) {
    }
}
