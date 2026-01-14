package uk.gov.hmcts.sptribs.document;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.cdam.model.Document;
import uk.gov.hmcts.sptribs.document.model.DownloadedDocumentResponse;
import uk.gov.hmcts.sptribs.exception.DocumentDownloadException;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;

import java.net.URLConnection;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentDownloadService {

    private final CaseDocumentClientApi caseDocumentClientApi;
    private final AuthTokenGenerator authTokenGenerator;

    public DownloadedDocumentResponse downloadDocument(String authorisation, String documentId) {
        log.info("Downloading document with id: {}", documentId);

        try {
            UUID documentUuid = UUID.fromString(documentId);
            String serviceAuth = authTokenGenerator.generate();

            Document documentMetadata = getDocumentMetadata(authorisation, serviceAuth, documentUuid);

            ResponseEntity<byte[]> binaryResponse = caseDocumentClientApi.getDocumentBinary(
                authorisation,
                serviceAuth,
                documentUuid
            );

            if (binaryResponse == null || binaryResponse.getBody() == null) {
                throw new DocumentDownloadException("Failed to download document binary for id: " + documentId);
            }

            String fileName = documentMetadata.originalDocumentName;
            String mimeType = getMimeType(fileName, documentMetadata.mimeType);

            return new DownloadedDocumentResponse(
                new ByteArrayResource(binaryResponse.getBody()),
                fileName,
                mimeType
            );
        } catch (IllegalArgumentException e) {
            log.error("Invalid document ID format: {}", documentId, e);
            throw new DocumentDownloadException("Invalid document ID format: " + documentId, e);
        } catch (DocumentDownloadException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to download document with id: {}", documentId, e);
            throw new DocumentDownloadException("Failed to download document: " + documentId, e);
        }
    }

    private Document getDocumentMetadata(String authorisation, String serviceAuth, UUID documentId) {
        log.info("Getting metadata for document: {}", documentId);

        ResponseEntity<Document> metadataResponse = caseDocumentClientApi.getDocument(
            authorisation,
            serviceAuth,
            documentId
        );

        if (metadataResponse == null || metadataResponse.getBody() == null) {
            throw new DocumentDownloadException("Failed to get document metadata for id: " + documentId);
        }

        return metadataResponse.getBody();
    }

    private String getMimeType(String fileName, String documentMimeType) {
        if (documentMimeType != null && !documentMimeType.isBlank()) {
            return documentMimeType;
        }

        if (fileName != null) {
            String guessedType = URLConnection.guessContentTypeFromName(fileName);
            if (guessedType != null) {
                return guessedType;
            }
        }

        return "application/octet-stream";
    }
}






