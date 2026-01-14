package uk.gov.hmcts.sptribs.document.model;

import org.springframework.core.io.Resource;

public record DownloadedDocumentResponse(Resource file, String fileName, String mimeType) {
}






