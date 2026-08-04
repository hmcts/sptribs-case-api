package uk.gov.hmcts.sptribs.document.bundling;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.cdam.model.UploadResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdCaseType;
import uk.gov.hmcts.sptribs.common.ccd.CcdJurisdiction;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.document.service.DocumentsService;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
@RequiredArgsConstructor
@Slf4j
public class AudioVideoEvidenceBundleService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String UNKNOWN_TYPE = "Unknown";
    private static final String EMPTY_VALUE = "";
    private static final String AUDIO_VIDEO_EVIDENCE_TEMPLATE = "/templates/audio_video_evidence.html";

    private final PDFServiceClient pdfServiceClient;
    private final CaseDocumentClientApi caseDocumentClientApi;
    private final DocumentsService documentsService;
    private final AuthTokenGenerator authTokenGenerator;
    private final HttpServletRequest request;

    public Document createAudioVideoEvidenceBundleDocument(CaseData caseData, Long caseId) {
        List<AudioVideoDocumentRow> rows = extractRows(caseData);
        if (rows.isEmpty()) {
            return null;
        }
        byte[] pdf = generatePdf(rows, caseId);
        String fileName = "audio-video-evidence-" + caseId + ".pdf";
        return upload(pdf, fileName);
    }

    List<AudioVideoDocumentRow> extractRows(CaseData caseData) {
        List<AudioVideoDocumentRow> rows = new ArrayList<>();
        List<ListValue<CaseworkerCICDocument>> allDocuments = DocumentListUtil.getAllCaseDocuments(caseData);
        Map<String, DocumentEntity> persistedDocuments = getPersistedDocuments(caseData, allDocuments);

        for (ListValue<CaseworkerCICDocument> documentListValue : allDocuments) {
            if (documentListValue == null || documentListValue.getValue() == null) {
                continue;
            }

            CaseworkerCICDocument document = documentListValue.getValue();
            if (!isAudioVideoDocument(document)) {
                continue;
            }

            String documentType = document.getDocumentCategory() != null
                ? document.getDocumentCategory().getLabel()
                : UNKNOWN_TYPE;
            String documentUrl = document.getDocumentLink().getBinaryUrl();
            DocumentEntity persistedDocument = persistedDocuments.get(documentUrl);
            String dateAdded = formatDate(getSavedAtDate(persistedDocument));

            rows.add(new AudioVideoDocumentRow(
                documentType,
                document.getDocumentLink().getFilename(),
                documentUrl,
                dateAdded,
                EMPTY_VALUE,
                EMPTY_VALUE,
                getSavedAtDate(persistedDocument)
            ));
        }

        rows.sort(
            Comparator.comparing(
                AudioVideoDocumentRow::sortableDate,
                Comparator.nullsLast(Comparator.naturalOrder())
            )
        );
        return rows;
    }

    private Map<String, DocumentEntity> getPersistedDocuments(CaseData caseData,
                                                              List<ListValue<CaseworkerCICDocument>> allDocuments) {
        Long caseReference = caseData.getCaseNumber() != null
            ? Long.valueOf(caseData.getCaseNumber())
            : null;
        if (caseReference == null) {
            return Map.of();
        }

        Set<String> binaryUrls = new LinkedHashSet<>();
        for (ListValue<CaseworkerCICDocument> documentListValue : allDocuments) {
            if (documentListValue == null || documentListValue.getValue() == null) {
                continue;
            }
            CaseworkerCICDocument document = documentListValue.getValue();
            if (isAudioVideoDocument(document)) {
                binaryUrls.add(document.getDocumentLink().getBinaryUrl());
            }
        }

        return documentsService.getCaseDocumentsByBinaryUrls(caseReference, List.copyOf(binaryUrls));
    }

    private boolean isAudioVideoDocument(CaseworkerCICDocument document) {
        if (document.getDocumentLink() == null || StringUtils.isBlank(document.getDocumentLink().getFilename())) {
            return false;
        }
        if (StringUtils.isBlank(document.getDocumentLink().getBinaryUrl())) {
            return false;
        }

        String extension = StringUtils.substringAfterLast(
            document.getDocumentLink().getFilename(),
            "."
        ).toLowerCase(Locale.ROOT);
        return "mp3".equals(extension) || "mp4".equals(extension);
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return EMPTY_VALUE;
        }
        return DATE_FORMATTER.format(date);
    }

    private LocalDate getSavedAtDate(DocumentEntity documentEntity) {
        if (documentEntity == null) {
            return null;
        }
        OffsetDateTime savedAt = documentEntity.getSavedAt();
        return savedAt == null ? null : savedAt.toLocalDate();
    }

    private byte[] generatePdf(List<AudioVideoDocumentRow> rows, Long caseId) {
        byte[] template = loadTemplate();
        Map<String, Object> placeholders = Map.of(
            "caseId", String.valueOf(caseId),
            "rowsHtml", buildRowsHtml(rows)
        );
        log.info("Generating audio/video evidence PDF for case {} with {} rows", caseId, rows.size());
        return pdfServiceClient.generateFromHtml(template, placeholders);
    }

    private String buildRowsHtml(List<AudioVideoDocumentRow> rows) {
        StringBuilder rowsHtml = new StringBuilder();
        if (rows.isEmpty()) {
            rowsHtml.append("<tr><td colspan=\"5\">No active MP3/MP4 documents found.</td></tr>");
        } else {
            for (AudioVideoDocumentRow row : rows) {
                rowsHtml.append("<tr><td>")
                    .append(escapeHtml(row.documentType()))
                    .append("</td><td>")
                    .append(buildDocumentLink(row.documentFilename(), row.documentUrl()))
                    .append("</td><td>")
                    .append(escapeHtml(row.dateAdded()))
                    .append("</td><td>")
                    .append(escapeHtml(row.dateApproved()))
                    .append("</td><td>")
                    .append(escapeHtml(row.uploadedBy()))
                    .append("</td></tr>");
            }
        }
        return rowsHtml.toString();
    }

    private byte[] loadTemplate() {
        try (InputStream inputStream = getClass().getResourceAsStream(AUDIO_VIDEO_EVIDENCE_TEMPLATE)) {
            return IOUtils.toByteArray(Objects.requireNonNull(inputStream));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load audio/video evidence PDF template", exception);
        }
    }

    private String buildDocumentLink(String filename, String documentUrl) {
        if (StringUtils.isBlank(documentUrl)) {
            return "";
        }

        String safeFileName = escapeHtml(StringUtils.defaultIfBlank(filename, documentUrl));
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

    private Document upload(byte[] pdf, String fileName) {
        InMemoryMultipartFile multipartFile = new InMemoryMultipartFile(fileName, pdf);
        DocumentUploadRequest uploadRequest = new DocumentUploadRequest(
            Classification.RESTRICTED.toString(),
            CcdCaseType.CIC.getCaseTypeName(),
            CcdJurisdiction.CRIMINAL_INJURIES_COMPENSATION.getJurisdictionId(),
            singletonList(multipartFile)
        );

        String serviceToken = authTokenGenerator.generate();
        String authorizationHeader = request.getHeader(AUTHORIZATION);

        UploadResponse uploadResponse = caseDocumentClientApi.uploadDocuments(
            authorizationHeader,
            serviceToken,
            uploadRequest
        );
        if (uploadResponse == null || uploadResponse.getDocuments() == null || uploadResponse.getDocuments().isEmpty()) {
            throw new IllegalStateException("Unable to upload audio/video evidence bundle document");
        }

        uk.gov.hmcts.sptribs.cdam.model.Document uploadedDocument = uploadResponse.getDocuments().getFirst();
        Document document = new Document();
        document.setFilename(fileName);
        document.setUrl(uploadedDocument.links.self.href);
        document.setBinaryUrl(uploadedDocument.links.binary.href);
        return document;
    }

    record AudioVideoDocumentRow(
        String documentType,
        String documentFilename,
        String documentUrl,
        String dateAdded,
        String dateApproved,
        String uploadedBy,
        LocalDate sortableDate
    ) {
    }
}
